package edu.psgv.healpointbackend.utilities;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class PasswordUtilsTest {
    @Test
    void hashPassword_shouldNotBeSameAsRawPassword() {
        String raw = "StrongP@ssw0rd!";
        String hashed = PasswordUtils.hashPassword(raw);
        assertNotNull(hashed, "Hashed password should not be null");
        assertNotEquals(raw, hashed, "Hashed password should not equal the raw password");
    }

    @Test
    void hashPassword_comparableWithEncoderMatches() {
        String raw = "StrongP@ssw0rd!";
        String hashed = PasswordUtils.hashPassword(raw);

        PasswordEncoder encoder = new BCryptPasswordEncoder();
        assertTrue(encoder.matches(raw, hashed), "BCrypt encoder should match raw password with hashed password");
    }

    @Test
    void hashPassword_invalidInput_throwException() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtils.hashPassword(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtils.hashPassword(""));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtils.hashPassword("   "));
    }

    @Test
    void verifyPassword_correctPassword_returnTrue() {
        String raw = "StrongP@ssw0rd!";
        String hashed = PasswordUtils.hashPassword(raw);

        assertTrue(PasswordUtils.verifyPassword(raw, hashed), "Verification should succeed for matching password");
    }

    @Test
    void verifyPassword_incorrectPassword_returnFalse() {
        String raw = "StrongP@ssw0rd!";
        String hashed = PasswordUtils.hashPassword(raw);

        assertFalse(PasswordUtils.verifyPassword("WrongP@ss", hashed), "Verification should fail for non-matching password");
    }

    @Test
    void verifyPassword_invalidInput_throwException() {
        String hashed = PasswordUtils.hashPassword("StrongP@ssw0rd!");

        assertThrows(IllegalArgumentException.class, () -> PasswordUtils.verifyPassword(null, hashed));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtils.verifyPassword("", hashed));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtils.verifyPassword("   ", hashed));

        assertThrows(IllegalArgumentException.class, () -> PasswordUtils.verifyPassword("StrongP@ssw0rd!", null));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtils.verifyPassword("StrongP@ssw0rd!", ""));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtils.verifyPassword("StrongP@ssw0rd!", "   "));
    }
}