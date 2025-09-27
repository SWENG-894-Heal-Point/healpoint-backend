package edu.psgv.healpointbackend.controller;

import edu.psgv.healpointbackend.dto.TokenDto;
import edu.psgv.healpointbackend.dto.UpdateProfileDto;
import edu.psgv.healpointbackend.dto.UserLookupDto;
import edu.psgv.healpointbackend.service.AccessManager;
import edu.psgv.healpointbackend.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ProfileController {
    private final ProfileService profileService;
    private final AccessManager accessManager;

    public ProfileController(ProfileService profileService, AccessManager accessManager) {
        this.profileService = profileService;
        this.accessManager = accessManager;
    }

    @PostMapping("/api/get-my-profile")
    public ResponseEntity<Object> getUserProfile(@Valid @RequestBody TokenDto request) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @PostMapping("/api/update-my-profile")
    public ResponseEntity<Object> updateUserProfile(@Valid @RequestBody UpdateProfileDto request) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @PostMapping("/api/get-doctor-profile")
    public ResponseEntity<Object> getDoctorProfile(@Valid @RequestBody UserLookupDto request) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @PostMapping("/api/get-patient-profile")
    public ResponseEntity<Object> getPatientProfile(@Valid @RequestBody UserLookupDto request) {
        throw new UnsupportedOperationException("Method not implemented");
    }
}
