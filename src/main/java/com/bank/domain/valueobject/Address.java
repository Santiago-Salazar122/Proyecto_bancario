package com.bank.domain.valueobject;

import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * Value Object que representa una dirección física.
 * @Embeddable permite que JPA lo almacene directamente en la tabla users.
 * El campo "fullAddress" se mapea a la columna "address" via @AttributeOverride en User.
 */
@Embeddable
public class Address {

    private String fullAddress;

    /** Constructor vacío requerido por JPA. */
    protected Address() {}

    public Address(String fullAddress) {
        if (fullAddress == null || fullAddress.isBlank())
            throw new IllegalArgumentException("The address is required.");
        this.fullAddress = fullAddress.trim();
    }

    public String getFullAddress() { return fullAddress; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(fullAddress, ((Address) o).fullAddress);
    }

    @Override public int hashCode() { return Objects.hash(fullAddress); }
    @Override public String toString() { return fullAddress; }
}
