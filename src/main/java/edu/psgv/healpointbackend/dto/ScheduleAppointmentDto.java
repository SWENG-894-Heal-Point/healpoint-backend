package edu.psgv.healpointbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;


/**
 * Data Transfer Object for scheduling an appointment.
 *
 * @author Mahfuzur Rahman
 */
@Getter
@Setter
public class ScheduleAppointmentDto extends TokenDto {
    private int doctorId;
    private int patientId;

    @NotNull
    private LocalDate appointmentDate;

    @NotNull
    private LocalTime appointmentTime;

    @NotBlank
    private String reason;
}
