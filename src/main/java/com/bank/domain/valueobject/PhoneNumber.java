package com.bank.domain.valueobject;

import java.util.Objects;

/**
 * Value Object representing a phone number.
 *
 * Immutable: once created, it cannot be modified.
 *
 * Validations:
 * - Required (cannot be null or blank).
 * - Minimum length: 7 digits.
 * - Maximum length: 15 digits.
 * - Only numeric digits are allowed (spaces and hyphens are ignored during validation).
 */
public class PhoneNumber {

    private static final int MIN_LENGTH = 7;
    private static final int MAX_LENGTH = 15;

    private final String number;

    /**
     * Creates a PhoneNumber instance validating the format and length.
     *
     * @param number the phone number
     * @throws IllegalArgumentException if null, blank, or does not meet the required length
     */
    public PhoneNumber(String number) {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("The phone number is required.");
        }

        // Extract only digits to validate the actual length
        String digitsOnly = number.replaceAll("[^0-9]", "");

        if (digitsOnly.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                "The phone number must have at least " + MIN_LENGTH
                + " digits. Digits found: " + digitsOnly.length()
            );
        }

        if (digitsOnly.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "The phone number cannot exceed " + MAX_LENGTH
                + " digits. Digits found: " + digitsOnly.length()
            );
        }

        this.number = number.trim();
    }

    public String getNumber() {
        return number;
    }

    /**
     * Returns only the digits of the phone number (without spaces, hyphens, or other characters).
     */
    public String getDigits() {
        return number.replaceAll("[^0-9]", "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return Objects.equals(getDigits(), that.getDigits());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDigits());
    }

    @Override
    public String toString() {
        return number;
    }
}
