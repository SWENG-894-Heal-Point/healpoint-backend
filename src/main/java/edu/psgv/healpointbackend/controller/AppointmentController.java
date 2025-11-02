package edu.psgv.healpointbackend.controller;

import edu.psgv.healpointbackend.dto.ScheduleAppointmentDto;
import edu.psgv.healpointbackend.model.Roles;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.service.AccessManager;
import edu.psgv.healpointbackend.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * REST controller for managing appointment scheduling.
 *
 * @author Mahfuzur Rahman
 */
@RestController
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final AccessManager accessManager;

    /**
     * Constructs a new AppointmentController with required services.
     *
     * @param appointmentService the service for appointment operations
     * @param accessManager      the service for access control
     */
    public AppointmentController(AppointmentService appointmentService, AccessManager accessManager) {
        this.appointmentService = appointmentService;
        this.accessManager = accessManager;
    }

    /**
     * Schedules a new appointment based on the provided details.
     *
     * @param dto the appointment scheduling details
     * @return ResponseEntity indicating success or failure of the operation
     */
    @PostMapping("/api/schedule-appointment")
    public ResponseEntity<Object> scheduleAppointment(@Valid @RequestBody ScheduleAppointmentDto dto) {
        try {
            User requestor = accessManager.enforceOwnershipBasedAccess(dto.getToken());
            String role = requestor.getRole().getDescription().toUpperCase();

            if (role.equals(Roles.PATIENT)) {
                dto.setPatientId(requestor.getId());
            } else if (role.equals(Roles.DOCTOR)) {
                dto.setDoctorId(requestor.getId());
            }

            appointmentService.scheduleAppointment(dto);
            LOGGER.info("Appointment scheduled successfully");
            return ResponseEntity.ok("Appointment scheduled successfully.");
        } catch (SecurityException e) {
            LOGGER.warn("Unauthorized access attempt with token: {}", dto.getToken(), e);
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.error("Error scheduling appointment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error scheduling appointment: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("An unexpected error occurred.");
        }
    }
}
