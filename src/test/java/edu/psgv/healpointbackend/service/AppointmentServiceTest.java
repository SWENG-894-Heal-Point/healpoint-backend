package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.AbstractTestBase;
import edu.psgv.healpointbackend.dto.AvailableAppointmentSlotsDto;
import edu.psgv.healpointbackend.dto.ScheduleAppointmentDto;
import edu.psgv.healpointbackend.dto.UpdateAppointmentDto;
import edu.psgv.healpointbackend.model.*;
import edu.psgv.healpointbackend.repository.AppointmentRepository;
import edu.psgv.healpointbackend.repository.DoctorRepository;
import edu.psgv.healpointbackend.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentServiceTest extends AbstractTestBase {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private AppointmentAvailabilityService appointmentAvailabilityService;

    @InjectMocks
    private AppointmentService appointmentService;

    private ScheduleAppointmentDto dto;
    private Doctor doctor;
    private Patient patient;
    private LocalDate date;
    private Slot slot;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(9, 0);
        slot = new Slot(startTime, startTime.plusMinutes(30));

        doctor = mockDoctor(1, "Doctor", "Smith");
        patient = mockPatient(2, "Patient", "Johnson");

        dto = new ScheduleAppointmentDto();
        dto.setDoctorId(1);
        dto.setPatientId(2);
        dto.setAppointmentDate(date);
        dto.setAppointmentTime(startTime);
        dto.setReason("Consultation");
    }

    @Test
    void getAllAppointmentsByUser_nonEmptyRepository_returnsExpectedAppointments() {
        User mockPatientUser = mockUser("patient@example.com", Roles.PATIENT, 2);

        Appointment a1 = mockAppointment(doctor, patient, "2025-07-05", "09:00", AppointmentStatus.COMPLETED);
        Appointment a2 = mockAppointment(doctor, patient, "2025-08-12", "11:30", AppointmentStatus.COMPLETED);
        Appointment a3 = mockAppointment(doctor, patient, "2025-12-22", "14:30", AppointmentStatus.SCHEDULED);

        when(appointmentRepository.findByPatientId(2)).thenReturn(List.of(a1, a2, a3));

        List<Appointment> appointments = appointmentService.getAllAppointmentsByUser(mockPatientUser);

        assertEquals(3, appointments.size());
        assertTrue(appointments.contains(a1));
        assertTrue(appointments.contains(a2));
        assertTrue(appointments.contains(a3));
        verify(appointmentRepository).findByPatientId(2);
    }

    @Test
    void getAllAppointmentsByUser_emptyRepository_returnsEmptyList() {
        User mockDoctorUser = mockUser("doctor@example.com", Roles.DOCTOR, 37);

        when(appointmentRepository.findByDoctorId(37)).thenReturn(new ArrayList<>());

        List<Appointment> appointments = assertDoesNotThrow(() -> appointmentService.getAllAppointmentsByUser(mockDoctorUser));

        assertTrue(appointments.isEmpty());
        verify(appointmentRepository).findByDoctorId(37);
    }

    @Test
    void scheduleAppointment_validInput_appointmentSaved() {
        // Arrange
        when(patientRepository.findById(2)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1)).thenReturn(Optional.of(doctor));

        AvailableAppointmentSlotsDto availableSlots = new AvailableAppointmentSlotsDto(doctor, date, List.of(slot));
        when(appointmentAvailabilityService.createAvailableSlotsDto(date, 1)).thenReturn(availableSlots);

        // Act
        appointmentService.scheduleAppointment(dto);

        // Assert
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void scheduleAppointment_missingPatientOrDoctor_throwsException() {
        // Arrange
        when(patientRepository.findById(2)).thenReturn(Optional.empty());
        when(doctorRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.scheduleAppointment(dto));
        assertTrue(ex1.getMessage().contains("Patient with ID 2 not found"));

        when(patientRepository.findById(2)).thenReturn(Optional.of(patient));
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.scheduleAppointment(dto));
    }

    @Test
    void scheduleAppointment_invalidSlot_throwsException() {
        when(patientRepository.findById(2)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1)).thenReturn(Optional.of(doctor));
        when(doctorRepository.findById(11)).thenReturn(Optional.of(doctor));
        when(appointmentAvailabilityService.createAvailableSlotsDto(date, 1))
                .thenReturn(new AvailableAppointmentSlotsDto(doctor, date, new ArrayList<>()));

        AvailableAppointmentSlotsDto slotsDto = new AvailableAppointmentSlotsDto(doctor, date,
                List.of(new Slot(LocalTime.of(10, 0), LocalTime.of(10, 30))));
        when(appointmentAvailabilityService.createAvailableSlotsDto(date, 11)).thenReturn(slotsDto);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.scheduleAppointment(dto));
        assertTrue(ex.getMessage().contains("does not have any available slots"));

        dto.setDoctorId(11);
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.scheduleAppointment(dto));
        assertTrue(ex2.getMessage().contains("does not have any available slots"));
    }

    @Test
    void updateAppointment_validRequest_appointmentUpdated() {
        LocalDate newDate = LocalDate.of(2025, 12, 20);
        LocalTime newTime = LocalTime.of(9, 0);

        AvailableAppointmentSlotsDto slotsDto = new AvailableAppointmentSlotsDto(doctor, newDate, List.of(new Slot(newTime, newTime.plusMinutes(30))));
        when(appointmentAvailabilityService.createAvailableSlotsDto(newDate, doctor.getId())).thenReturn(slotsDto);

        Appointment existingAppointment = mockAppointment(doctor, patient, "2025-12-16", "14:30", AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById(1)).thenReturn(Optional.of(existingAppointment));

        User requestor = mockUser("patient@example.com", Roles.PATIENT, 2);
        UpdateAppointmentDto updateDto1 = mockUpdateAppointmentDto(1, newDate, newTime, null);
        appointmentService.updateAppointment(updateDto1, requestor);
        verifyAppointmentUpdate(existingAppointment, newDate, newTime, AppointmentStatus.SCHEDULED, 1);

        UpdateAppointmentDto updateDto2 = mockUpdateAppointmentDto(1, null, null, AppointmentStatus.CANCELED);
        appointmentService.updateAppointment(updateDto2, requestor);
        verifyAppointmentUpdate(existingAppointment, newDate, newTime, AppointmentStatus.CANCELED, 2);
    }

    @Test
    void updateAppointment_selectPastDate_throwsException() {
        LocalDate newDate = LocalDate.now().minusDays(5);
        LocalTime newTime = LocalTime.of(9, 0);
        UpdateAppointmentDto updateDto = mockUpdateAppointmentDto(3, newDate, newTime, null);

        User requestor = mockUser("patient@example.com", Roles.PATIENT, 2);

        Appointment existingAppointment = mockAppointment(doctor, patient, "2025-12-16", "14:30", AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById(3)).thenReturn(Optional.of(existingAppointment));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.updateAppointment(updateDto, requestor));
        assertTrue(ex.getMessage().contains("Appointment date cannot be in the past."));
    }

    @Test
    void updateAppointment_invalidRequest_throwsException() {
        User requestor = mockUser("patient@example.com", Roles.PATIENT, 2);

        // Appointment not found
        UpdateAppointmentDto updateDto1 = mockUpdateAppointmentDto(99, null, null, AppointmentStatus.CANCELED);
        when(appointmentRepository.findById(99)).thenReturn(Optional.empty());
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.updateAppointment(updateDto1, requestor));
        assertTrue(ex1.getMessage().contains("Appointment with ID 99 not found"));

        // Unauthorized user
        Appointment existingAppointment = mockAppointment(doctor, patient, "2025-12-16", "14:30", AppointmentStatus.SCHEDULED);
        when(appointmentRepository.findById(2)).thenReturn(Optional.of(existingAppointment));
        UpdateAppointmentDto updateDto2 = mockUpdateAppointmentDto(2, null, null, AppointmentStatus.CANCELED);
        User unauthorizedUser = mockUser("patient@example.com", Roles.PATIENT, 99);
        SecurityException ex2 = assertThrows(SecurityException.class,
                () -> appointmentService.updateAppointment(updateDto2, unauthorizedUser));
        assertTrue(ex2.getMessage().contains("User is not authorized to update this appointment."));

        // Invalid status
        UpdateAppointmentDto updateDto3 = mockUpdateAppointmentDto(2, null, null, "INVALID_STATUS");
        when(appointmentRepository.findById(2)).thenReturn(Optional.of(existingAppointment));
        IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.updateAppointment(updateDto3, requestor));
        assertTrue(ex3.getMessage().contains("Invalid appointment status: INVALID_STATUS"));

        // Incomplete update info
        UpdateAppointmentDto updateDto4 = mockUpdateAppointmentDto(2, null, null, null);
        when(appointmentRepository.findById(2)).thenReturn(Optional.of(existingAppointment));
        IllegalArgumentException ex4 = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.updateAppointment(updateDto4, requestor));
        assertTrue(ex4.getMessage().contains("Either status or appointment date & time must be provided for update."));
    }

    UpdateAppointmentDto mockUpdateAppointmentDto(int id, LocalDate newDate, LocalTime newTime, String status) {
        UpdateAppointmentDto updateDto = new UpdateAppointmentDto();
        updateDto.setAppointmentId(id);
        updateDto.setAppointmentDate(newDate);
        updateDto.setAppointmentTime(newTime);
        updateDto.setStatus(status);
        return updateDto;
    }

    void verifyAppointmentUpdate(Appointment appointment, LocalDate newDate, LocalTime newTime, String status, int callCount) {

        verify(appointmentRepository, times(callCount)).save(appointment);
        Optional<Appointment> updatedAppointmentOpt = appointmentRepository.findById(1);
        assertTrue(updatedAppointmentOpt.isPresent());

        Appointment updatedAppointment = updatedAppointmentOpt.get();
        assertEquals(newDate, updatedAppointment.getAppointmentDate());
        assertEquals(newTime, updatedAppointment.getStartTime());
        assertEquals(status, updatedAppointment.getStatus());
    }
}
