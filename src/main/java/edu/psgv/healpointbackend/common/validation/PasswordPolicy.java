package edu.psgv.healpointbackend.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for validating password fields against a custom password policy.
 * <p>
 * This annotation can be applied to fields or parameters to enforce password requirements
 * such as minimum length, presence of uppercase/lowercase letters, digits, and special characters.
 * The actual validation logic is implemented in {@code PasswordPolicyValidator}.
 * </p>
 *
 * <pre>
 * Example usage:
 * {@code
 *   @PasswordPolicy
 *   private String password;
 * }
 * </pre>
 *
 * @author Mahfuzur Rahman
 * @see PasswordPolicyValidator
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordPolicyValidator.class)
public @interface PasswordPolicy {
    String message() default "Password does not meet policy requirements";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
