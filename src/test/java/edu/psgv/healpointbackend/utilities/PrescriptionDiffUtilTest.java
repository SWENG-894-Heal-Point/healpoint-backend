package edu.psgv.healpointbackend.utilities;

import edu.psgv.healpointbackend.model.PrescriptionItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrescriptionDiffUtilTest {
    private PrescriptionDiffUtil diffUtil;

    @BeforeEach
    void setUp() {
        diffUtil = new PrescriptionDiffUtil();
    }

    @Test
    void diffReport_variousInput_returnsCorrectOutput() {
        PrescriptionItem item1 = mockPrescriptionItem("A", 100, 2, 10, 3);
        PrescriptionItem item2 = mockPrescriptionItem("B", 200, 1, 5, 1);
        PrescriptionItem item3 = mockPrescriptionItem("C", 150, 3, 7, 2);
        PrescriptionItem updatedItem3 = mockPrescriptionItem("C", 80, 4, 30, 3);
        PrescriptionItem newItem = mockPrescriptionItem("D", 250, 2, 14, 4);

        List<PrescriptionItem> oldList = List.of(item1, item2, item3);
        List<PrescriptionItem> newList = List.of(item2, updatedItem3, newItem);

        String report = diffUtil.diffReport(oldList, newList);
        String expected = "Doctors updated your prescription! " + "D (250 mg) [added]; " + "A (100 mg) [removed]; " +
                "C: dosage changed from 150 to 80, frequency changed from 3 to 4, duration changed from 7 to 30, fills left changed from 2 to 3";

        assertEquals(expected, report);
    }

    protected PrescriptionItem mockPrescriptionItem(String medication, int dosage, int frequency, int duration, int fillsLeft) {
        PrescriptionItem item = new PrescriptionItem();
        item.setMedication(medication);
        item.setDosage(dosage);
        item.setFrequency(frequency);
        item.setDuration(duration);
        item.setFillsLeft(fillsLeft);
        return item;
    }
}