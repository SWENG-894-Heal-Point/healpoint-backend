package edu.psgv.healpointbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UpdateProfileDto extends RoleBasedDto {
    @NotBlank
    private String token;

    @Email
    String email;

    @NotBlank
    String gender;

    @Pattern(regexp = "^\\d{10}$", message = "Phone number is invalid. Please enter a 10-digit phone number without spaces or special characters.")
    String phone;
}
