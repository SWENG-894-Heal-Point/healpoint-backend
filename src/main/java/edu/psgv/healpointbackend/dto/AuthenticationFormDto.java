package edu.psgv.healpointbackend.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.*;

@Getter
@Setter
public class AuthenticationFormDto {
    @Email
    String email;

    @NotBlank
    String password;
}
