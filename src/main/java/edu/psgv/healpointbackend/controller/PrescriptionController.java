package edu.psgv.healpointbackend.controller;

import edu.psgv.healpointbackend.dto.PrescriptionDto;
import edu.psgv.healpointbackend.model.Prescription;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.service.AccessManager;
import edu.psgv.healpointbackend.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * REST controller for managing patient prescriptions.
 * Provides endpoints to retrieve and upsert (create or update) prescriptions.
 *
 * @author Mahfuzur Rahman
 */
@RestController
public class PrescriptionController {
    private final PrescriptionService prescriptionService;
    private final AccessManager accessManager;

    /**
     * Constructs a new PrescriptionController with required services.
     *
     * @param prescriptionService the service for prescription operations
     * @param accessManager       the service for access control
     */
    public PrescriptionController(PrescriptionService prescriptionService, AccessManager accessManager) {
        this.prescriptionService = prescriptionService;
        this.accessManager = accessManager;
    }

    /**
     * Retrieves the prescription for a given patient.
     * <p>
     * If {@code patientId} is 0, retrieves the prescription for the requestor (patient).
     * Otherwise, enforces employee group access for the provided token.
     *
     * @param token     the authentication token
     * @param patientId the ID of the patient (0 for self)
     * @return the prescription object or an error response
     */
    @GetMapping("/api/get-patient-prescription")
    public ResponseEntity<Object> getPrescription(@Valid @RequestParam String token, @Valid @RequestParam int patientId) {
        LOGGER.info("Received request to get prescription for patientId={}", patientId);
        try {
            if (patientId == 0) {
                User requestor = accessManager.enforceOwnershipBasedAccess(token);
                patientId = requestor.getId();
            } else {
                accessManager.enforceRoleBasedAccess(accessManager.getEmployeeGroup(), token);
            }
            LOGGER.info("Access granted. Fetching prescription for patientId={}", patientId);

            Prescription prescription = prescriptionService.getPrescription(patientId);
            LOGGER.info("Prescription retrieved successfully for patientId={}", patientId);

            return ResponseEntity.ok(prescription);
        } catch (SecurityException e) {
            LOGGER.warn("Unauthorized access attempt detected for patientId={}. Reason: {}", patientId, e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error retrieving prescription for patientId={}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Creates or updates a prescription for a patient.
     * <p>
     * Only users with doctor role are authorized to perform this operation.
     *
     * @param prescriptionDto the prescription data transfer object containing prescription details and token
     * @return the updated prescription object or an error response
     */
    @PostMapping("/api/create-or-update-prescription")
    public ResponseEntity<Object> upsertPrescription(@Valid @RequestBody PrescriptionDto prescriptionDto) {
        int patientId = prescriptionDto.getPatientId();
        LOGGER.info("Received request to create/update prescription for patientId={}", patientId);
        try {
            accessManager.enforceRoleBasedAccess(accessManager.getDoctorOnlyGroup(), prescriptionDto.getToken());
            LOGGER.info("Access granted. Processing prescription creation/update for patientId={}", patientId);

            prescriptionService.upsertPrescription(prescriptionDto);
            LOGGER.info("Prescription created/updated successfully for patientId={}", patientId);

            Prescription prescription = prescriptionService.getPrescription(patientId);
            LOGGER.info("Prescription retrieved successfully for patientId={}", patientId);

            return ResponseEntity.ok(prescription);
        } catch (SecurityException e) {
            LOGGER.warn("Unauthorized attempt to create/update prescription for patientId={}. Reason: {}", patientId, e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error during create/update prescription for patientId={}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
