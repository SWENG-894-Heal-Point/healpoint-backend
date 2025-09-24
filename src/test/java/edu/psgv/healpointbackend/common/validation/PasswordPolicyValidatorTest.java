package edu.psgv.healpointbackend.common.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
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
    void isValid_shortPassword_failsPolicy() {
        assertFalse(validator.isValid("Ab1@", context));
    }

    @Test
    void isValid_passwordMissingUppercase_failsPolicy() {
        assertFalse(validator.isValid("abcdefg1@", context));
    }

    @Test
    void isValid_passwordMissingLowercase_failsPolicy() {
        assertFalse(validator.isValid("ABCDEFG1@", context));
    }

    @Test
    void isValid_passwordMissingDigit_failsPolicy() {
        assertFalse(validator.isValid("Abcdefg@", context));
    }

    @Test
    void isValid_asswordMissingSpecialChar_failsPolicy() {
        assertFalse(validator.isValid("Abcdef12", context));
    }

    @Test
    void isValid_nullPassword_failsPolicy() {
        assertFalse(validator.isValid(null, context));
    }


}