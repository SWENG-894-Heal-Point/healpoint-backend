package edu.psgv.healpointbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.psgv.healpointbackend.model.Doctor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;


/**
 * Data Transfer Object for available appointment dates for a doctor.
 *
 * @author Mahfuzur Rahman
 */
@AllArgsConstructor
public class AvailableAppointmentDatesDto {
    @JsonProperty("doctor")
    private Doctor doctor;

    @JsonProperty("availableDates")
    private List<LocalDate> availableDates;
}
