package com.bank.domain.valueobject;

import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * Value Object que representa una dirección de correo electrónico.
 *
 * @Embeddable le dice a JPA que este objeto puede ser embebido
 * dentro de una entidad (@Entity) como si sus campos fueran
 * columnas directas de esa tabla. En User, el campo "value"
 * se mapea a la columna "email" via @AttributeOverride.
 *
 * Inmutable: una vez creado no se puede modificar.
 * Valida que tenga "@" y un dominio válido.
 */
@Embeddable
public class Email {

    private String value;

    /** Constructor vacío requerido por JPA para @Embeddable. */
    protected Email() {}

    public Email(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("The email address is required.");

        String cleaned = value.trim().toLowerCase();

        if (!cleaned.contains("@"))
            throw new IllegalArgumentException(
                "The email must contain '@'. Value: " + value);

        String[] parts = cleaned.split("@", 2);

        if (parts[0].isEmpty())
            throw new IllegalArgumentException(
                "The email must have a user before '@'. Value: " + value);

        if (!parts[1].contains(".") || parts[1].startsWith(".") || parts[1].endsWith("."))
            throw new IllegalArgumentException(
                "The email must have a valid domain after '@'. Value: " + value);

        this.value = cleaned;
    }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(value, ((Email) o).value);
    }

    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
