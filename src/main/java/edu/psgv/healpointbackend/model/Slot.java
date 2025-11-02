package edu.psgv.healpointbackend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;


/**
 * Represents a time slot with a start and end time.
 *
 * @author Mahfuzur Rahman
 */
@Getter
@Setter
public class Slot {
    public Slot(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    protected Slot() {}

    @JsonFormat(pattern = "HH:mm")
    @Column(name = "StartTime", nullable = false)
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    @Column(name = "EndTime", nullable = false)
    private LocalTime endTime;
}
