package edu.psgv.healpointbackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;


/**
 * Data Transfer Object for updating an appointment.
 * Extends TokenDto to include authentication token information.
 *
 * @author Mahfuzur Rahman
 */
@Getter
@Setter
public class UpdateAppointmentDto extends TokenDto {
    @NotNull
    private int appointmentId;

    private LocalDate appointmentDate;

    private LocalTime appointmentTime;

    private String status;
}
