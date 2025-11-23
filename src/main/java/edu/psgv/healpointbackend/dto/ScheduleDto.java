package edu.psgv.healpointbackend.dto;

import edu.psgv.healpointbackend.model.WorkDay;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


/**
 * Data Transfer Object for scheduling a doctor's work days.
 * <p>
 * Contains fields for doctor ID and a list of work days.
 * Validation annotations ensure that required fields are provided.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Getter
@Setter
public class ScheduleDto extends TokenDto {
    @NotNull
    private int doctorId;

    private List<WorkDay> workDays;
}
