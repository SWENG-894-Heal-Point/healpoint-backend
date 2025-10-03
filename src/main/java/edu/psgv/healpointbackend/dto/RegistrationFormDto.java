package edu.psgv.healpointbackend.dto;

import edu.psgv.healpointbackend.common.validation.PasswordPolicy;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Data Transfer Object for user registration.
 * <p>
 * Contains all fields required for registering a user, including both patient and doctor-specific fields.
 * Validation annotations ensure that the data meets requirements for each field.
 * </p>
 *
 * <ul>
 *   <li>Patient fields: street, city, state, zip, insurance, memberId</li>
 *   <li>Doctor fields: medicalDegree, medicalSpecialty, npiNumber, experience, languages</li>
 * </ul>
 *
 * @author Mahfuzur Rahman
 * @see PasswordPolicy
 */
@Getter
@Setter
public class RegistrationFormDto extends RoleBasedDto {
    @Email
    String email;

    @PasswordPolicy(message = "Your password must be at least 8 characters long and include a symbol, uppercase letter, lowercase letter, and number.")
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

    @Pattern(regexp = "^\\d{10}$", message = "Phone number is invalid. Please enter a 10-digit phone number without spaces or special characters.")
    String phone;
}
