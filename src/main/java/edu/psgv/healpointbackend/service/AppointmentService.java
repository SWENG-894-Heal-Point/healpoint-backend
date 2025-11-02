package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.dto.AvailableAppointmentSlotsDto;
import edu.psgv.healpointbackend.dto.ScheduleAppointmentDto;
import edu.psgv.healpointbackend.model.Appointment;
import edu.psgv.healpointbackend.model.Doctor;
import edu.psgv.healpointbackend.model.Patient;
import edu.psgv.healpointbackend.model.Slot;
import edu.psgv.healpointbackend.repository.AppointmentRepository;
import edu.psgv.healpointbackend.repository.DoctorRepository;
import edu.psgv.healpointbackend.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * Service class for managing appointments.
 * <p>
 * Provides methods to schedule appointments for patients with doctors.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Service
public class AppointmentService {
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentAvailabilityService appointmentAvailabilityService;

    /**
     * Constructs a new AppointmentService with required repositories and services.
     *
     * @param patientRepository              the repository for patient operations
     * @param doctorRepository               the repository for doctor operations
     * @param appointmentRepository          the repository for appointment operations
     * @param appointmentAvailabilityService the service for checking appointment availability
     */
    public AppointmentService(PatientRepository patientRepository, DoctorRepository doctorRepository, AppointmentRepository appointmentRepository, AppointmentAvailabilityService appointmentAvailabilityService) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.appointmentAvailabilityService = appointmentAvailabilityService;
    }

    /**
     * Schedules a new appointment based on the provided details.
     *
     * @param dto the appointment scheduling details
     * @throws IllegalArgumentException if the patient or doctor does not exist, or if the appointment slot is unavailable
     */
    public void scheduleAppointment(ScheduleAppointmentDto dto) {
        LOGGER.info("Scheduling appointment for patient ID: {}, doctor ID: {}, date: {}, time: {}",
                dto.getPatientId(), dto.getDoctorId(), dto.getAppointmentDate(), dto.getAppointmentTime());

        Patient patient = patientRepository.findById(dto.getPatientId()).orElseThrow(() -> new IllegalArgumentException("Patient with ID " + dto.getPatientId() + " not found"));
        Doctor doctor = doctorRepository.findById(dto.getDoctorId()).orElseThrow(() -> new IllegalArgumentException("Doctor with ID " + dto.getDoctorId() + " not found"));
        Slot slot = isAppointmentAvailable(dto);

        Appointment appointment = new Appointment(doctor, patient, dto.getAppointmentDate(), slot.getStartTime(), slot.getEndTime(), dto.getReason());
        appointmentRepository.save(appointment);

        LOGGER.info("Appointment successfully scheduled: doctor ID={}, patient ID={}, date={}, time={}-{}",
                dto.getDoctorId(), dto.getPatientId(), dto.getAppointmentDate(),
                slot.getStartTime(), slot.getEndTime());
    }

    /**
     * Checks if the requested appointment slot is available for the given doctor on the specified date.
     *
     * @param dto the appointment scheduling details
     * @return the available Slot
     * @throws IllegalArgumentException if the appointment slot is unavailable
     */
    private Slot isAppointmentAvailable(ScheduleAppointmentDto dto) {
        LocalDate appointmentDate = dto.getAppointmentDate();
        if (appointmentDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Appointment date cannot be in the past.");
        }

        AvailableAppointmentSlotsDto slotsDto = appointmentAvailabilityService.createAvailableSlotsDto(appointmentDate, dto.getDoctorId());
        String exceptionMessage = String.format("Doctor with ID %d does not have any available slots on %s at %s", dto.getDoctorId(), appointmentDate, dto.getAppointmentTime());

        if (slotsDto == null || slotsDto.getAvailableSlots().isEmpty()) {
            throw new IllegalArgumentException(exceptionMessage);
        }
        return slotsDto.getAvailableSlots().stream()
                .filter(slot -> slot.getStartTime().equals(dto.getAppointmentTime())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(exceptionMessage));
    }
}
