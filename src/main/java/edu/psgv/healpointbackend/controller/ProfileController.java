package edu.psgv.healpointbackend.controller;

import edu.psgv.healpointbackend.dto.TokenDto;
import edu.psgv.healpointbackend.dto.UpdateProfileDto;
import edu.psgv.healpointbackend.dto.UserLookupDto;
import edu.psgv.healpointbackend.model.Roles;
import edu.psgv.healpointbackend.service.AccessManager;
import edu.psgv.healpointbackend.service.ProfileService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * REST controller for handling profile-related operations.
 * Provides endpoints for retrieving and updating user, doctor, and patient profiles.
 *
 * @author Mahfuzur Rahman
 */
@RestController
public class ProfileController {
    private final ProfileService profileService;
    private final AccessManager accessManager;

    /**
     * Constructs a ProfileController with required services.
     *
     * @param profileService service for profile operations
     * @param accessManager  service for access control
     */
    public ProfileController(ProfileService profileService, AccessManager accessManager) {
        this.profileService = profileService;
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
            String requestorEmail = accessManager.enforceOwnershipBasedAccess(request.getToken());
            LOGGER.debug("Ownership verified. Fetching profile for email={}", requestorEmail);

            ResponseEntity<Object> response = profileService.getUserProfile(requestorEmail, null);
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
            String requestorEmail = accessManager.enforceOwnershipBasedAccess(request.getToken());
            LOGGER.debug("Ownership verified for email={}", requestorEmail);

            String emailAtPresent = profileService.updateUserProfile(request);
            LOGGER.info("Profile updated successfully for email={}", emailAtPresent);

            return profileService.getUserProfile(emailAtPresent, null);
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
     * Endpoint to retrieve a doctor's profile by email.
     *
     * @param request DTO containing the doctor's email
     * @return ResponseEntity with the doctor's profile or error message
     */
    @PostMapping("/api/get-doctor-profile")
    public ResponseEntity<Object> getDoctorProfile(@Valid @RequestBody UserLookupDto request) {
        LOGGER.info("Received request to get DOCTOR profile for email={}", request.getEmail());
        try {
            ResponseEntity<Object> response = profileService.getUserProfile(request.getEmail(), Roles.DOCTOR);
            LOGGER.info("Doctor profile retrieved successfully for email={}", request.getEmail());

            return response;
        } catch (SecurityException e) {
            LOGGER.warn("Unauthorized attempt to get doctor profile for email={}", request.getEmail(), e);
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error retrieving doctor profile for email={}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint to retrieve a patient's profile by email.
     * Access is restricted to users with employee roles.
     *
     * @param request DTO containing the patient's email and authentication token
     * @return ResponseEntity with the patient's profile or error message
     */
    @PostMapping("/api/get-patient-profile")
    public ResponseEntity<Object> getPatientProfile(@Valid @RequestBody UserLookupDto request) {
        LOGGER.info("Received request to get PATIENT profile for email={}", request.getEmail());
        try {
            accessManager.enforceRoleBasedAccess(accessManager.getEmployeeGroup(), request.getToken());
            LOGGER.debug("Role-based access check passed for email={}", request.getEmail());

            ResponseEntity<Object> response = profileService.getUserProfile(request.getEmail(), Roles.PATIENT);
            LOGGER.info("Patient profile retrieved successfully for email={}", request.getEmail());

            return response;
        } catch (SecurityException e) {
            LOGGER.warn("Unauthorized attempt to get patient profile for email={}", request.getEmail(), e);
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error retrieving patient profile for email={}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
