package edu.psgv.healpointbackend.utilities;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * Utility class for password hashing and verification using BCrypt.
 *
 * @author Mahfuzur Rahman
 */
public class PasswordUtils {
    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Hashes a plain text password using BCrypt.
     *
     * @param password the plain text password to hash
     * @return the hashed password
     * @throws IllegalArgumentException if the input password is null, empty, or whitespace only
     */
    public static String hashPassword(String password) {
        try {
            String validPassword = IoHelper.validateString(password);
            return encoder.encode(validPassword);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Verifies a plain text password against a hashed password.
     *
     * @param password       the plain text password to verify
     * @param hashedPassword the hashed password to compare against
     * @return true if the passwords match, false otherwise
     * @throws IllegalArgumentException if either input is null, empty, or whitespace only
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        try {
            String validPassword = IoHelper.validateString(password);
            String validHashedPassword = IoHelper.validateString(hashedPassword);
            return encoder.matches(validPassword, validHashedPassword);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
