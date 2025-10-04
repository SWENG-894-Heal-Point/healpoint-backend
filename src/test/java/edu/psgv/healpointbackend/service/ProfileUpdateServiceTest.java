package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.AbstractTestBase;
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


class ProfileUpdateServiceTest extends AbstractTestBase {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private Datastore datastore;

    @InjectMocks
    private ProfileUpdateService profileUpdateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        profileUpdateService = new ProfileUpdateService(userRepository, patientRepository, doctorRepository, datastore);
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

            profileUpdateService.updatePassword(dto);

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
        assertThrows(SecurityException.class, () -> profileUpdateService.updatePassword(dto1));

        // Case 2 & 3 require static mocks
        User user = new User("test@example.com", "hashedOld", null);
        when(datastore.getUserByToken("token")).thenReturn(user);

        try (MockedStatic<PasswordUtils> mocked = mockStatic(PasswordUtils.class)) {
            // Case 2: wrong old password
            NewPasswordDto dto2 = mockPasswordDto("token", "wrongOld", "newPass", "newPass");
            mocked.when(() -> PasswordUtils.verifyPassword("wrongOld", "hashedOld")).thenReturn(false);
            assertThrows(SecurityException.class, () -> profileUpdateService.updatePassword(dto2));

            // Case 3: mismatch new password
            NewPasswordDto dto3 = mockPasswordDto("token", "oldPass", "newPass", "mismatchPass");
            mocked.when(() -> PasswordUtils.verifyPassword("oldPass", "hashedOld")).thenReturn(true);
            mocked.when(() -> PasswordUtils.verifyPassword("newPass", "mismatchPass")).thenReturn(false);
            assertThrows(IllegalArgumentException.class, () -> profileUpdateService.updatePassword(dto3));
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

        String updatedEmail = profileUpdateService.updateUserProfile(dto, "patient@example.com");

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

        String updatedEmail = profileUpdateService.updateUserProfile(dto, "doctor@example.com");

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

        assertThrows(EntityNotFoundException.class, () -> profileUpdateService.updateUserProfile(dto, "old@example.com"));
    }

    @Test
    void updateUserProfile_patientProfileMissing_throwsEntityNotFoundException() {
        User user = mockUser(12, "patient2@example.com", Roles.PATIENT);
        UpdateProfileDto dto = new UpdateProfileDto();
        dto.setEmail("patient2@example.com");

        when(userRepository.findByEmailIgnoreCase(dto.getEmail())).thenReturn(Optional.of(user));
        when(patientRepository.findById(12)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> profileUpdateService.updateUserProfile(dto, "patient2@example.com"));
    }

    @Test
    void updateUserProfile_doctorProfileMissing_throwsEntityNotFoundException() {
        User user = mockUser(13, "doctor2@example.com", Roles.DOCTOR);
        UpdateProfileDto dto = new UpdateProfileDto();
        dto.setEmail("doctor2@example.com");

        when(userRepository.findByEmailIgnoreCase(dto.getEmail())).thenReturn(Optional.of(user));
        when(doctorRepository.findById(13)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> profileUpdateService.updateUserProfile(dto, "doctor2@example.com"));
    }
}
