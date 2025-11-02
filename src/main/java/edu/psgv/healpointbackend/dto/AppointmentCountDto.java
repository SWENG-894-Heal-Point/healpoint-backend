package edu.psgv.healpointbackend.dto;

import lombok.Getter;

import java.time.LocalDate;


/**
 * Data Transfer Object for appointment counts grouped by doctor and date.
 *
 * @author Mahfuzur Rahman
 */
@Getter
public class AppointmentCountDto {
    private Integer doctorId;
    private LocalDate appointmentDate;
    private Integer appointmentCount;

    /**
     * Constructs an AppointmentCountDto with the specified doctor ID, appointment date, and appointment count.
     *
     * @param doctorId         the ID of the doctor
     * @param appointmentDate  the date of the appointments
     * @param appointmentCount the count of appointments
     */
    public AppointmentCountDto(Integer doctorId, LocalDate appointmentDate, Long appointmentCount) {
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.appointmentCount = Math.toIntExact(appointmentCount);
    }
}
