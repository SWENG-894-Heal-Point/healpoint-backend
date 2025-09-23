package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.dto.RegistrationFormDto;
import edu.psgv.healpointbackend.model.*;
import edu.psgv.healpointbackend.repository.*;

import edu.psgv.healpointbackend.utilities.PasswordUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private EmployeeAccountRepository employeeAccountRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void checkIfUserExists_trueWhenPresent() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mock(User.class)));

        Boolean exists = registrationService.checkIfUserExists("test@example.com");

        assertTrue(exists);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void checkIfUserExists_falseWhenAbsent() {
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());

        Boolean exists = registrationService.checkIfUserExists("john.doe@example.com");

        assertFalse(exists);
        verify(userRepository).findByEmail("john.doe@example.com");
    }

    @Test
    void checkIfUserExists_invalidInput_throwException() {
        assertThrows(IllegalArgumentException.class, () -> registrationService.checkIfUserExists(null));
        assertThrows(IllegalArgumentException.class, () -> registrationService.checkIfUserExists(""));
        assertThrows(IllegalArgumentException.class, () -> registrationService.checkIfUserExists("   "));
        verifyNoInteractions(userRepository);
    }

    @Test
    void registerUser_successfulRegistration_returns200() {
        try (MockedStatic<PasswordUtils> passwordUtilsMock = mockStatic(PasswordUtils.class)) {
            RegistrationFormDto request = new RegistrationFormDto();
            request.setEmail("newuser@example.com");
            request.setPassword("password123*");
            request.setConfirmPassword("password123*");
            request.setRole("PATIENT");
            request.setFirstName("John");
            request.setLastName("Doe");
            request.setDateOfBirth(LocalDate.parse("1990-01-01"));
            request.setGender("Male");
            request.setPhone("1234567890");
            request.setStreetAddress("123 Main St");
            request.setCity("Springfield");
            request.setState("IL");
            request.setZipCode("62704");

            when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
            Role role = new Role();
            role.setDescription("PATIENT");
            when(roleRepository.findByDescription("PATIENT")).thenReturn(Optional.of(role));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

            passwordUtilsMock.when(() -> PasswordUtils.hashPassword("password123*")).thenReturn("hashedPassword");

            ResponseEntity<String> response = registrationService.registerUser(request);

            assertEquals(200, response.getStatusCode().value());
            assertEquals("User registered successfully", response.getBody());
            passwordUtilsMock.verify(() -> PasswordUtils.hashPassword("password123*"));
        }
    }

    @Test
    void registerUser_userAlreadyExists_returns409() {
        RegistrationFormDto request = mock(RegistrationFormDto.class);
        when(request.getEmail()).thenReturn("existing@example.com");
        when(request.getPassword()).thenReturn("password123");
        when(request.getConfirmPassword()).thenReturn("password123");
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(mock(User.class)));

        ResponseEntity<String> response = registrationService.registerUser(request);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("User already exists.", response.getBody());
    }

    @Test
    void registerUser_passwordsDoNotMatch_returns400() {
        RegistrationFormDto request = mock(RegistrationFormDto.class);
        when(request.getEmail()).thenReturn("newuser@example.com");
        when(request.getPassword()).thenReturn("password123");
        when(request.getConfirmPassword()).thenReturn("password456");

        ResponseEntity<String> response = registrationService.registerUser(request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Passwords do not match.", response.getBody());
    }

    @Test
    void registerUser_invalidRole_returns500() {
        RegistrationFormDto request = mock(RegistrationFormDto.class);
        when(request.getEmail()).thenReturn("newuser@example.com");
        when(request.getPassword()).thenReturn("password123");
        when(request.getConfirmPassword()).thenReturn("password123");
        when(request.getRole()).thenReturn("INVALID_ROLE");
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByDescription("INVALID_ROLE")).thenReturn(Optional.empty());

        ResponseEntity<String> response = registrationService.registerUser(request);

        assertEquals(500, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid role."));
    }

    @Test
    void registerUser_employeeEmailNotFound_returns500() {
        RegistrationFormDto request = mock(RegistrationFormDto.class);
        when(request.getEmail()).thenReturn("invalidEmployee@email.com");
        when(request.getPassword()).thenReturn("password123");
        when(request.getConfirmPassword()).thenReturn("password123");
        when(request.getRole()).thenReturn("ADMIN");
        when(userRepository.findByEmail("invalidEmployee@email.com")).thenReturn(Optional.empty());
        when(employeeAccountRepository.findByEmail("invalidEmployee@email.com")).thenReturn(Optional.empty());

        Role role = new Role();
        role.setDescription("ADMIN");
        when(roleRepository.findByDescription("ADMIN")).thenReturn(Optional.of(role));

        ResponseEntity<String> response = registrationService.registerUser(request);
        assertEquals(500, response.getStatusCode().value());
        assertTrue(response.getBody().contains("The provided employee email does not exist in the system."));
    }

    @Test
    void registerUser_exceptionDuringRegistration_returns500() {
        RegistrationFormDto request = mock(RegistrationFormDto.class);
        when(request.getEmail()).thenReturn("newuser@example.com");
        when(request.getPassword()).thenReturn("password123");
        when(request.getConfirmPassword()).thenReturn("password123");
        when(request.getRole()).thenReturn("PATIENT");
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        Role role = new Role();
        role.setDescription("PATIENT");
        when(roleRepository.findByDescription("PATIENT")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<String> response = registrationService.registerUser(request);

        assertEquals(500, response.getStatusCode().value());
    }
}
