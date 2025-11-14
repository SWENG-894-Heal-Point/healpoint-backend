package edu.psgv.healpointbackend.utilities;

import edu.psgv.healpointbackend.model.PrescriptionItem;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * Utility class for generating diff reports between lists of PrescriptionItem objects.
 *
 * @author Mahfuzur Rahman
 */
@Component
public class PrescriptionDiffUtil {
    /**
     * Generates a diff report between two lists of PrescriptionItem objects.
     *
     * @param oldList the original list of PrescriptionItem objects
     * @param newList the updated list of PrescriptionItem objects
     * @return a string report detailing additions, removals, and changes
     */
    public String diffReport(List<PrescriptionItem> oldList, List<PrescriptionItem> newList) {
        List<String> reportItems = new java.util.ArrayList<>();

        List<PrescriptionItem> added = (List<PrescriptionItem>) CollectionUtils.subtract(newList, oldList);
        List<PrescriptionItem> removed = (List<PrescriptionItem>) CollectionUtils.subtract(oldList, newList);
        List<PrescriptionItem> common = (List<PrescriptionItem>) CollectionUtils.intersection(oldList, newList);

        for (PrescriptionItem item : added) {
            reportItems.add(String.format("%s (%d mg) [added]", item.getMedication(), item.getDosage()));
        }

        for (PrescriptionItem item : removed) {
            reportItems.add(String.format("%s (%d mg) [removed]", item.getMedication(), item.getDosage()));
        }

        for (PrescriptionItem item : common) {
            PrescriptionItem oldItem = oldList.stream()
                    .filter(i -> i.getMedication().equals(item.getMedication()))
                    .findFirst().orElse(new PrescriptionItem());
            PrescriptionItem newItem = newList.stream()
                    .filter(i -> i.getMedication().equals(item.getMedication()))
                    .findFirst().orElse(new PrescriptionItem());

            String changes = comparePrescriptionItem(oldItem, newItem);
            if (!IoHelper.isNullOrEmpty(changes)) {
                reportItems.add(changes);
            }
        }

        String prefix = reportItems.isEmpty() ? "" : "Doctors updated your prescription! ";
        return prefix.concat(String.join("; ", reportItems));
    }

    /**
     * Compares two PrescriptionItem objects and returns a string describing the differences.
     *
     * @param oldItem the original PrescriptionItem
     * @param newItem the updated PrescriptionItem
     * @return a string detailing the differences, or an empty string if no differences exist
     */
    public String comparePrescriptionItem(PrescriptionItem oldItem, PrescriptionItem newItem) {
        List<String> differences = new ArrayList<>();

        if (!oldItem.getDosage().equals(newItem.getDosage())) {
            differences.add(String.format("dosage changed from %d to %d", oldItem.getDosage(), newItem.getDosage()));
        }
        if (!oldItem.getFrequency().equals(newItem.getFrequency())) {
            differences.add(String.format("frequency changed from %d to %d", oldItem.getFrequency(), newItem.getFrequency()));
        }
        if (!oldItem.getDuration().equals(newItem.getDuration())) {
            differences.add(String.format("duration changed from %d to %d", oldItem.getDuration(), newItem.getDuration()));
        }
        if (!oldItem.getFillsLeft().equals(newItem.getFillsLeft())) {
            differences.add(String.format("fillsLeft changed from %d to %d", oldItem.getFillsLeft(), newItem.getFillsLeft()));
        }

        String prefix = differences.isEmpty() ? "" : String.format("%s: ", oldItem.getMedication());
        return prefix.concat(String.join(", ", differences));
    }
}
