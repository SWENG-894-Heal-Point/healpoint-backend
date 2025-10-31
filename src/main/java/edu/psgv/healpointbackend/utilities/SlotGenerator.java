package edu.psgv.healpointbackend.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.psgv.healpointbackend.model.Slot;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static edu.psgv.healpointbackend.HealpointBackendApplication.CONFIG_READER;

@Component
public class SlotGenerator {
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

    private boolean isEligibleForBreak(LocalTime shiftStart, LocalTime shiftEnd) {
        int minWorkHoursForBreak = Integer.parseInt(String.valueOf(CONFIG_READER.get("minWorkHoursForBreak")));
        long workedHours = Duration.between(shiftStart, shiftEnd).toHours();
        return workedHours >= minWorkHoursForBreak;
    }
}
