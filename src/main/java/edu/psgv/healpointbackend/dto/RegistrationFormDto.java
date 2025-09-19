package edu.psgv.healpointbackend.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;


public class RegistrationFormDto {
    @NotBlank
    String role;

    @Email
    String email;

    @Size(min = 8, message = "Your password must be at least 8 characters long and include a symbol, uppercase letter, lowercase letter, and number.")
    String password;

    @NotBlank
    String confirmPassword;

    @NotBlank
    String firstName;

    @NotBlank
    String lastName;

    @Past
    LocalDate dateOfBirth;

    @NotBlank
    String gender;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number is invalid. Pease enter a 10-digit phone number without spaces or special characters.")
    String phone;

    // Patient-specific fields
    String street;
    String city;
    String state;
    String zip;
    String insurance;
    String memberId;

    // Doctor-specific fields
    String medicalDegree;
    String medicalSpecialty;
    String npiNumber;
    String experience;
    String languages;
}
