package edu.psgv.healpointbackend.controller;

import edu.psgv.healpointbackend.dto.AuthenticationFormDto;
import edu.psgv.healpointbackend.dto.TokenDto;
import edu.psgv.healpointbackend.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController controller;

    private AuthenticationFormDto authForm;
    private TokenDto tokenDto;

    @BeforeEach
    void setUp() {
        authForm = new AuthenticationFormDto();
        authForm.setEmail("user@example.com");
        authForm.setPassword("password123");

        tokenDto = new TokenDto();
        tokenDto.setToken("jwt-token-xyz");
    }

    @Test
    void authenticateUser_whenServiceReturns200() {
        // Arrange
        ResponseEntity<String> serviceResponse = ResponseEntity.ok("jwt-token-xyz");
        when(authenticationService.authenticateUser(authForm)).thenReturn(serviceResponse);

        // Act
        ResponseEntity<String> resp = controller.authenticateUser(authForm);

        // Assert
        assertEquals(200, resp.getStatusCode().value());
        assertEquals("jwt-token-xyz", resp.getBody());
        verify(authenticationService).authenticateUser(authForm);
    }

    @Test
    void authenticateUser_whenServiceReturnsUnknownEmail() {
        // Arrange
        ResponseEntity<String> serviceResponse = ResponseEntity.status(401).body("No account associated with this email address.");
        when(authenticationService.authenticateUser(authForm)).thenReturn(serviceResponse);

        // Act
        ResponseEntity<String> resp = controller.authenticateUser(authForm);

        // Assert
        assertEquals(401, resp.getStatusCode().value());
        assertEquals("No account associated with this email address.", resp.getBody());
        verify(authenticationService).authenticateUser(authForm);
    }

    @Test
    void authenticateUser_whenServiceThrowsIllegalArgument_returns400() {
        // Arrange
        when(authenticationService.authenticateUser(authForm)).thenThrow(new IllegalArgumentException("Invalid payload"));

        // Act
        ResponseEntity<String> resp = controller.authenticateUser(authForm);

        // Assert
        assertEquals(400, resp.getStatusCode().value());
        assertEquals("Invalid payload", resp.getBody());
        verify(authenticationService).authenticateUser(authForm);
    }

    @Test
    void logoutUser_whenServiceSucceeds_returns200() {
        // Act
        ResponseEntity<String> resp = controller.logoutUser(tokenDto);

        // Assert
        assertEquals(200, resp.getStatusCode().value());
        assertEquals("User logged out successfully.", resp.getBody());
        verify(authenticationService).logoutUser("jwt-token-xyz");
    }

    @Test
    void logoutUser_whenServiceThrowsIllegalArgument_returns400() {
        // Arrange
        doThrow(new IllegalArgumentException("Bad token")).when(authenticationService).logoutUser("jwt-token-xyz");

        // Act
        ResponseEntity<String> resp = controller.logoutUser(tokenDto);

        // Assert
        assertEquals(400, resp.getStatusCode().value());
        assertEquals("Bad token", resp.getBody());
        verify(authenticationService).logoutUser("jwt-token-xyz");
    }
}
