package main.java.hospital.util;

import java.util.regex.Pattern;

/**
 * Utility class for validating user input.
 */
public class ValidationUtils {
    // Regular expressions for validation
    private static final String NAME_REGEX = "^[a-zA-Z\\s-']+$";
    private static final String PHONE_REGEX = "^[0-9\\-\\+\\(\\)\\s]+$";
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    // Compiled patterns for better performance
    private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    /**
     * Validates if a string is a valid name.
     * Only allows letters, spaces, hyphens, and apostrophes.
     *
     * @param name The name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Validates if a string is a valid phone number.
     * Allows digits, hyphens, plus signs, parentheses, and spaces.
     *
     * @param phone The phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhoneNumber(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validates if a string is a valid email address.
     *
     * @param email The email address to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates if a string can be parsed as an integer.
     *
     * @param value The string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates if a string can be parsed as a double.
     *
     * @param value The string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Ensures a string is not null or empty.
     *
     * @param value The string to check
     * @return true if not null or empty, false otherwise
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}