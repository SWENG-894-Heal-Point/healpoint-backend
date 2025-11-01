package edu.psgv.healpointbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.psgv.healpointbackend.model.Doctor;
import edu.psgv.healpointbackend.model.WorkDay;
import edu.psgv.healpointbackend.repository.DoctorRepository;
import edu.psgv.healpointbackend.repository.WorkDayRepository;
import edu.psgv.healpointbackend.utilities.SlotGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScheduleManagerTest {

    @Mock
    private WorkDayRepository workDayRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private SlotGenerator slotGenerator;

    @InjectMocks
    private ScheduleManager scheduleManager;

    @BeforeEach
    void setup() throws JsonProcessingException {
        MockitoAnnotations.openMocks(this);

        when(slotGenerator.generateSlots(any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(Collections.emptyList());
    }

    private WorkDay createWorkDay(String dayName, LocalTime start, LocalTime end) {
        return WorkDay.builder()
                .dayName(dayName)
                .startTime(start)
                .endTime(end)
                .build();
    }

    @Test
    void getValidDayName_variousInputs_expectedOutputs() throws Exception {
        var method = ScheduleManager.class.getDeclaredMethod("getValidDayName", String.class);
        method.setAccessible(true);

        assertNull(method.invoke(scheduleManager, (Object) null));
        assertNull(method.invoke(scheduleManager, "   "));
        assertEquals("MON", method.invoke(scheduleManager, "monday"));
        assertEquals("TUE", method.invoke(scheduleManager, "tuesday"));
        assertNull(method.invoke(scheduleManager, "Funday"));
    }

    @Test
    void getValidWorkDays_mixedInputs_filtersInvalidOnes() throws Exception {
        var method = ScheduleManager.class.getDeclaredMethod("getValidWorkDays", Doctor.class, List.class);
        method.setAccessible(true);

        List<WorkDay> input = List.of(
                createWorkDay("Mon", LocalTime.of(9, 0), LocalTime.of(17, 0)),   // valid
                createWorkDay("Funday", LocalTime.of(9, 0), LocalTime.of(17, 0)), // invalid
                createWorkDay("Tue", null, LocalTime.of(17, 0)),                  // invalid
                createWorkDay("Wed", LocalTime.of(9, 0), null)                    // invalid
        );

        Doctor doctor = mock(Doctor.class);
        when(doctor.getId()).thenReturn(1);

        @SuppressWarnings("unchecked")
        List<WorkDay> result = (List<WorkDay>) method.invoke(scheduleManager, doctor, input);

        assertEquals(1, result.size());
        assertEquals("MON", result.get(0).getDayName());
        assertEquals(1, result.get(0).getDoctor().getId());
    }


    @Test
    void upsertWorkDays_invalidInputs_throwsException() {
        // Invalid doctor ID
        when(doctorRepository.existsById(99)).thenReturn(false);

        List<WorkDay> workDays = List.of(createWorkDay("Mon", LocalTime.of(9, 0), LocalTime.of(17, 0)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> scheduleManager.upsertWorkDays(99, workDays));

        assertTrue(ex.getMessage().contains("Invalid doctor ID"));
        verifyNoInteractions(workDayRepository);

        // Invalid day names
        when(doctorRepository.existsById(1)).thenReturn(true);

        List<WorkDay> invalidDays = List.of(createWorkDay("Invalid", LocalTime.of(9, 0), LocalTime.of(17, 0)));

        assertThrows(IllegalArgumentException.class,
                () -> scheduleManager.upsertWorkDays(1, invalidDays));

        verifyNoInteractions(workDayRepository);
    }

    @Test
    void upsertWorkDays_existingAndNewWorkDays_savesCorrectly() throws JsonProcessingException {
        when(doctorRepository.findById(1)).thenReturn(Optional.of(mock(Doctor.class)));

        WorkDay existingDay = createWorkDay("MON", LocalTime.of(9, 0), LocalTime.of(17, 0));
        WorkDay newDay = createWorkDay("TUE", LocalTime.of(10, 0), LocalTime.of(18, 0));

        WorkDay repoDay = createWorkDay("MON", LocalTime.of(8, 0), LocalTime.of(16, 0));

        when(workDayRepository.findByDoctorIdAndDayName(1, "MON")).thenReturn(Optional.of(repoDay));
        when(workDayRepository.findByDoctorIdAndDayName(1, "TUE")).thenReturn(Optional.empty());

        scheduleManager.upsertWorkDays(1, List.of(existingDay, newDay));

        // Existing one should be updated
        assertEquals(LocalTime.of(9, 0), repoDay.getStartTime());
        assertEquals(LocalTime.of(17, 0), repoDay.getEndTime());

        // Save called twice (once for update, once for new)
        verify(workDayRepository, times(2)).save(any(WorkDay.class));
    }
}
