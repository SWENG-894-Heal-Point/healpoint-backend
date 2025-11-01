package edu.psgv.healpointbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


/**
 * Data Transfer Object for carrying an authentication token.
 *
 * @author Mahfuzur Rahman
 */
public class TokenDto {
    @Getter
    @Setter
    @NotBlank
    private String token;
}
