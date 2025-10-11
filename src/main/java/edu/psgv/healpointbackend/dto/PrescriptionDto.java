package edu.psgv.healpointbackend.dto;

import edu.psgv.healpointbackend.model.PrescriptionItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


/**
 * Data Transfer Object for patient prescriptions.
 * <p>
 * Contains fields for patient ID, prescription items, and additional instructions.
 * Validation annotations ensure that required fields are provided.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Getter
@Setter
public class PrescriptionDto {
    @NotBlank
    private String token;

    @NotNull
    private int patientId;

    private String instruction;

    private List<PrescriptionItem> prescriptionItems;
}
