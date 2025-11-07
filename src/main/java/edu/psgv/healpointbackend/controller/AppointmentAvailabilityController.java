package edu.psgv.healpointbackend.controller;

import edu.psgv.healpointbackend.dto.AvailableAppointmentDatesDto;
import edu.psgv.healpointbackend.dto.AvailableAppointmentSlotsDto;
import edu.psgv.healpointbackend.service.AccessManager;
import edu.psgv.healpointbackend.service.AppointmentAvailabilityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * REST controller for fetching available appointment dates.
 *
 * @author Mahfuzur Rahman
 */
@RestController
public class AppointmentAvailabilityController {
    private final AppointmentAvailabilityService appointmentAvailabilityService;
    private final AccessManager accessManager;

    /**
     * Constructs a new AppointmentAvailabilityController with required services.
     *
     * @param appointmentAvailabilityService the service for appointment availability operations
     * @param accessManager                  the service for access control
     */
    public AppointmentAvailabilityController(AppointmentAvailabilityService appointmentAvailabilityService, AccessManager accessManager) {
        this.appointmentAvailabilityService = appointmentAvailabilityService;
        this.accessManager = accessManager;
    }

    /**
     * Endpoint to get available appointment dates for all doctors.
     *
     * @param token the authentication token
     * @return a ResponseEntity containing the available appointment dates or an error message
     */
    @GetMapping("/api/available-appointment-dates")
    public ResponseEntity<Object> getAvailableAppointmentDates(@Valid @RequestParam String token) {
        try {
            accessManager.enforceOwnershipBasedAccess(token);
            List<AvailableAppointmentDatesDto> availableDates = appointmentAvailabilityService.getAvailableAppointmentDates();
            return ResponseEntity.ok(availableDates);
        } catch (SecurityException e) {
            LOGGER.error("Unauthorized access attempt with token: {}", token, e);
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error fetching available appointment dates.", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint to get available appointment slots for given doctors on a specific date.
     *
     * @param token     the authentication token
     * @param date      the date for which to check available slots
     * @param doctorIds the list of doctor IDs to check availability for
     * @return a ResponseEntity containing the available appointment slots or an error message
     */
    @GetMapping("/api/available-appointment-slots")
    public ResponseEntity<Object> getAvailableAppointmentSlots(@Valid @RequestParam String token,
                                                               @Valid @RequestParam LocalDate date,
                                                               @Valid @RequestParam List<Integer> doctorIds) {
        try {
            accessManager.enforceOwnershipBasedAccess(token);
            List<AvailableAppointmentSlotsDto> availableSlots = appointmentAvailabilityService.getAvailableAppointmentSlots(date, doctorIds);
            return ResponseEntity.ok(availableSlots);
        } catch (SecurityException e) {
            LOGGER.error("Unauthorized access attempt with token: {}", token, e);
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error fetching available appointment slots.", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
