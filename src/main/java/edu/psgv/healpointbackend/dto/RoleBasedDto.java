package edu.psgv.healpointbackend.dto;

import edu.psgv.healpointbackend.common.validation.ValidRoleFields;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


/**
 * DTO that includes fields specific to user roles (Patient or Doctor).
 * Validates that the appropriate fields are provided based on the role.
 *
 * @author Mahfuzur Rahman
 */
@Getter
@Setter
@ValidRoleFields
public class RoleBasedDto {
    @NotBlank
    String role;

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
