package edu.psgv.healpointbackend.controller;

import edu.psgv.healpointbackend.dto.UserLookupDto;
import edu.psgv.healpointbackend.model.PatientProfile;
import edu.psgv.healpointbackend.model.Roles;
import edu.psgv.healpointbackend.service.AccessManager;
import edu.psgv.healpointbackend.service.ProfileGetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * REST controller for handling profile-related operations.
 * Provides endpoints for retrieving doctor, and patient profiles.
 *
 * @author Mahfuzur Rahman
 */
@RestController
public class UserLookupController {
    private final ProfileGetService profileGetService;
    private final AccessManager accessManager;

    /**
     * Constructs a UserLookupController with required services.
     *
     * @param profileGetService service for profile retrieval
     * @param accessManager     service for access control
     */
    public UserLookupController(ProfileGetService profileGetService, AccessManager accessManager) {
        this.profileGetService = profileGetService;
        this.accessManager = accessManager;
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
            ResponseEntity<Object> response = profileGetService.getUserProfile(request.getEmail(), Roles.DOCTOR);
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

            ResponseEntity<Object> response = profileGetService.getUserProfile(request.getEmail(), Roles.PATIENT);
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

    /**
     * Endpoint to retrieve all patient profiles.
     * Access is restricted to users with employee roles.
     *
     * @param token authentication token of the requester
     * @return ResponseEntity with the list of all patient profiles or error message
     */
    @GetMapping("/api/get-all-patients")
    public ResponseEntity<Object> getAllPatients(@RequestParam String token) {
        LOGGER.info("Received request to get all patients");
        try {
            accessManager.enforceRoleBasedAccess(accessManager.getEmployeeGroup(), token);
            LOGGER.debug("Role-based access check passed for getting all patients");

            ArrayList<PatientProfile> profiles = profileGetService.getAllPatients();
            LOGGER.info("All patients retrieved successfully");
            return ResponseEntity.ok(profiles);
        } catch (SecurityException e) {
            LOGGER.warn("Unauthorized attempt to get patient list");
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error retrieving all patients: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
