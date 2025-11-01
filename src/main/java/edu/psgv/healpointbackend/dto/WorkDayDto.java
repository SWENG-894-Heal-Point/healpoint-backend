package edu.psgv.healpointbackend.dto;

import edu.psgv.healpointbackend.model.WorkDay;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


/**
 * DTO for transferring work day data along with an authentication token.
 *
 * @author Mahfuzur Rahman
 */
public class WorkDayDto extends TokenDto {
    @Getter
    @Setter
    private List<WorkDay> workDays;
}
