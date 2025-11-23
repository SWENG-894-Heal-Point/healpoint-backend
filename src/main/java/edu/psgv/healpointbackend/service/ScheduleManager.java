package edu.psgv.healpointbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.psgv.healpointbackend.model.Doctor;
import edu.psgv.healpointbackend.model.Slot;
import edu.psgv.healpointbackend.model.WorkDay;
import edu.psgv.healpointbackend.repository.DoctorRepository;
import edu.psgv.healpointbackend.repository.WorkDayRepository;
import edu.psgv.healpointbackend.utilities.SlotGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * Service class for managing doctors' work schedules.
 * <p>
 * Provides methods to upsert work days and validate schedule data.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Service
public class ScheduleManager {
    private final WorkDayRepository workDayRepository;
    private final DoctorRepository doctorRepository;
    private final SlotGenerator slotGenerator;

    /**
     * Constructs a new ScheduleManager with required repositories and utilities.
     *
     * @param workDayRepository the repository for work day operations
     * @param doctorRepository  the repository for doctor operations
     * @param slotGenerator     the utility for generating time slots
     */
    public ScheduleManager(WorkDayRepository workDayRepository, DoctorRepository doctorRepository, SlotGenerator slotGenerator) {
        this.doctorRepository = doctorRepository;
        this.workDayRepository = workDayRepository;
        this.slotGenerator = slotGenerator;
    }

    /**
     * Retrieves the work days for a given doctor by their ID.
     *
     * @param doctorId the ID of the doctor
     * @return a list of WorkDay objects associated with the doctor
     */
    public List<WorkDay> getWorkDaysByDoctorId(Integer doctorId) {
        LOGGER.info("Fetching work days for doctorId: {}", doctorId);
        return workDayRepository.findByDoctorId(doctorId);
    }

    /**
     * Upserts the work days for a given doctor.
     * <p>
     * Validates the provided work days and either updates existing entries or creates new ones.
     * </p>
     *
     * @param doctorId the ID of the doctor
     * @param workDays the list of work days to upsert
     * @throws IllegalArgumentException if the doctor ID is invalid or no valid work days are provided
     * @throws JsonProcessingException  if there is an error processing JSON data
     */
    public void upsertWorkDays(Integer doctorId, List<WorkDay> workDays) throws JsonProcessingException {
        LOGGER.info("Starting upsertWorkDays for doctorId: {}", doctorId);
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) {
            LOGGER.error("Doctor with ID {} does not exist.", doctorId);
            throw new IllegalArgumentException("Invalid doctor ID: " + doctorId);
        }

        List<WorkDay> validWorkDays = getValidWorkDays(doctor, workDays);

        List<WorkDay> existingDays = workDayRepository.findByDoctorId(doctorId);
        Set<String> incoming = validWorkDays.stream().map(WorkDay::getDayName).collect(Collectors.toSet());
        existingDays.stream().filter(ed -> !incoming.contains(ed.getDayName())).forEach(workDayRepository::delete);

        for (WorkDay wd : validWorkDays) {
            LOGGER.debug("Processing work day: {}", wd.getDayName());
            WorkDay existingWd = workDayRepository.findByDoctorIdAndDayName(doctorId, wd.getDayName()).orElse(null);

            if (existingWd != null) {
                LOGGER.info("Updating existing work day '{}' for doctor ID {}. Old times: {}-{}, New times: {}-{}",
                        wd.getDayName(), doctorId, existingWd.getStartTime(), existingWd.getEndTime(), wd.getStartTime(), wd.getEndTime());

                existingWd.setStartTime(wd.getStartTime());
                existingWd.setEndTime(wd.getEndTime());
                workDayRepository.save(existingWd);
            } else {
                LOGGER.info("Inserting new work day '{}' for doctor ID {} with times {}-{}",
                        wd.getDayName(), doctorId, wd.getStartTime(), wd.getEndTime());
                workDayRepository.save(wd);
            }
        }
    }

    /**
     * Validates and filters the provided work days.
     *
     * @param doctor   the doctor associated with the work days
     * @param workDays the list of work days to validate
     * @return a list of valid work days
     * @throws JsonProcessingException if there is an error processing JSON data
     */
    private List<WorkDay> getValidWorkDays(Doctor doctor, List<WorkDay> workDays) throws JsonProcessingException {
        List<WorkDay> validWorkDays = new ArrayList<>();

        for (WorkDay wd : workDays) {
            String validDayName = getValidDayName(wd.getDayName());
            List<Slot> slots = slotGenerator.generateSlots(wd.getStartTime(), wd.getEndTime());

            if (validDayName != null && wd.getStartTime() != null && wd.getEndTime() != null) {
                WorkDay validWd = WorkDay.builder()
                        .doctor(doctor)
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

    /**
     * Validates the provided day name.
     *
     * @param dayName the day name to validate
     * @return the valid day name in uppercase (first three letters) or null if invalid
     */
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