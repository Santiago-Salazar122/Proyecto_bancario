package com.bank.domain.model.user;

import com.bank.domain.enums.UserRole;
import com.bank.domain.enums.UserStatus;
import com.bank.domain.valueobject.Address;
import com.bank.domain.valueobject.Email;
import com.bank.domain.valueobject.PhoneNumber;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * Root Entity representing any user of the banking system.
 *
 * This is the centralized user entity. All users (clients, employees, analysts)
 * share this base structure.
 *
 * Field mappings:
 * - User_ID              → userId
 * - Related_ID           → relatedId (reference to the associated Client entity)
 * - Full_Name            → fullName
 * - Identification_ID    → identificationId (DNI / National ID / Tax ID - UNIQUE)
 * - Email_Address        → email (Value Object)
 * - Phone_Number         → phoneNumber (Value Object)
 * - Date_Of_Birth        → dateOfBirth
 * - Address              → address (Value Object)
 * - System_Role          → role (Enum)
 * - User_Status          → status (Enum)
 *
 * Applicable business rules:
 * - Username and password are required for authentication.
 * - Only users with ACTIVE status can operate.
 * - The uniqueness of Identification_ID is validated at the service/repository level.
 */
public class User {

    private final int userId;
    private String relatedId;
    private String fullName;
    private String identificationId;
    private Email email;
    private PhoneNumber phoneNumber;
    private LocalDate dateOfBirth;
    private Address address;
    private UserRole role;
    private UserStatus status;

    // Authentication fields
    private String username;
    private String password;

    /**
     * Full constructor to create a system user.
     *
     * @param userId           unique identifier of the user in the system
     * @param relatedId        identifier of the associated client entity (may be null for internal employees)
     * @param fullName         full name of the user
     * @param identificationId identification number (DNI/National ID/Tax ID) - must be unique
     * @param email            email address (Value Object with validation)
     * @param phoneNumber      phone number (Value Object with validation)
     * @param dateOfBirth      date of birth (may be null for companies)
     * @param address          residence or contact address (Value Object)
     * @param role             role assigned in the system
     * @param username         username for authentication
     * @param password         password for authentication
     */
    public User(int userId, String relatedId, String fullName,
                String identificationId, Email email, PhoneNumber phoneNumber,
                LocalDate dateOfBirth, Address address, UserRole role,
                String username, String password) {

        validateRequiredFields(fullName, identificationId, username, password);

        this.userId = userId;
        this.relatedId = relatedId;
        this.fullName = fullName;
        this.identificationId = identificationId;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.role = role;
        this.status = UserStatus.ACTIVE; // By default, a new user is active
        this.username = username;
        this.password = password;

        // If the user is an Individual Client, validate legal age
        if (role == UserRole.INDIVIDUAL_CLIENT) {
            validateLegalAge(dateOfBirth);
        }
    }

    // ==================== BUSINESS METHODS ====================

    /**
     * Checks whether the user can perform operations in the system.
     * Only users with ACTIVE status can operate.
     *
     * @return true if the status is ACTIVE
     */
    public boolean canOperate() {
        return status.canOperate();
    }

    /**
     * Checks whether the provided credentials match the user's credentials.
     *
     * @param username username provided
     * @param password password provided
     * @return true if both credentials match
     */
    public boolean authenticate(String username, String password) {
        return this.username.equals(username)
            && this.password.equals(password);
    }

    /**
     * Checks whether the user has a specific role.
     * Useful for authorization checks before each operation.
     *
     * @param requiredRole the role required for the operation
     * @return true if the user has that role
     */
    public boolean hasRole(UserRole requiredRole) {
        return this.role == requiredRole;
    }

    /**
     * Changes the user's status.
     *
     * @param newStatus the new status to assign
     */
    public void changeStatus(UserStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("The new status cannot be null.");
        }
        this.status = newStatus;
    }

    // ==================== PRIVATE VALIDATIONS ====================

    private void validateRequiredFields(String name, String identification,
                                        String user, String pass) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("The full name is required.");
        }
        if (identification == null || identification.isBlank()) {
            throw new IllegalArgumentException("The identification number is required.");
        }
        if (user == null || user.isBlank()) {
            throw new IllegalArgumentException("The username is required.");
        }
        if (pass == null || pass.isBlank()) {
            throw new IllegalArgumentException("The password is required.");
        }
    }

    /**
     * Validates that the person is at least 18 years old.
     * Business rule: Only individuals of legal age can be individual clients.
     *
     * @param dateOfBirth date of birth to validate
     * @throws IllegalArgumentException if the person is under 18
     */
    private void validateLegalAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new IllegalArgumentException(
                "The date of birth is required for Individual Client."
            );
        }
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age < 18) {
            throw new IllegalArgumentException(
                "The client must be of legal age (at least 18 years old). Calculated age: " + age
            );
        }
    }

    // ==================== GETTERS ====================

    public int getUserId() {
        return userId;
    }

    public String getRelatedId() {
        return relatedId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getIdentificationId() {
        return identificationId;
    }

    public Email getEmail() {
        return email;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Address getAddress() {
        return address;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public String getUsername() {
        return username;
    }

    // ==================== EQUALS, HASHCODE, TOSTRING ====================

    /**
     * Two users are equal if they have the same userId.
     * In DDD, entities are compared by identity, not by attributes.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "User{" +
            "userId=" + userId +
            ", fullName='" + fullName + '\'' +
            ", identificationId='" + identificationId + '\'' +
            ", role=" + role +
            ", status=" + status +
            '}';
    }
}
