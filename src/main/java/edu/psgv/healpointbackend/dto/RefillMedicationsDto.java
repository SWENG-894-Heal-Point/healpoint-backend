package edu.psgv.healpointbackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


/**
 * Data Transfer Object for requesting medication refills.
 * <p>
 * Contains a list of medication names to be refilled along with an authentication token.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
public class RefillMedicationsDto extends TokenDto {
    @Getter
    @Setter
    @NotNull
    private List<String> medications;
}
