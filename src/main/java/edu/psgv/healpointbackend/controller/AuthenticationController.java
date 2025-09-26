package edu.psgv.healpointbackend.controller;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;

import edu.psgv.healpointbackend.dto.AuthenticationFormDto;
import edu.psgv.healpointbackend.dto.TokenDto;
import edu.psgv.healpointbackend.service.AuthenticationService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * REST controller for handling user authentication and logout operations.
 *
 * @author Mahfuzur Rahman
 */
@RestController
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    /**
     * Constructs the AuthenticationController with the required AuthenticationService.
     *
     * @param authenticationService the authentication service to use
     */
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Authenticates a user based on the provided credentials.
     *
     * @param request the authentication form data (validated)
     * @return a ResponseEntity containing the authentication result or error message
     */
    @PostMapping("/api/authenticate-user")
    public ResponseEntity<String> authenticateUser(@Valid @RequestBody AuthenticationFormDto request) {
        LOGGER.info("Received authentication request for email: {}", request.getEmail());

        try {
            ResponseEntity<String> response = authenticationService.authenticateUser(request);
            LOGGER.info("Authentication response for {}: {}", request.getEmail(), response.getStatusCode());
            return response;
        } catch (IllegalArgumentException e) {
            LOGGER.error("Authentication error for {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Logs out a user based on the provided token.
     *
     * @param request the token data transfer object (validated)
     * @return a ResponseEntity indicating the logout result or error message
     */
    @PostMapping("/api/logout-user")
    public ResponseEntity<String> logoutUser(@Valid @RequestBody TokenDto request) {
        try {
            authenticationService.logoutUser(request.getToken());
            return ResponseEntity.ok("User logged out successfully.");
        } catch (IllegalArgumentException e) {
            LOGGER.error("Logout error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
