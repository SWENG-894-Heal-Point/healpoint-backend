package edu.psgv.healpointbackend.controller;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;

import edu.psgv.healpointbackend.dto.RegistrationFormDto;
import edu.psgv.healpointbackend.service.RegistrationService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * REST controller for user registration and existence checking endpoints.
 *
 * @author Mahfuzur Rahman
 */
@RestController
public class RegistrationController {
    private final RegistrationService registrationService;

    /**
     * Constructs a RegistrationController with the given RegistrationService.
     *
     * @param registrationService the service handling registration logic
     */
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }


    /**
     * Checks if a user exists by email.
     *
     * @param email the email address to check for existence
     * @return ResponseEntity containing true if user exists, false otherwise; bad request if input is invalid
     */
    @GetMapping("/api/user-existence")
    public ResponseEntity<Boolean> userExistence(@RequestParam(value = "email", defaultValue = "") String email) {
        try {
            LOGGER.info("Checking user existence for email: {}", email);
            Boolean exists = registrationService.checkIfUserExists(email);

            LOGGER.info("User existence result for {}: {}", email, exists);
            return ResponseEntity.ok(exists);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Error checking user existence for {}: {}", email, e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Registers a new user with the provided registration form data.
     *
     * @param request the registration form data
     * @return ResponseEntity containing registration result message and status code
     */
    @PostMapping("/api/register-user")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegistrationFormDto request) {
        try {
            LOGGER.info("Registering user with email: {}", request.getEmail());
            ResponseEntity<String> response = registrationService.registerUser(request);

            LOGGER.info("Registration completed for {}: {}", request.getEmail(), response.getStatusCode());
            return response;
        } catch (IllegalArgumentException e) {
            LOGGER.error("Error registering user {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
