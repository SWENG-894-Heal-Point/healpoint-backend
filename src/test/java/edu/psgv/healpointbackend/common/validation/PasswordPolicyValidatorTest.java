package edu.psgv.healpointbackend.common.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PasswordPolicyValidatorTest {

    private PasswordPolicyValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new PasswordPolicyValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void isValid_validPassword_passesPolicy() {
        assertTrue(validator.isValid("Abcdef1@", context));
    }

    @Test
    void isValid_invalidPassword_failsPolicy() {
        assertFalse(validator.isValid("Ab1@", context));        // too short
        assertFalse(validator.isValid("abcdefg1@", context));   // missing uppercase
        assertFalse(validator.isValid("ABCDEFG1@", context));   // missing lowercase
        assertFalse(validator.isValid("Abcdefg@", context));    // missing digit
        assertFalse(validator.isValid("Abcdef12", context));    // missing special character
        assertFalse(validator.isValid(null, context));          // null case
    }
}