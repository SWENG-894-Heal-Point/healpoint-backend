package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.AbstractTestBase;
import edu.psgv.healpointbackend.dto.AvailableAppointmentSlotsDto;
import edu.psgv.healpointbackend.dto.ScheduleAppointmentDto;
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
    private LocalTime startTime;
    private Slot slot;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        date = LocalDate.now().plusDays(1);
        startTime = LocalTime.of(9, 0);
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

        assertTrue(appointments.size() == 3);
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
}
