package edu.psgv.healpointbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public class TokenDto {
    @Getter
    @Setter
    @NotBlank
    private String token;
}
