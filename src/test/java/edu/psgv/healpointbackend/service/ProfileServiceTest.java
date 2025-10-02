package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.common.state.Datastore;
import edu.psgv.healpointbackend.dto.NewPasswordDto;
import edu.psgv.healpointbackend.dto.UpdateProfileDto;
import edu.psgv.healpointbackend.model.*;
import edu.psgv.healpointbackend.repository.DoctorRepository;
import edu.psgv.healpointbackend.repository.PatientRepository;
import edu.psgv.healpointbackend.repository.UserRepository;
import edu.psgv.healpointbackend.utilities.PasswordUtils;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private Datastore datastore;

    @InjectMocks
    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        profileService = new ProfileService(userRepository, patientRepository, doctorRepository, datastore);
    }

    private User mockUser(Integer id, String email, String roleDesc) {
        Role role = new Role();
        String hashedPassword = "hashedPassword";
        role.setDescription(roleDesc);
        User user = new User(email, hashedPassword, role);

        if (id != null) {
            ReflectionTestUtils.setField(user, "id", id);
        }

        return user;
    }

    private NewPasswordDto mockPasswordDto(String token, String oldPassword, String newPassword, String confirmPassword) {
        NewPasswordDto dto = new NewPasswordDto();
        dto.setToken(token);
        dto.setOldPassword(oldPassword);
        dto.setNewPassword(newPassword);
        dto.setConfirmNewPassword(confirmPassword);
        return dto;
    }

    @Test
    void getUserProfile_userNotFound_returns401Response() {
        when(userRepository.findByEmailIgnoreCase("notfound@example.com")).thenReturn(Optional.empty());

        ResponseEntity<Object> response = profileService.getUserProfile("notfound@example.com", null);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("No account associated with this email address.", response.getBody());
    }

    @Test
    void getUserProfile_patientFound_returnsPatientProfile() {
        User user = mockUser(1, "patient@example.com", Roles.PATIENT);
        Patient patient = Patient.builder().id(1).build();

        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));

        ResponseEntity<Object> response = profileService.getUserProfile(user.getEmail(), "patient");

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof PatientProfile);
    }

    @Test
    void getUserProfile_patientProfileMissing_returns404Response() {
        User user = mockUser(2, "patient2@example.com", Roles.PATIENT);

        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(patientRepository.findById(2)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = profileService.getUserProfile(user.getEmail(), null);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Patient profile not found.", response.getBody());
    }

    @Test
    void getUserProfile_doctorFound_returnsDoctorProfile() {
        User user = mockUser(3, "doctor@example.com", Roles.DOCTOR);
        Doctor doctor = Doctor.builder().id(3).build();

        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(doctorRepository.findById(3)).thenReturn(Optional.of(doctor));

        ResponseEntity<Object> response = profileService.getUserProfile(user.getEmail(), "doctor");

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof DoctorProfile);
    }

    @Test
    void getUserProfile_doctorProfileMissing_returns404Response() {
        User user = mockUser(4, "doctor2@example.com", Roles.DOCTOR);

        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(doctorRepository.findById(4)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = profileService.getUserProfile(user.getEmail(), null);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Doctor profile not found.", response.getBody());
    }

    @Test
    void getUserProfile_userWithUnsupportedRole_returnsGenericMessage() {
        User user = mockUser(5, "staff@example.com", "SUPPORT STAFF");

        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));

        ResponseEntity<Object> response = profileService.getUserProfile(user.getEmail(), null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("This user does not have a profile.", response.getBody());
    }

    @Test
    void getUserProfile_repositoryThrowsException_returns500Response() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenThrow(new RuntimeException("DB failure"));

        ResponseEntity<Object> response = profileService.getUserProfile("boom@example.com", null);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("DB failure", response.getBody());
    }

    @Test
    void updatePassword_validInput_passwordUpdated() {
        User user = new User("test@example.com", "hashedOld", null);
        NewPasswordDto dto = mockPasswordDto("token", "oldPass", "newPass", "newPass");

        when(datastore.getUserByToken("token")).thenReturn(user);

        try (MockedStatic<PasswordUtils> mocked = mockStatic(PasswordUtils.class)) {
            mocked.when(() -> PasswordUtils.verifyPassword("oldPass", "hashedOld")).thenReturn(true);
            mocked.when(() -> PasswordUtils.verifyPassword("newPass", "newPass")).thenReturn(true);
            mocked.when(() -> PasswordUtils.hashPassword("newPass")).thenReturn("hashedNew");

            profileService.updatePassword(dto);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            verify(datastore).updateUser(captor.capture());

            User savedUser = captor.getValue();
            assertEquals("hashedNew", savedUser.getPassword());
        }
    }

    @Test
    void updatePassword_invalidInputs_throwExceptions() {
        // Case 1: User not found
        NewPasswordDto dto1 = mockPasswordDto("badToken", "oldPass", "newPass", "newPass");
        when(datastore.getUserByToken("badToken")).thenReturn(null);
        assertThrows(SecurityException.class, () -> profileService.updatePassword(dto1));

        // Case 2 & 3 require static mocks
        User user = new User("test@example.com", "hashedOld", null);
        when(datastore.getUserByToken("token")).thenReturn(user);

        try (MockedStatic<PasswordUtils> mocked = mockStatic(PasswordUtils.class)) {
            // Case 2: wrong old password
            NewPasswordDto dto2 = mockPasswordDto("token", "wrongOld", "newPass", "newPass");
            mocked.when(() -> PasswordUtils.verifyPassword("wrongOld", "hashedOld")).thenReturn(false);
            assertThrows(SecurityException.class, () -> profileService.updatePassword(dto2));

            // Case 3: mismatch new password
            NewPasswordDto dto3 = mockPasswordDto("token", "oldPass", "newPass", "mismatchPass");
            mocked.when(() -> PasswordUtils.verifyPassword("oldPass", "hashedOld")).thenReturn(true);
            mocked.when(() -> PasswordUtils.verifyPassword("newPass", "mismatchPass")).thenReturn(false);
            assertThrows(IllegalArgumentException.class, () -> profileService.updatePassword(dto3));
        }
    }

    @Test
    void updateUserProfile_existingPatient_updatesUserAndPatientProfile() {
        User user = mockUser(10, "patient@example.com", Roles.PATIENT);
        Patient patient = Patient.builder().id(10).build();

        UpdateProfileDto dto = new UpdateProfileDto();
        dto.setEmail("newPatient@example.com");
        dto.setToken("token");
        dto.setGender("M");
        dto.setPhone("1234567890");

        when(userRepository.findByEmailIgnoreCase(dto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(patientRepository.findById(10)).thenReturn(Optional.of(patient));

        User loggedUser = mockUser(10, "patient@example.com", Roles.PATIENT);
        when(datastore.getUserByToken("token")).thenReturn(loggedUser);

        String updatedEmail = profileService.updateUserProfile(dto, "patient@example.com");

        verify(userRepository).save(user);
        verify(patientRepository).save(patient);
        verify(datastore).updateUser(loggedUser);

        assertEquals("newPatient@example.com", updatedEmail);
    }

    @Test
    void updateUserProfile_existingDoctor_updatesUserAndDoctorProfile() {
        User user = mockUser(11, "doctor@example.com", Roles.DOCTOR);
        Doctor doctor = Doctor.builder().id(11).build();

        UpdateProfileDto dto = new UpdateProfileDto();
        dto.setEmail("newDoctor@example.com");
        dto.setToken("token");
        dto.setGender("F");
        dto.setPhone("9876543210");

        when(userRepository.findByEmailIgnoreCase(dto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(doctorRepository.findById(11)).thenReturn(Optional.of(doctor));

        User loggedUser = mockUser(11, "doctor@example.com", Roles.DOCTOR);
        when(datastore.getUserByToken("token")).thenReturn(loggedUser);

        String updatedEmail = profileService.updateUserProfile(dto, "doctor@example.com");

        verify(userRepository).save(user);
        verify(doctorRepository).save(doctor);
        verify(datastore).updateUser(loggedUser);

        assertEquals("newDoctor@example.com", updatedEmail);
    }

    @Test
    void updateUserProfile_userNotFound_throwsEntityNotFoundException() {
        UpdateProfileDto dto = new UpdateProfileDto();
        dto.setEmail("missing@example.com");

        when(userRepository.findByEmailIgnoreCase(dto.getEmail())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> profileService.updateUserProfile(dto, "old@example.com"));
    }

    @Test
    void updateUserProfile_patientProfileMissing_throwsEntityNotFoundException() {
        User user = mockUser(12, "patient2@example.com", Roles.PATIENT);
        UpdateProfileDto dto = new UpdateProfileDto();
        dto.setEmail("patient2@example.com");

        when(userRepository.findByEmailIgnoreCase(dto.getEmail())).thenReturn(Optional.of(user));
        when(patientRepository.findById(12)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> profileService.updateUserProfile(dto, "patient2@example.com"));
    }

    @Test
    void updateUserProfile_doctorProfileMissing_throwsEntityNotFoundException() {
        User user = mockUser(13, "doctor2@example.com", Roles.DOCTOR);
        UpdateProfileDto dto = new UpdateProfileDto();
        dto.setEmail("doctor2@example.com");

        when(userRepository.findByEmailIgnoreCase(dto.getEmail())).thenReturn(Optional.of(user));
        when(doctorRepository.findById(13)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> profileService.updateUserProfile(dto, "doctor2@example.com"));
    }
}
