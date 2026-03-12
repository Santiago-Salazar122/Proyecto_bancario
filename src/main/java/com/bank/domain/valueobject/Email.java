package com.bank.domain.valueobject;

import java.util.Objects;

/**
 * Value Object representing an email address.
 *
 * Immutable: once created, it cannot be modified.
 *
 * Validations:
 * - Required (cannot be null or blank).
 * - Must contain the character "@".
 * - Must have a valid domain after "@" (at least one dot).
 *
 * In DDD, Value Objects encapsulate validation within the object itself,
 * guaranteeing that an invalid instance can never exist in the domain.
 */
public class Email {

    private final String value;

    /**
     * Creates an Email instance validating the format.
     *
     * @param value the email address
     * @throws IllegalArgumentException if the value is null, blank, or does not match the format
     */
    public Email(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("The email address is required.");
        }

        String cleaned = value.trim().toLowerCase();

        if (!cleaned.contains("@")) {
            throw new IllegalArgumentException(
                "The email address must contain '@'. Value received: " + value
            );
        }

        String[] parts = cleaned.split("@", 2);

        if (parts[0].isEmpty()) {
            throw new IllegalArgumentException(
                "The email address must have a user before '@'. Value received: " + value
            );
        }

        if (!parts[1].contains(".") || parts[1].startsWith(".") || parts[1].endsWith(".")) {
            throw new IllegalArgumentException(
                "The email address must have a valid domain after '@'. Value received: " + value
            );
        }

        this.value = cleaned;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
