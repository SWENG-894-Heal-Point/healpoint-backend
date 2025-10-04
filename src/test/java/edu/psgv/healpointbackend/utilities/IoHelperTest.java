package edu.psgv.healpointbackend.utilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IoHelperTest {

    @Test
    void validateString_validInput_returnsTrimmedString() {
        String result = IoHelper.validateString("  valid input  ");
        assertEquals("valid input", result);
    }

    @Test
    void validateString_invalidInput_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> IoHelper.validateString(null));
        assertThrows(IllegalArgumentException.class, () -> IoHelper.validateString(""));
        assertThrows(IllegalArgumentException.class, () -> IoHelper.validateString("    "));
    }

    @Test
    void isNullOrEmpty_invalidInput_returnsTrue() {
        assertTrue(IoHelper.isNullOrEmpty(null));
        assertTrue(IoHelper.isNullOrEmpty(""));
        assertTrue(IoHelper.isNullOrEmpty("    "));
    }

    @Test
    void isNullOrEmpty_nonEmptyString_returnsFalse() {
        assertFalse(IoHelper.isNullOrEmpty("test"));
        assertFalse(IoHelper.isNullOrEmpty("  text  "));
    }
}