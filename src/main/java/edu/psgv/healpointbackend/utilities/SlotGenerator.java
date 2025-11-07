package edu.psgv.healpointbackend.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.psgv.healpointbackend.model.Slot;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static edu.psgv.healpointbackend.HealpointBackendApplication.CONFIG_READER;


/**
 * Utility class for generating time slots within a given shift.
 * <p>
 * Generates slots based on configured duration and includes logic for breaks.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Component
public class SlotGenerator {
    /**
     * Generates time slots for a given shift.
     *
     * @param shiftStart the start time of the shift
     * @param shiftEnd   the end time of the shift
     * @return a list of generated slots
     * @throws JsonProcessingException if there is an error processing JSON data
     */
    public List<Slot> generateSlots(LocalTime shiftStart, LocalTime shiftEnd) throws JsonProcessingException {
        List<Slot> slots = new ArrayList<>();

        int slotDurationMinutes = Integer.parseInt(String.valueOf(CONFIG_READER.get("slotDurationMinutes")));
        if (slotDurationMinutes <= 0 || shiftStart == null || shiftEnd == null || !shiftStart.isBefore(shiftEnd)) {
            return slots;
        }

        LocalTime lastStart = shiftEnd.minusMinutes(slotDurationMinutes);
        for (LocalTime current = shiftStart; !current.isAfter(lastStart); current = current.plusMinutes(slotDurationMinutes)) {
            LocalTime slotEndTime = current.plusMinutes(slotDurationMinutes);
            slots.add(new Slot(current, slotEndTime));
        }

        int totalSlots = slots.size();
        if (isEligibleForBreak(shiftStart, shiftEnd) && totalSlots > 2) {
            slots.remove(totalSlots / 2);
        }

        return slots;
    }

    /**
     * Checks if the shift duration qualifies for a break.
     *
     * @param shiftStart the start time of the shift
     * @param shiftEnd   the end time of the shift
     * @return true if the shift duration meets or exceeds the minimum required hours for a break, false otherwise
     */
    private boolean isEligibleForBreak(LocalTime shiftStart, LocalTime shiftEnd) {
        int minWorkHoursForBreak = Integer.parseInt(String.valueOf(CONFIG_READER.get("minWorkHoursForBreak")));
        long workedHours = Duration.between(shiftStart, shiftEnd).toHours();
        return workedHours >= minWorkHoursForBreak;
    }
}
