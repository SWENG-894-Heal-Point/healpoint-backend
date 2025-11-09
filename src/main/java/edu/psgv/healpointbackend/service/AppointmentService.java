package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.dto.AvailableAppointmentSlotsDto;
import edu.psgv.healpointbackend.dto.ScheduleAppointmentDto;
import edu.psgv.healpointbackend.dto.UpdateAppointmentDto;
import edu.psgv.healpointbackend.model.*;
import edu.psgv.healpointbackend.repository.AppointmentRepository;
import edu.psgv.healpointbackend.repository.DoctorRepository;
import edu.psgv.healpointbackend.repository.PatientRepository;
import edu.psgv.healpointbackend.utilities.IoHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
     * Retrieves all appointments associated with the given user.
     *
     * @param user the user whose appointments are to be fetched
     * @return a list of Appointment objects
     */
    public List<Appointment> getAllAppointmentsByUser(User user) {
        int userId = user.getId();
        String role = user.getRole().getDescription();
        LOGGER.info("Fetching all appointments for user ID: {}, role: {}", userId, role);

        List<Appointment> appointments = role.equalsIgnoreCase(Roles.PATIENT)
                ? appointmentRepository.findByPatientId(userId)
                : appointmentRepository.findByDoctorId(userId);

        LOGGER.debug("Found {} appointments for user ID: {}", appointments.size(), userId);
        return appointments;
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
        Slot slot = isAppointmentAvailable(dto.getDoctorId(), dto.getAppointmentDate(), dto.getAppointmentTime());

        Appointment appointment = new Appointment(doctor, patient, dto.getAppointmentDate(), slot.getStartTime(), slot.getEndTime(), dto.getReason());
        appointmentRepository.save(appointment);

        LOGGER.info("Appointment successfully scheduled: doctor ID={}, patient ID={}, date={}, time={}-{}",
                dto.getDoctorId(), dto.getPatientId(), dto.getAppointmentDate(),
                slot.getStartTime(), slot.getEndTime());
    }

    /**
     * Updates an existing appointment based on the provided details.
     *
     * @param dto       the appointment update details
     * @param requestor the user requesting the update
     * @throws IllegalArgumentException if the appointment does not exist or if the provided status is invalid
     * @throws SecurityException        if the requestor is not authorized to update the appointment
     */
    public void updateAppointment(UpdateAppointmentDto dto, User requestor) {
        LOGGER.info("Updating appointment ID: {} by user ID: {}", dto.getAppointmentId(), requestor.getId());
        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new IllegalArgumentException("Appointment with ID " + dto.getAppointmentId() + " not found"));

        if (!appointment.getPatient().getId().equals(requestor.getId()) && !appointment.getDoctor().getId().equals(requestor.getId())) {
            throw new SecurityException("User is not authorized to update this appointment.");
        }

        if (!IoHelper.isNullOrEmpty(dto.getStatus())) {
            String status = IoHelper.validateString(dto.getStatus()).toUpperCase();
            if (!AppointmentStatus.VALID_STATUSES.contains(status)) {
                throw new IllegalArgumentException("Invalid appointment status: " + dto.getStatus());
            }

            appointment.setStatus(status);
            LOGGER.info("Appointment ID: {} status updated for {} to {}", dto.getAppointmentId(), appointment.getStatus(), status);
        } else if (dto.getAppointmentDate() != null && dto.getAppointmentTime() != null) {
            Slot slot = isAppointmentAvailable(appointment.getDoctor().getId(), dto.getAppointmentDate(), dto.getAppointmentTime());
            appointment.setAppointmentDate(dto.getAppointmentDate());
            appointment.setStartTime(slot.getStartTime());
            appointment.setEndTime(slot.getEndTime());
            LOGGER.info("Appointment ID: {} rescheduled to date: {}, time: {}-{}", dto.getAppointmentId(), dto.getAppointmentDate(), slot.getStartTime(), slot.getEndTime());
        } else {
            throw new IllegalArgumentException("Either status or appointment date & time must be provided for update.");
        }

        appointmentRepository.save(appointment);
        LOGGER.info("Appointment ID: {} updated successfully", dto.getAppointmentId());
    }

    /**
     * Checks if the requested appointment slot is available for the given doctor on the specified date.
     *
     * @param doctorId        the ID of the doctor
     * @param appointmentDate the date of the appointment
     * @param appointmentTime the time of the appointment
     * @return the available Slot
     * @throws IllegalArgumentException if the appointment slot is unavailable
     */
    private Slot isAppointmentAvailable(int doctorId, LocalDate appointmentDate, LocalTime appointmentTime) {
        if (appointmentDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Appointment date cannot be in the past.");
        }

        AvailableAppointmentSlotsDto slotsDto = appointmentAvailabilityService.createAvailableSlotsDto(appointmentDate, doctorId);
        String exceptionMessage = String.format("Doctor with ID %d does not have any available slots on %s at %s", doctorId, appointmentDate, appointmentTime);

        if (slotsDto == null || slotsDto.getAvailableSlots().isEmpty()) {
            throw new IllegalArgumentException(exceptionMessage);
        }
        return slotsDto.getAvailableSlots().stream()
                .filter(slot -> slot.getStartTime().equals(appointmentTime)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(exceptionMessage));
    }
}
