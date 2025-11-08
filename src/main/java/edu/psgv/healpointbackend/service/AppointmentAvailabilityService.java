package edu.psgv.healpointbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.psgv.healpointbackend.dto.AppointmentCountDto;
import edu.psgv.healpointbackend.dto.AvailableAppointmentDatesDto;
import edu.psgv.healpointbackend.dto.AvailableAppointmentSlotsDto;
import edu.psgv.healpointbackend.model.*;
import edu.psgv.healpointbackend.repository.AppointmentRepository;
import edu.psgv.healpointbackend.repository.DoctorRepository;
import edu.psgv.healpointbackend.repository.WorkDayRepository;
import edu.psgv.healpointbackend.utilities.SlotGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Service class for calculating available appointment dates for doctors.
 * <p>
 * Provides methods to retrieve available dates based on doctors' schedules and existing appointments.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Service
public class AppointmentAvailabilityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentAvailabilityService.class);
    private static final int MAX_APPOINTMENT_DAYS = 90;

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final WorkDayRepository workDayRepository;
    private final SlotGenerator slotGenerator;

    /**
     * Constructs a new AppointmentAvailabilityService with required repositories.
     *
     * @param doctorRepository      the repository for doctor operations
     * @param appointmentRepository the repository for appointment operations
     * @param workDayRepository     the repository for work day operations
     */
    public AppointmentAvailabilityService(DoctorRepository doctorRepository,
                                          AppointmentRepository appointmentRepository,
                                          WorkDayRepository workDayRepository, SlotGenerator slotGenerator) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.workDayRepository = workDayRepository;
        this.slotGenerator = slotGenerator;
    }

    /**
     * Retrieves available appointment slots for a list of doctors on a specific date.
     *
     * @param selectedDate the date for which to check availability
     * @param doctorIds    the list of doctor IDs
     * @return a list of AvailableAppointmentSlotsDto containing available slots for each doctor
     * @throws JsonProcessingException if there is an error processing JSON data
     */
    public List<AvailableAppointmentSlotsDto> getAvailableAppointmentSlots(LocalDate selectedDate, List<Integer> doctorIds) {
        LOGGER.info("Fetching available appointment slots for date: {} and doctor IDs: {}", selectedDate, doctorIds);
        List<AvailableAppointmentSlotsDto> availableSlotsList = new ArrayList<>();

        for (Integer doctorId : doctorIds) {
            LOGGER.debug("Processing doctor with ID: {}", doctorId);

            AvailableAppointmentSlotsDto slotsDto = createAvailableSlotsDto(selectedDate, doctorId);
            if (slotsDto != null) {
                LOGGER.info("Doctor {} has {} available slots on {}", doctorId, slotsDto.getAvailableSlots().size(), selectedDate);
                availableSlotsList.add(slotsDto);
            } else {
                LOGGER.info("Doctor {} has no available slots on {}", doctorId, selectedDate);
            }
        }

        LOGGER.info("Total doctors with available slots: {}", availableSlotsList.size());
        return availableSlotsList;
    }

    /**
     * Creates an AvailableAppointmentSlotsDto for a specific doctor on a given date.
     *
     * @param selectedDate the date for which to check availability
     * @param doctorId     the ID of the doctor
     * @return an AvailableAppointmentSlotsDto if slots are available, null otherwise
     * @throws JsonProcessingException if there is an error processing JSON data
     */
    public AvailableAppointmentSlotsDto createAvailableSlotsDto(LocalDate selectedDate, Integer doctorId) {
        try {
            LocalDate minDate = LocalDate.now().plusDays(1);
            if (selectedDate.isBefore(minDate)) return null;

            String selectedDayName = selectedDate.getDayOfWeek().name().substring(0, 3);
            Doctor doctor = doctorRepository.findById(doctorId).orElse(null);

            WorkDay daySchedule = workDayRepository.findByDoctorIdAndDayName(doctorId, selectedDayName).orElse(null);
            if (daySchedule == null) return null;

            List<Slot> slots = slotGenerator.generateSlots(daySchedule.getStartTime(), daySchedule.getEndTime());
            List<Appointment> bookedAppointments = appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, selectedDate).stream()
                    .filter(appointment -> !appointment.getStatus().equalsIgnoreCase(AppointmentStatus.CANCELED))
                    .toList();

            for (Appointment appointment : bookedAppointments) {
                slots.removeIf(slot -> slot.getStartTime().equals(appointment.getStartTime()) && slot.getEndTime().equals(appointment.getEndTime()));
            }

            return slots.isEmpty() ? null : new AvailableAppointmentSlotsDto(doctor, selectedDate, slots);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error processing JSON for available appointment slots.", e);
            return null;
        }
    }

    /**
     * Retrieves available appointment dates for all doctors.
     * <p>
     * Considers each doctor's work schedule and existing appointments to determine availability.
     * </p>
     *
     * @return a list of AvailableAppointmentDatesDto containing available dates for each doctor
     */
    public List<AvailableAppointmentDatesDto> getAvailableAppointmentDates() {
        LOGGER.info("Starting available appointment date calculation...");

        LocalDate minDate = LocalDate.now().plusDays(1);
        List<AppointmentCountDto> appointmentCounts = appointmentRepository.getAppointmentCounts();
        List<Doctor> doctors = doctorRepository.findAll();
        List<AvailableAppointmentDatesDto> availableDatesList = new ArrayList<>();

        Map<Integer, Map<LocalDate, Integer>> appointmentMap = appointmentCounts.stream()
                .collect(Collectors.groupingBy(
                        AppointmentCountDto::getDoctorId,
                        Collectors.toMap(AppointmentCountDto::getAppointmentDate, AppointmentCountDto::getAppointmentCount)
                ));

        for (Doctor doctor : doctors) {
            List<WorkDay> schedule = workDayRepository.findByDoctorId(doctor.getId());
            if (schedule.isEmpty()) {
                LOGGER.warn("Doctor {} has no work schedule defined.", doctor.getId());
                continue;
            }

            availableDatesList.add(new AvailableAppointmentDatesDto(doctor,
                    calculateAvailableDatesForDoctor(doctor, minDate, convertScheduleToMap(schedule), appointmentMap)));
        }

        LOGGER.info("Completed availability calculation for {} doctors.", doctors.size());
        return availableDatesList;
    }

    /**
     * Calculates available appointment dates for a specific doctor.
     *
     * @param doctor         the doctor for whom to calculate available dates
     * @param minDate        the current date
     * @param scheduleMap    a map of work days to slot counts
     * @param appointmentMap a map of existing appointments grouped by doctor and date
     * @return a list of available LocalDate objects for the doctor
     */
    private List<LocalDate> calculateAvailableDatesForDoctor(Doctor doctor, LocalDate minDate, Map<String, Integer> scheduleMap,
                                                             Map<Integer, Map<LocalDate, Integer>> appointmentMap) {
        final Map<LocalDate, Integer> bookedByDate = appointmentMap.getOrDefault(doctor.getId(), Collections.emptyMap());

        return IntStream.range(0, MAX_APPOINTMENT_DAYS)
                .mapToObj(minDate::plusDays)
                .filter(date -> {
                    String dayKey = date.getDayOfWeek().name().substring(0, 3);
                    int slots = scheduleMap.getOrDefault(dayKey, 0);
                    return slots > 0 && bookedByDate.getOrDefault(date, 0) < slots;
                }).toList();
    }

    /**
     * Converts a list of WorkDay objects to a map of day names to slot counts.
     *
     * @param schedule the list of WorkDay objects
     * @return a map with day names as keys and slot counts as values
     */
    private Map<String, Integer> convertScheduleToMap(List<WorkDay> schedule) {
        return schedule.stream().collect(Collectors.toMap(WorkDay::getDayName, WorkDay::getSlotCount));
    }
}
