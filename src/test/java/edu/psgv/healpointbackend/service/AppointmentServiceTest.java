package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.AbstractTestBase;
import edu.psgv.healpointbackend.dto.AvailableAppointmentSlotsDto;
import edu.psgv.healpointbackend.dto.ScheduleAppointmentDto;
import edu.psgv.healpointbackend.model.Appointment;
import edu.psgv.healpointbackend.model.Doctor;
import edu.psgv.healpointbackend.model.Patient;
import edu.psgv.healpointbackend.model.Slot;
import edu.psgv.healpointbackend.repository.AppointmentRepository;
import edu.psgv.healpointbackend.repository.DoctorRepository;
import edu.psgv.healpointbackend.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    @Mock
    private Logger logger;

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
        date = LocalDate.of(2025, 11, 5);
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
        // Arrange
        when(patientRepository.findById(2)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1)).thenReturn(Optional.of(doctor));
        when(doctorRepository.findById(11)).thenReturn(Optional.of(doctor));
        when(appointmentAvailabilityService.createAvailableSlotsDto(date, 1))
                .thenReturn(new AvailableAppointmentSlotsDto(doctor, date, new ArrayList<>()));

        AvailableAppointmentSlotsDto slotsDto = new AvailableAppointmentSlotsDto(doctor, date,
                List.of(new Slot(LocalTime.of(10, 0), LocalTime.of(10, 30))));
        when(appointmentAvailabilityService.createAvailableSlotsDto(date, 11)).thenReturn(slotsDto);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.scheduleAppointment(dto));
        assertTrue(ex.getMessage().contains("does not have any available slots"));

        dto.setDoctorId(11);
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.scheduleAppointment(dto));
        assertTrue(ex2.getMessage().contains("does not have any available slots"));
    }
}
