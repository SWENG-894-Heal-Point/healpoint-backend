package edu.psgv.healpointbackend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


/**
 * Represents an appointment in the system.
 * Maps to the "Appointment" table in the "dbo" schema.
 *
 * @author Mahfuzur Rahman
 */
@Getter
@Setter
@Entity
@Table(name = "Appointment", schema = "dbo")
public class Appointment {
    // Required by JPA
    protected Appointment() {
        super();
    }

    // Custom constructors
    public Appointment(Doctor doctor, Patient patient, LocalDate appointmentDate, LocalTime startTime, LocalTime endTime, String reason) {
        this.doctor = doctor;
        this.patient = patient;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reason = reason;
        this.status = AppointmentStatus.SCHEDULED;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AppointmentID", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "DoctorID", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PatientID", nullable = false)
    private Patient patient;

    @Column(name = "AppointmentDate", nullable = false)
    private LocalDate appointmentDate;

    @JsonFormat(pattern = "HH:mm")
    @Column(name = "StartTime", nullable = false)
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    @Column(name = "EndTime", nullable = false)
    private LocalTime endTime;

    @Column(name = "Reason", nullable = false, length = 50)
    private String reason;

    @Column(name = "Status", nullable = false, length = 20)
    private String status;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
