package edu.psgv.healpointbackend.utilities;

/**
 * Utility class for input/output helper methods.
 *
 * @author Mahfuzur Rahman
 */
public class IoHelper {
    /**
     * Private constructor to prevent instantiation.
     */
    private IoHelper() {
    }

    /**
     * Validates the given string by checking if it is not null, empty, or whitespace only.
     * If valid, returns the trimmed version of the string.
     *
     * @param text the input string to validate
     * @return the trimmed string if valid
     * @throws IllegalArgumentException if the input is null, empty, or contains only whitespace
     */
    public static String validateString(String text) {
        if (isNullOrEmpty(text)) {
            throw new IllegalArgumentException("Input string must not be null, empty, or whitespace only.");
        }
        return text.trim();
    }

    /**
     * Checks if the given string is null, empty, or contains only whitespace.
     *
     * @param text the string to check
     * @return true if the string is null, empty, or whitespace only; false otherwise
     */
    public static boolean isNullOrEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }
}
