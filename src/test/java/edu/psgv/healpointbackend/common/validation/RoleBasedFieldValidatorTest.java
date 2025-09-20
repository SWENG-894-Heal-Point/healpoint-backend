package edu.psgv.healpointbackend.common.validation;

import edu.psgv.healpointbackend.dto.RegistrationFormDto;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RoleBasedFieldValidatorTest {

    private RoleBasedFieldValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new RoleBasedFieldValidator();

        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        org.mockito.Mockito.lenient().when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        org.mockito.Mockito.lenient().when(builder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    void isValid_patientRole_AllFieldsFilled_returnsTrue() {
        RegistrationFormDto dto = new RegistrationFormDto();
        dto.setRole("patient");
        dto.setStreetAddress("123 Main St");
        dto.setCity("Springfield");
        dto.setState("PA");
        dto.setZipCode("19000");

        assertTrue(validator.isValid(dto, context));
    }

    @Test
    void isValid_patientRole_MissingField_returnsFalse() {
        RegistrationFormDto dto = new RegistrationFormDto();
        dto.setRole("patient");
        dto.setStreetAddress("123 Main St");
        dto.setCity("Springfield");
        dto.setState("PA");
        dto.setZipCode("");  // missing

        assertFalse(validator.isValid(dto, context));
    }

    @Test
    void isValid_doctorRole_AllFieldsFilled_returnsTrue() {
        RegistrationFormDto dto = new RegistrationFormDto();
        dto.setRole("doctor");
        dto.setMedicalDegree("MD");
        dto.setSpecialty("Cardiology");
        dto.setNpiNumber("9876543210");

        assertTrue(validator.isValid(dto, context));
    }

    @Test
    void isValid_doctorRole_MissingField_returnsFalse() {
        RegistrationFormDto dto = new RegistrationFormDto();
        dto.setRole("doctor");
        dto.setMedicalDegree("MD");
        dto.setSpecialty("Cardiology");
        dto.setNpiNumber(""); // missing

        assertFalse(validator.isValid(dto, context));
    }

    @Test
    void isValid_unknownRole_returnsTrue() {
        RegistrationFormDto dto = new RegistrationFormDto();
        dto.setRole("admin");

        assertTrue(validator.isValid(dto, context));
    }

    @Test
    void isValid_nullRole_returnsTrue() {
        RegistrationFormDto dto = new RegistrationFormDto();
        dto.setRole(null);

        assertTrue(validator.isValid(dto, context));
    }


}