package com.bank.domain.model.user;

import com.bank.domain.enums.UserRole;
import com.bank.domain.enums.UserStatus;
import com.bank.domain.valueobject.Address;
import com.bank.domain.valueobject.Email;
import com.bank.domain.valueobject.PhoneNumber;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * Entidad raíz que representa a cualquier usuario del sistema bancario.
 *
 * Se mapea a la tabla "users" en MySQL (via JPA/Hibernate).
 * Todos los usuarios comparten esta misma entidad:
 * clientes, empleados, analistas, supervisores, etc.
 * El campo "role" distingue el tipo.
 *
 * Reglas de negocio implementadas aquí:
 * - Solo usuarios ACTIVE pueden operar (ver canOperate()).
 * - INDIVIDUAL_CLIENT debe ser mayor de 18 años (validateLegalAge()).
 * - identificationId debe ser único — validado en RegisterUserService.
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * ID auto-generado por MySQL (AUTO_INCREMENT).
     * GenerationType.IDENTITY delega el incremento a la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    /** Referencia al cliente asociado. Null para empleados internos. */
    @Column(name = "related_id", length = 30)
    private String relatedId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    /**
     * Número de identificación (Cédula, DNI, NIT).
     * unique = true crea un índice UNIQUE en MySQL.
     */
    @Column(name = "identification_id", nullable = false, unique = true, length = 20)
    private String identificationId;

    /**
     * Los Value Objects (Email, PhoneNumber, Address) se almacenan
     * como columnas simples en MySQL usando @Embedded.
     * @AttributeOverride mapea el campo interno del VO al nombre de columna correcto.
     */
    @Embedded
    @AttributeOverride(name = "value",
        column = @Column(name = "email", nullable = false, length = 100))
    private Email email;

    @Embedded
    @AttributeOverride(name = "number",
        column = @Column(name = "phone_number", nullable = false, length = 15))
    private PhoneNumber phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Embedded
    @AttributeOverride(name = "fullAddress",
        column = @Column(name = "address", nullable = false, length = 200))
    private Address address;

    /**
     * EnumType.STRING guarda el nombre del enum como texto en MySQL.
     * Más legible que EnumType.ORDINAL (que guarda el número de posición).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /** Constructor sin argumentos requerido por JPA/Hibernate. */
    protected User() {}

    /**
     * Constructor completo para crear un usuario del sistema.
     */
    public User(String relatedId, String fullName, String identificationId,
                Email email, PhoneNumber phoneNumber, LocalDate dateOfBirth,
                Address address, UserRole role, String username, String password) {

        validateRequiredFields(fullName, identificationId, username, password);

        this.relatedId = relatedId;
        this.fullName = fullName;
        this.identificationId = identificationId;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.role = role;
        this.status = UserStatus.ACTIVE; // Todo usuario nuevo comienza ACTIVO
        this.username = username;
        this.password = password;

        // Regla de negocio: personas naturales deben ser mayores de edad
        if (role == UserRole.INDIVIDUAL_CLIENT) {
            validateLegalAge(dateOfBirth);
        }
    }

    // ═══════════════════ MÉTODOS DE NEGOCIO ═══════════════════

    /** Solo los usuarios ACTIVE pueden realizar operaciones. */
    public boolean canOperate() { return status.canOperate(); }

    /** Verifica si el usuario tiene un rol específico. */
    public boolean hasRole(UserRole requiredRole) { return this.role == requiredRole; }

    /** Verifica credenciales para autenticación. */
    public boolean authenticate(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    /** Cambia el estado del usuario (ACTIVE, INACTIVE, BLOCKED). */
    public void changeStatus(UserStatus newStatus) {
        if (newStatus == null) throw new IllegalArgumentException("The new status cannot be null.");
        this.status = newStatus;
    }

    /** Calcula la edad del usuario en años. */
    public int calculateAge() {
        if (dateOfBirth == null) return -1;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    // ═══════════════════ VALIDACIONES PRIVADAS ═══════════════════

    private void validateRequiredFields(String name, String identification,
                                        String user, String pass) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("The full name is required.");
        if (identification == null || identification.isBlank())
            throw new IllegalArgumentException("The identification number is required.");
        if (user == null || user.isBlank())
            throw new IllegalArgumentException("The username is required.");
        if (pass == null || pass.isBlank())
            throw new IllegalArgumentException("The password is required.");
    }

    /** Valida mayoría de edad (mínimo 18 años). */
    private void validateLegalAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null)
            throw new IllegalArgumentException("Date of birth is required for Individual Client.");
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age < 18)
            throw new IllegalArgumentException(
                "The client must be of legal age (at least 18 years). Calculated age: " + age);
    }

    // ═══════════════════ GETTERS ═══════════════════

    public Long getUserId()             { return userId; }
    public String getRelatedId()        { return relatedId; }
    public String getFullName()         { return fullName; }
    public String getIdentificationId() { return identificationId; }
    public Email getEmail()             { return email; }
    public PhoneNumber getPhoneNumber() { return phoneNumber; }
    public LocalDate getDateOfBirth()   { return dateOfBirth; }
    public Address getAddress()         { return address; }
    public UserRole getRole()           { return role; }
    public UserStatus getStatus()       { return status; }
    public String getUsername()         { return username; }

    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }
    public void setFullName(String fullName)   { this.fullName = fullName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(userId, ((User) o).userId);
    }

    @Override public int hashCode() { return Objects.hash(userId); }

    @Override
    public String toString() {
        return "User{userId=" + userId + ", fullName='" + fullName + '\''
            + ", identificationId='" + identificationId + '\''
            + ", role=" + role + ", status=" + status + '}';
    }
}
