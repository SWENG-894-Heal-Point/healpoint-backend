package edu.psgv.healpointbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


/**
 * DTO for authentication form data, includes email and password.
 *
 * @author Mahfuzur Rahman
 */
@Getter
@Setter
public class AuthenticationFormDto {
    @Email
    String email;

    @NotBlank
    String password;
}
