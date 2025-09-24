package edu.psgv.healpointbackend.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Annotation for validating that all required fields are present in a registration form
 * based on the user's role (e.g., patient or doctor).
 * <p>
 * This annotation should be applied to classes (typically DTOs) to ensure that
 * role-specific fields are filled in. The validation logic is implemented in
 * {@link RoleBasedFieldValidator}.
 * </p>
 *
 * <pre>
 * Example usage:
 * {@code
 *   @ValidRoleFields
 *   public class RegistrationFormDto { ... }
 * }
 * </pre>
 *
 * @author Mahfuzur Rahman
 * @see RoleBasedFieldValidator
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RoleBasedFieldValidator.class)
@Documented
public @interface ValidRoleFields {
    String message() default "Missing required fields based on role";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

