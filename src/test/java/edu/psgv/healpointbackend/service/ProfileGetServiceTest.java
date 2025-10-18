package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.AbstractTestBase;
import edu.psgv.healpointbackend.model.*;
import edu.psgv.healpointbackend.repository.DoctorRepository;
import edu.psgv.healpointbackend.repository.PatientRepository;
import edu.psgv.healpointbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProfileGetServiceTest extends AbstractTestBase {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @InjectMocks
    private ProfileGetService profileGetService;

    @BeforeEach
    void setUp() {
        profileGetService = new ProfileGetService(userRepository, patientRepository, doctorRepository);
    }

    @Test
    void getUserProfile_userNotFound_returns401Response() {
        when(userRepository.findByEmailIgnoreCase("notfound@example.com")).thenReturn(Optional.empty());

        ResponseEntity<Object> response = profileGetService.getUserProfile("notfound@example.com", null);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("No account associated with this email address.", response.getBody());
    }

    @Test
    void getUserProfile_patientFound_returnsPatientProfile() {
        User user = mockUser(1, "patient@example.com", Roles.PATIENT);
        Patient patient = Patient.builder().id(1).build();

        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));

        ResponseEntity<Object> response = profileGetService.getUserProfile(user.getEmail(), "patient");

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(PatientProfile.class, response.getBody());

    }

    @Test
    void getUserProfile_patientProfileMissing_returns404Response() {
        User user = mockUser(2, "patient2@example.com", Roles.PATIENT);

        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(patientRepository.findById(2)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = profileGetService.getUserProfile(user.getEmail(), null);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Patient profile not found.", response.getBody());
    }

    @Test
    void getUserProfile_doctorFound_returnsDoctorProfile() {
        User user = mockUser(3, "doctor@example.com", Roles.DOCTOR);
        Doctor doctor = Doctor.builder().id(3).build();

        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(doctorRepository.findById(3)).thenReturn(Optional.of(doctor));

        ResponseEntity<Object> response = profileGetService.getUserProfile(user.getEmail(), "doctor");

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(DoctorProfile.class, response.getBody());
    }

    @Test
    void getUserProfile_doctorProfileMissing_returns404Response() {
        User user = mockUser(4, "doctor2@example.com", Roles.DOCTOR);

        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(doctorRepository.findById(4)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = profileGetService.getUserProfile(user.getEmail(), null);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Doctor profile not found.", response.getBody());
    }

    @Test
    void getUserProfile_userWithUnsupportedRole_returnsGenericMessage() {
        User user = mockUser(5, "staff@example.com", "SUPPORT STAFF");

        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));

        ResponseEntity<Object> response = profileGetService.getUserProfile(user.getEmail(), null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("This user does not have a profile.", response.getBody());
    }

    @Test
    void getUserProfile_repositoryThrowsException_returns500Response() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenThrow(new RuntimeException("DB failure"));

        ResponseEntity<Object> response = profileGetService.getUserProfile("boom@example.com", null);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("DB failure", response.getBody());
    }

    @Test // FR-16.3 UT-21
    void getAllPatients_variousInputs_expectedOutputs() {
        // Arrange (covers multiple paths)
        Patient p1 = mockPatient(1, "John", "Doe");
        Patient p2 = mockPatient(2, "Alice", "Smith");
        Patient p3 = mockPatient(3, "Jane", "Roe");

        when(patientRepository.findAll()).thenReturn(List.of(p1, p2, p3));

        // Case 1: valid user for p1
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser(1, "john@example.com", "Patient")));
        // Case 2: no user found for p2
        when(userRepository.findById(2)).thenReturn(Optional.empty());
        // Case 3: valid user for p3
        when(userRepository.findById(3)).thenReturn(Optional.of(mockUser(3, "jane@example.com", "Patient")));

        // Act
        ArrayList<PatientProfile> result = profileGetService.getAllPatients();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getEmail().equals("john@example.com")));
        assertTrue(result.stream().anyMatch(p -> p.getEmail().equals("jane@example.com")));
        verify(patientRepository).findAll();
        verify(userRepository, times(3)).findById(anyInt());
    }

    @Test // FR-16.4 UT-22
    void getAllPatients_emptyRepository_returnsEmptyList() {
        // Arrange
        when(patientRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        ArrayList<PatientProfile> result = profileGetService.getAllPatients();

        // Assert
        assertTrue(result.isEmpty());
        verify(patientRepository).findAll();
        verifyNoInteractions(userRepository);
    }
}