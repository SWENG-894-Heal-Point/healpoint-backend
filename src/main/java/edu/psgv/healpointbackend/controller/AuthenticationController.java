package edu.psgv.healpointbackend.controller;

import edu.psgv.healpointbackend.dto.AuthenticationFormDto;
import edu.psgv.healpointbackend.dto.TokenDto;
import edu.psgv.healpointbackend.service.AuthenticationService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/api/authenticate-user")
    public ResponseEntity<String> authenticateUser(@Valid @RequestBody AuthenticationFormDto request) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @PostMapping("/api/logout-user")
    public ResponseEntity<String> logoutUser(@Valid @RequestBody TokenDto request) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
