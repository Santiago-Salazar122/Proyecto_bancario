package com.bank.domain.valueobject;

import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * Value Object que representa un número de teléfono.
 * @Embeddable permite que JPA lo almacene como columna en la tabla users.
 * El campo "number" se mapea a "phone_number" via @AttributeOverride en User.
 */
@Embeddable
public class PhoneNumber {

    private static final int MIN_LENGTH = 7;
    private static final int MAX_LENGTH = 15;

    private String number;

    /** Constructor vacío requerido por JPA. */
    protected PhoneNumber() {}

    public PhoneNumber(String number) {
        if (number == null || number.isBlank())
            throw new IllegalArgumentException("The phone number is required.");

        String digits = number.replaceAll("[^0-9]", "");

        if (digits.length() < MIN_LENGTH)
            throw new IllegalArgumentException(
                "Phone must have at least " + MIN_LENGTH + " digits. Found: " + digits.length());

        if (digits.length() > MAX_LENGTH)
            throw new IllegalArgumentException(
                "Phone cannot exceed " + MAX_LENGTH + " digits. Found: " + digits.length());

        this.number = number.trim();
    }

    public String getNumber() { return number; }
    public String getDigits() { return number.replaceAll("[^0-9]", ""); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(getDigits(), ((PhoneNumber) o).getDigits());
    }

    @Override public int hashCode() { return Objects.hash(getDigits()); }
    @Override public String toString() { return number; }
}
