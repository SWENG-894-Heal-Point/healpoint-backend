package edu.psgv.healpointbackend.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.psgv.healpointbackend.model.Slot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SlotGeneratorTest {

    private SlotGenerator slotGenerator;

    @BeforeEach
    void setUp() {
        slotGenerator = new SlotGenerator();
    }

    @Test
    void generateSlots_validShiftWithBreak_removesMiddleSlot() throws JsonProcessingException {
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(14, 0);

        List<Slot> result = slotGenerator.generateSlots(start, end);
        List<LocalTime> startTimes = result.stream().map(Slot::getStartTime).toList();

        assertEquals(11, result.size());
        assertFalse(startTimes.contains(LocalTime.of(11, 0)));
    }

    @Test
    void generateSlots_validShiftWithoutBreak_allSlotsGenerated() throws JsonProcessingException {
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(11, 0);

        List<Slot> result = slotGenerator.generateSlots(start, end);

        assertEquals(6, result.size());
        assertEquals(LocalTime.of(9, 30), result.get(3).getStartTime());
    }

    @Test
    void generateSlots_invalidShift_noSlotsGenerated() throws JsonProcessingException {
        LocalTime start = LocalTime.of(12, 0);
        LocalTime end = LocalTime.of(8, 0);

        // Start time is null
        List<Slot> result = slotGenerator.generateSlots(null, end);
        assertEquals(0, result.size());

        // End time is null
        result = slotGenerator.generateSlots(start, null);
        assertEquals(0, result.size());

        // Start time is after end time
        result = slotGenerator.generateSlots(start, end);
        assertEquals(0, result.size());
    }
}