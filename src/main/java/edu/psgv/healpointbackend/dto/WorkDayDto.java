package edu.psgv.healpointbackend.dto;

import edu.psgv.healpointbackend.model.WorkDay;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


public class WorkDayDto extends TokenDto {
    @Getter
    @Setter
    private List<WorkDay> workDays;
}
