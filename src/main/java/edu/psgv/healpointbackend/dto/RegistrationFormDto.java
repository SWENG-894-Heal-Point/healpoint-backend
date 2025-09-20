package edu.psgv.healpointbackend.dto;

import edu.psgv.healpointbackend.common.validation.PasswordPolicy;
import edu.psgv.healpointbackend.common.validation.ValidRoleFields;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Data Transfer Object for user registration.
 * <p>
 * Contains all fields required for registering a user, including both patient and doctor-specific fields.
 * Validation annotations ensure that the data meets requirements for each field.
 * The {@link ValidRoleFields} annotation enforces that required fields are present based on the user's role.
 * </p>
 *
 * <ul>
 *   <li>Patient fields: street, city, state, zip, insurance, memberId</li>
 *   <li>Doctor fields: medicalDegree, medicalSpecialty, npiNumber, experience, languages</li>
 * </ul>
 *
 * @author Mahfuzur Rahman
 * @see ValidRoleFields
 * @see PasswordPolicy
 */
@Getter
@Setter
@ValidRoleFields
public class RegistrationFormDto {
    @Email
    String email;

    @PasswordPolicy(message = "Your password must be at least 8 characters long and include a symbol, uppercase letter, lowercase letter, and number.")
    String password;

    @NotBlank
    String role;

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

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number is invalid. Please enter a 10-digit phone number without spaces or special characters.")
    String phone;

    // Patient-specific fields
    String streetAddress;
    String city;
    String state;
    String zipCode;
    String insuranceProvider;
    String insuranceId;

    // Doctor-specific fields
    String medicalDegree;
    String specialty;
    String npiNumber;
    Integer experience;
    String languages;
}
