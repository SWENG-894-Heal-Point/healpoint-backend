package edu.psgv.healpointbackend.controller;

import edu.psgv.healpointbackend.dto.ScheduleDto;
import edu.psgv.healpointbackend.model.User;
import edu.psgv.healpointbackend.model.WorkDay;
import edu.psgv.healpointbackend.service.AccessManager;
import edu.psgv.healpointbackend.service.ScheduleManager;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * REST controller for managing doctor schedules.
 * Provides endpoints to retrieve and upsert (insert or update) doctor schedules.
 *
 * @author Mahfuzur Rahman
 */
@RestController
public class ScheduleController {
    private final ScheduleManager scheduleManager;
    private final AccessManager accessManager;

    /**
     * Constructs a new ScheduleController with required services.
     *
     * @param scheduleManager the service for schedule operations
     * @param accessManager   the service for access control
     */
    public ScheduleController(ScheduleManager scheduleManager, AccessManager accessManager) {
        this.scheduleManager = scheduleManager;
        this.accessManager = accessManager;
    }

    /**
     * Endpoint to retrieve a doctor's schedule by their ID.
     *
     * @param token    the authentication token
     * @param doctorId the ID of the doctor whose schedule is to be retrieved
     * @return ResponseEntity containing the list of WorkDay objects or an error message
     */
    @GetMapping("/api/get-doctor-schedule")
    public ResponseEntity<Object> getDoctorSchedule(@Valid @RequestParam String token, @Valid @RequestParam int doctorId) {
        LOGGER.info("Received request to get schedule for doctorId={}", doctorId);
        try {
            User requestor = accessManager.enforceRoleBasedAccess(accessManager.getSaGroup(), token);
            LOGGER.info("Role-based access granted for user: {}, role: {}", requestor.getEmail(), requestor.getRole().getDescription());

            List<WorkDay> schedule = scheduleManager.getWorkDaysByDoctorId(doctorId);
            LOGGER.info("Successfully retrieved schedule for doctorId={}", doctorId);
            return ResponseEntity.ok(schedule);
        } catch (SecurityException e) {
            LOGGER.warn("Unauthorized access attempt with token: {}", token, e);
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid data provided for retrieving schedule for doctorId={}. Reason: {}", doctorId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error retrieving schedule for doctorId={}: {}", doctorId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint to insert or update a doctor's schedule.
     *
     * @param scheduleDto DTO containing the doctor's ID, work days, and authentication token
     * @return ResponseEntity indicating success or failure of the operation
     */
    @PostMapping("/api/insert-or-update-schedule")
    public ResponseEntity<Object> upsertSchedule(@Valid @RequestBody ScheduleDto scheduleDto) {
        int doctorId = scheduleDto.getDoctorId();
        LOGGER.info("Received request to upsert schedule for doctorId={}", doctorId);
        try {
            User requestor = accessManager.enforceRoleBasedAccess(accessManager.getSaGroup(), scheduleDto.getToken());
            LOGGER.info("Role-based access granted for user: {}, role: {}", requestor.getEmail(), requestor.getRole().getDescription());

            scheduleManager.upsertWorkDays(doctorId, scheduleDto.getWorkDays());
            LOGGER.info("Successfully upserted schedule for doctorId={}", doctorId);
            return ResponseEntity.ok("Schedule upserted successfully.");
        } catch (SecurityException e) {
            LOGGER.warn("Unauthorized access attempt with token: {}", scheduleDto.getToken(), e);
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid data provided for upserting schedule for doctorId={}. Reason: {}", doctorId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error upserting schedule for doctorId={}: {}", doctorId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
