package edu.psgv.healpointbackend.controller;

import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.dto.NewPasswordDto;
import edu.psgv.healpointbackend.dto.TokenDto;
import edu.psgv.healpointbackend.dto.UpdateProfileDto;
import edu.psgv.healpointbackend.service.AccessManager;
import edu.psgv.healpointbackend.service.ProfileGetService;
import edu.psgv.healpointbackend.service.ProfileUpdateService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * REST controller for handling profile-related operations for the authenticated user.
 * Provides endpoints for retrieving and updating the user's own profile and password.
 *
 * @author Mahfuzur Rahman
 */
@RestController
public class MyProfileController {
    private final ProfileUpdateService profileUpdateService;
    private final ProfileGetService profileGetService;
    private final AccessManager accessManager;

    /**
     * Constructs a MyProfileController with required services.
     *
     * @param profileUpdateService service for profile operations
     * @param profileGetService    service for profile retrieval
     * @param accessManager        service for access control
     */
    public MyProfileController(ProfileUpdateService profileUpdateService, ProfileGetService profileGetService, AccessManager accessManager) {
        this.profileUpdateService = profileUpdateService;
        this.profileGetService = profileGetService;
        this.accessManager = accessManager;
    }

    /**
     * Endpoint to retrieve the profile of the authenticated user.
     *
     * @param request DTO containing the authentication token
     * @return ResponseEntity with the user's profile or error message
     */
    @PostMapping("/api/get-my-profile")
    public ResponseEntity<Object> getUserProfile(@Valid @RequestBody TokenDto request) {
        LOGGER.info("Received request to get my profile");
        try {
            User requestor = accessManager.enforceOwnershipBasedAccess(request.getToken());
            String requestorEmail = requestor.getEmail();
            LOGGER.debug("Ownership verified. Fetching profile for email={}", requestorEmail);

            ResponseEntity<Object> response = profileGetService.getUserProfile(requestorEmail, null);
            LOGGER.info("Profile retrieval successful for email={}", requestorEmail);

            return response;
        } catch (SecurityException e) {
            LOGGER.warn("Unauthorized access attempt", e);
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error retrieving profile: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint to update the profile of the authenticated user.
     *
     * @param request DTO containing updated profile information
     * @return ResponseEntity with the updated profile or error message
     */
    @PostMapping("/api/update-my-profile")
    public ResponseEntity<Object> updateUserProfile(@Valid @RequestBody UpdateProfileDto request) {
        LOGGER.info("Received request to update profile for email={}", request.getEmail());
        try {
            User requestor = accessManager.enforceOwnershipBasedAccess(request.getToken());
            String requestorEmail = requestor.getEmail();
            LOGGER.debug("Ownership verified for email={}", requestorEmail);

            String emailAtPresent = profileUpdateService.updateUserProfile(request, requestorEmail);
            LOGGER.info("Profile updated successfully for email={}", emailAtPresent);

            return profileGetService.getUserProfile(emailAtPresent, null);
        } catch (EntityNotFoundException e) {
            LOGGER.warn("Profile update failed - entity not found for email={}", request.getEmail(), e);
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (SecurityException e) {
            LOGGER.warn("Unauthorized profile update attempt for email={}", request.getEmail(), e);
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error updating profile for email={}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint to update the password of the authenticated user.
     *
     * @param request DTO containing the new password and authentication token
     * @return ResponseEntity indicating success or failure of the operation
     */
    @PostMapping("/api/update-my-password")
    public ResponseEntity<String> updateMyPassword(@Valid @RequestBody NewPasswordDto request) {
        LOGGER.info("Received request to update password");
        try {
            User requestor = accessManager.enforceOwnershipBasedAccess(request.getToken());
            String requestorEmail = requestor.getEmail();
            LOGGER.debug("Ownership verified for email={}", requestorEmail);

            profileUpdateService.updatePassword(request);
            LOGGER.info("Password updated successfully for email={}", requestorEmail);

            return ResponseEntity.ok("Password updated successfully.");
        } catch (SecurityException e) {
            LOGGER.warn("Unauthorized password update attempt", e);
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Password update failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error updating password: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
