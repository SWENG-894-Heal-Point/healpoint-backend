package edu.psgv.healpointbackend.controller;

import edu.psgv.healpointbackend.dto.RegistrationFormDto;
import edu.psgv.healpointbackend.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerTest {
    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private RegistrationController registrationController;

    @Test
    void userExistence_userExists_returnsTrue() {
        when(registrationService.checkIfUserExists("user@example.com")).thenReturn(true);

        ResponseEntity<Boolean> response = registrationController.userExistence("user@example.com");

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody());
    }

    @Test
    void userExistence_userDoesNotExist_returnsFalse() {
        when(registrationService.checkIfUserExists("nouser@example.com")).thenReturn(false);

        ResponseEntity<Boolean> response = registrationController.userExistence("nouser@example.com");

        assertEquals(200, response.getStatusCode().value());
        assertFalse(response.getBody());
    }

    @Test
    void userExistence_invalidInput_returnsBadRequest() {
        when(registrationService.checkIfUserExists("")).thenThrow(new IllegalArgumentException("Invalid email"));

        ResponseEntity<Boolean> response = registrationController.userExistence("");

        assertEquals(400, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void registerUser_successfulRegistration_returnsOkResponse() {
        RegistrationFormDto dto = mock(RegistrationFormDto.class);
        ResponseEntity<String> serviceResponse = ResponseEntity.ok("User registered successfully");
        when(registrationService.registerUser(dto)).thenReturn(serviceResponse);

        ResponseEntity<String> response = registrationController.registerUser(dto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("User registered successfully", response.getBody());
    }

    @Test
    void registerUser_userAlreadyExists_returnsConflictResponse() {
        RegistrationFormDto dto = mock(RegistrationFormDto.class);
        ResponseEntity<String> serviceResponse = ResponseEntity.status(409).body("User already exists");
        when(registrationService.registerUser(dto)).thenReturn(serviceResponse);

        ResponseEntity<String> response = registrationController.registerUser(dto);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("User already exists", response.getBody());
    }

    @Test
    void registerUser_invalidInput_returnsBadRequestWithFalseBody() {
        RegistrationFormDto dto = mock(RegistrationFormDto.class);
        when(registrationService.registerUser(dto)).thenThrow(new IllegalArgumentException("Invalid input"));

        ResponseEntity<String> response = registrationController.registerUser(dto);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid input", response.getBody());
    }

    @Test
    void registerUser_serviceThrowsRuntimeException_returnsInternalServerError() {
        RegistrationFormDto dto = mock(RegistrationFormDto.class);
        when(registrationService.registerUser(dto)).thenThrow(new RuntimeException("Unexpected error"));

        assertThrows(RuntimeException.class, () -> registrationController.registerUser(dto));
    }
}