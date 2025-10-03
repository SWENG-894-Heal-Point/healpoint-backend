package edu.psgv.healpointbackend.common.validation;

import edu.psgv.healpointbackend.dto.RegistrationFormDto;
import edu.psgv.healpointbackend.dto.RoleBasedDto;
import edu.psgv.healpointbackend.model.Roles;
import edu.psgv.healpointbackend.utilities.IoHelper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for the {@link ValidRoleFields} annotation.
 * <p>
 * This class checks that all required fields for a user are filled in based on their role
 * (either "patient" or "doctor") in the {@link RegistrationFormDto}.
 * <ul>
 *   <li>If the role is "patient", it validates that street, city, state, zip, insurance, and memberId are not empty.</li>
 *   <li>If the role is "doctor", it validates that medicalDegree, medicalSpecialty, npiNumber, experience, and languages are not empty.</li>
 * </ul>
 * If any required field is missing, a custom constraint violation message is added.
 * </p>
 *
 * @author Mahfuzur Rahman
 * @see ValidRoleFields
 * @see RoleBasedDto
 */
public class RoleBasedFieldValidator implements ConstraintValidator<ValidRoleFields, RoleBasedDto> {

    /**
     * Validates that all required fields for the given role are filled in.
     *
     * @param dto     the registration form data transfer object to validate
     * @param context the validation context
     * @return {@code true} if all required fields for the role are filled in, {@code false} otherwise
     */
    @Override
    public boolean isValid(RoleBasedDto dto, ConstraintValidatorContext context) {
        if (IoHelper.isNullOrEmpty(dto.getRole())) {
            return true;
        }

        boolean isValid = true;

        if (dto.getRole().equalsIgnoreCase(Roles.PATIENT)) {
            isValid &= !IoHelper.isNullOrEmpty(dto.getStreetAddress());
            isValid &= !IoHelper.isNullOrEmpty(dto.getCity());
            isValid &= !IoHelper.isNullOrEmpty(dto.getState());
            isValid &= !IoHelper.isNullOrEmpty(dto.getZipCode());

            if (!isValid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("All patient fields must be filled in.")
                        .addConstraintViolation();
            }
        } else if (dto.getRole().equalsIgnoreCase(Roles.DOCTOR)) {
            isValid &= !IoHelper.isNullOrEmpty(dto.getMedicalDegree());
            isValid &= !IoHelper.isNullOrEmpty(dto.getSpecialty());
            isValid &= !IoHelper.isNullOrEmpty(dto.getNpiNumber());

            if (!isValid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("All doctor fields must be filled in.")
                        .addConstraintViolation();
            }
        }

        return isValid;
    }
}
