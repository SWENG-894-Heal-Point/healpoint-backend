package edu.psgv.healpointbackend.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

/**
 * Validator for the {@link PasswordPolicy} annotation.
 * <p>
 * This class enforces password requirements such as:
 * <ul>
 *   <li>Minimum length of 8 characters</li>
 *   <li>At least one uppercase letter</li>
 *   <li>At least one lowercase letter</li>
 *   <li>At least one digit</li>
 *   <li>At least one special character from [@ $ ! % * ? &]</li>
 * </ul>
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
 * @see PasswordPolicy
 */
@Component
public class PasswordPolicyValidator implements ConstraintValidator<PasswordPolicy, String> {
    /**
     * Validates the given password against the defined policy.
     *
     * @param password the password to validate
     * @param context  the validation context
     * @return {@code true} if the password meets all requirements, {@code false} otherwise
     */
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        return password != null
                && password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*\\d.*")
                && password.matches(".*[!@#$%^*&?].*");
    }
}

