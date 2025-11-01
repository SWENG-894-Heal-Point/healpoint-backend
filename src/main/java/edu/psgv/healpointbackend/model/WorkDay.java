package edu.psgv.healpointbackend.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;


/**
 * Represents a work day for a doctor in the system.
 * Maps to the "WorkDay" table in the "dbo" schema.
 *
 * @author Mahfuzur Rahman
 */
@Getter
@Setter
@Entity
@Table(name = "WorkDay", schema = "dbo")
public class WorkDay {

    // Required by JPA
    protected WorkDay() {
    }

    // Custom constructors
    @Builder
    public WorkDay(Integer id, Integer doctorId, String dayName, LocalTime startTime, LocalTime endTime, Integer slotCount) {
        this.id = id;
        this.doctorId = doctorId;
        this.dayName = dayName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotCount = slotCount;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WorkDayID")
    private Integer id;

    @Column(name = "DoctorID", nullable = false)
    private Integer doctorId;

    @Column(name = "DayName", nullable = false, length = 20)
    private String dayName;

    @Column(name = "StartTime", nullable = false)
    private LocalTime startTime;

    @Column(name = "EndTime", nullable = false)
    private LocalTime endTime;

    @Column(name = "SlotCount", nullable = false)
    private Integer slotCount;
}
