package edu.psgv.healpointbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.psgv.healpointbackend.AbstractTestBase;
import edu.psgv.healpointbackend.dto.AppointmentCountDto;
import edu.psgv.healpointbackend.dto.AvailableAppointmentDatesDto;
import edu.psgv.healpointbackend.dto.AvailableAppointmentSlotsDto;
import edu.psgv.healpointbackend.model.*;
import edu.psgv.healpointbackend.repository.AppointmentRepository;
import edu.psgv.healpointbackend.repository.DoctorRepository;
import edu.psgv.healpointbackend.repository.WorkDayRepository;
import edu.psgv.healpointbackend.utilities.SlotGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentAvailabilityServiceTest extends AbstractTestBase {
    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private WorkDayRepository workDayRepository;

    @Mock
    private SlotGenerator slotGenerator;

    @Spy
    @InjectMocks
    private AppointmentAvailabilityService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAvailableAppointmentSlots_multipleDoctors_mixedAvailability() {
        LocalDate date = LocalDate.now().plusDays(1);
        List<Integer> doctorIds = List.of(1, 2);

        // Doctor 1 has available slots
        Doctor doctor1 = mockDoctor(1, "Test", "Doctor1");
        AvailableAppointmentSlotsDto slotsDto = new AvailableAppointmentSlotsDto(doctor1, date, List.of(new Slot(LocalTime.of(9, 0), LocalTime.of(10, 0))));

        doReturn(slotsDto).when(service).createAvailableSlotsDto(date, 1);

        // Doctor 2 has no available slots
        doReturn(null).when(service).createAvailableSlotsDto(date, 2);

        List<AvailableAppointmentSlotsDto> result = service.getAvailableAppointmentSlots(date, doctorIds);

        assertEquals(1, result.size());
        assertEquals(slotsDto.getDoctor(), result.get(0).getDoctor());
        assertEquals(slotsDto.getAppointmentDate(), result.get(0).getAppointmentDate());
        assertEquals(slotsDto.getAvailableSlots(), result.get(0).getAvailableSlots());
    }

    @Test
    void createAvailableSlotsDto_validSchedule_someSlotsBooked_returnsRemainingSlots() throws JsonProcessingException {
        int doctorId = 1;
        Doctor doctor = mockDoctor(doctorId, "Test", "Doctor");
        Patient patient = mockPatient(1, "Test", "Patient");
        LocalDate date = LocalDate.now().plusDays(1);
        String dayName = date.getDayOfWeek().name().substring(0, 3);

        WorkDay workDay = WorkDay.builder().dayName(dayName).startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(10, 0)).build();

        Slot slot1 = new Slot(LocalTime.of(8, 0), LocalTime.of(9, 0));
        Slot slot2 = new Slot(LocalTime.of(9, 0), LocalTime.of(10, 0));

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(workDayRepository.findByDoctorIdAndDayName(doctorId, dayName)).thenReturn(Optional.of(workDay));
        when(slotGenerator.generateSlots(workDay.getStartTime(), workDay.getEndTime())).thenReturn(new ArrayList<>(List.of(slot1, slot2)));

        Appointment booked = new Appointment(doctor, patient, date, LocalTime.of(8, 0), LocalTime.of(9, 0), "Follow-up");
        booked.setStartTime(LocalTime.of(8, 0));
        booked.setEndTime(LocalTime.of(9, 0));
        when(appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, date)).thenReturn(List.of(booked));

        AvailableAppointmentSlotsDto result = service.createAvailableSlotsDto(date, doctorId);

        assertNotNull(result);
        assertEquals(1, result.getAvailableSlots().size());
        assertEquals(slot2.getStartTime(), result.getAvailableSlots().get(0).getStartTime());
    }

    @Test
    void getAvailableAppointmentDates_mixedDoctorSchedules_returnsAvailableDates() {
        Doctor doc1 = mockDoctor(1, "Test", "Doctor1");
        Doctor doc2 = mockDoctor(2, "Test", "Doctor2");
        WorkDay wd1 = WorkDay.builder().doctor(doc1).dayName("THU").slotCount(5).build();

        LocalDate date1 = LocalDate.now().plusDays(3);
        AppointmentCountDto countDto1 = new AppointmentCountDto(1, date1, 3L);
        AppointmentCountDto countDto2 = new AppointmentCountDto(2, date1, 5L);

        when(doctorRepository.findAll()).thenReturn(List.of(doc1, doc2));
        when(workDayRepository.findByDoctorId(1)).thenReturn(List.of(wd1));
        when(workDayRepository.findByDoctorId(2)).thenReturn(Collections.emptyList());
        when(appointmentRepository.getAppointmentCounts()).thenReturn(List.of(countDto1, countDto2));

        List<AvailableAppointmentDatesDto> result = service.getAvailableAppointmentDates();

        assertEquals(1, result.size());
        assertEquals(doc1, result.get(0).getDoctor());
        assertFalse(result.get(0).getAvailableDates().isEmpty());
    }

    @Test
    void getAvailableAppointmentDates_noSchedules_returnsEmptyList() {
        when(doctorRepository.findAll()).thenReturn(Collections.emptyList());
        when(appointmentRepository.getAppointmentCounts()).thenReturn(Collections.emptyList());

        List<AvailableAppointmentDatesDto> result = service.getAvailableAppointmentDates();

        assertTrue(result.isEmpty());
    }
}