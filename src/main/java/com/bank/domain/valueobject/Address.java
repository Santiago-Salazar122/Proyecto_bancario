package com.bank.domain.valueobject;

import java.util.Objects;

/**
 * Value Object representing a mailing or fiscal address.
 *
 * Immutable: once created, it cannot be modified.
 *
 * Validations:
 * - Required (cannot be null or blank).
 *
 * Modelled as a Value Object to encapsulate validation and allow
 * future extensions (splitting into street, city, postal code, etc.).
 */
public class Address {

    private final String fullAddress;

    /**
     * Creates an Address instance, validating that it is not empty.
     *
     * @param fullAddress the complete address as text
     * @throws IllegalArgumentException if null or blank
     */
    public Address(String fullAddress) {
        if (fullAddress == null || fullAddress.isBlank()) {
            throw new IllegalArgumentException("The address is required.");
        }
        this.fullAddress = fullAddress.trim();
    }

    public String getFullAddress() {
        return fullAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(fullAddress, address.fullAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullAddress);
    }

    @Override
    public String toString() {
        return fullAddress;
    }
}
