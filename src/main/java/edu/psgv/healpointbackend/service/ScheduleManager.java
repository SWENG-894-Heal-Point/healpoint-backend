package edu.psgv.healpointbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.psgv.healpointbackend.model.Slot;
import edu.psgv.healpointbackend.model.WorkDay;
import edu.psgv.healpointbackend.repository.DoctorRepository;
import edu.psgv.healpointbackend.repository.WorkDayRepository;
import edu.psgv.healpointbackend.utilities.SlotGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class ScheduleManager {
    private final WorkDayRepository workDayRepository;
    private final DoctorRepository doctorRepository;
    private final SlotGenerator slotGenerator;

    public ScheduleManager(WorkDayRepository workDayRepository, DoctorRepository doctorRepository, SlotGenerator slotGenerator) {
        this.doctorRepository = doctorRepository;
        this.workDayRepository = workDayRepository;
        this.slotGenerator = slotGenerator;
    }

    public void upsertWorkDays(Integer doctorId, List<WorkDay> workDays) throws JsonProcessingException {
        if (!doctorRepository.existsById(doctorId)) {
            throw new IllegalArgumentException("Invalid doctor ID: " + doctorId);
        }

        List<WorkDay> validWorkDays = getValidWorkDays(doctorId, workDays);
        if (validWorkDays.isEmpty()) {
            throw new IllegalArgumentException("No valid work days provided.");
        }

        for (WorkDay wd : validWorkDays) {
            WorkDay existingWd = workDayRepository.findByDoctorIdAndDayName(doctorId, wd.getDayName()).orElse(null);

            if (existingWd != null) {
                existingWd.setStartTime(wd.getStartTime());
                existingWd.setEndTime(wd.getEndTime());
                workDayRepository.save(existingWd);
            } else {
                workDayRepository.save(wd);
            }
        }
    }

    private List<WorkDay> getValidWorkDays(Integer doctorId, List<WorkDay> workDays) throws JsonProcessingException {
        List<WorkDay> validWorkDays = new ArrayList<>();

        for (WorkDay wd : workDays) {
            String validDayName = getValidDayName(wd.getDayName());
            List<Slot> slots = slotGenerator.generateSlots(wd.getStartTime(), wd.getEndTime());

            if (validDayName != null && wd.getStartTime() != null && wd.getEndTime() != null) {
                WorkDay validWd = WorkDay.builder()
                        .doctorId(doctorId)
                        .dayName(validDayName)
                        .startTime(wd.getStartTime())
                        .endTime(wd.getEndTime())
                        .slotCount(slots.size())
                        .build();
                validWorkDays.add(validWd);
            }
        }

        return validWorkDays;
    }

    private String getValidDayName(String dayName) {
        if (dayName == null) return null;
        String s = dayName.trim().toUpperCase();
        if (s.isEmpty()) return null;
        String key = s.length() >= 3 ? s.substring(0, 3) : s;

        List<String> validKeys = List.of("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT");
        if (validKeys.contains(key)) {
            return key;
        } else {
            return null;
        }
    }
}