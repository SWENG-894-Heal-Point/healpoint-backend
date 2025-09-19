package edu.psgv.healpointbackend.controller;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;

import edu.psgv.healpointbackend.dto.RegistrationFormDto;
import edu.psgv.healpointbackend.service.RegistrationService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class RegistrationController {
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/api/user-existence")
    public ResponseEntity<Boolean> userExistence(@RequestParam(value = "email", defaultValue = "") String email) {
         throw new UnsupportedOperationException("Not implemented yet.");
    }

    @PostMapping("/api/register-user")
    public ResponseEntity<Boolean> registerUser(@Valid @RequestBody RegistrationFormDto request) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
