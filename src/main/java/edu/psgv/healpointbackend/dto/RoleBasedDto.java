package edu.psgv.healpointbackend.dto;

import edu.psgv.healpointbackend.common.validation.ValidRoleFields;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ValidRoleFields
public class RoleBasedDto {
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
