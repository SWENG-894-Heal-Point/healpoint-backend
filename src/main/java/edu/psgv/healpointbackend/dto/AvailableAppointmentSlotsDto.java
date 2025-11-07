package edu.psgv.healpointbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.psgv.healpointbackend.model.Doctor;
import edu.psgv.healpointbackend.model.Slot;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;


/**
 * Data Transfer Object for available appointment slots for a doctor on a specific date.
 *
 * @author Mahfuzur Rahman
 */
@AllArgsConstructor
@Getter
public class AvailableAppointmentSlotsDto {
    @JsonProperty("doctor")
    private Doctor doctor;

    @JsonProperty("appointmentDate")
    private LocalDate appointmentDate;

    @JsonProperty("availableSlots")
    private List<Slot> availableSlots;
}
