package com.bank.domain.model.client;

import com.bank.domain.valueobject.Address;
import com.bank.domain.valueobject.Email;
import com.bank.domain.valueobject.PhoneNumber;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * Entity representing an Individual Client of the bank.
 *
 * This is the natural person who is the holder or beneficiary of banking products.
 * This is an Aggregate Root within the Clients Bounded Context.
 *
 * Field mappings:
 * - Full name              → fullName
 * - Identification number  → identificationNumber (UNIQUE across the system)
 * - Email address          → email (Value Object)
 * - Phone number           → phoneNumber (Value Object)
 * - Date of birth          → dateOfBirth (must be at least 18 years old)
 * - Address                → address (Value Object)
 * - Role                   → "Individual Client" (fixed value)
 *
 * Business rules:
 * - All fields are required.
 * - Identification number is unique across the entire application (validated in service/repo).
 * - Must be at least 18 years old.
 * - Can only view/operate on their own products.
 * - Cannot view other clients' information or business accounts.
 */
public class IndividualClient {

    private static final int MINIMUM_AGE = 18;

    private final String identificationNumber;
    private String fullName;
    private Email email;
    private PhoneNumber phoneNumber;
    private LocalDate dateOfBirth;
    private Address address;

    /**
     * Creates a new Individual Client with all business validations.
     *
     * @param identificationNumber national ID or equivalent (UNIQUE)
     * @param fullName             first and last name of the person
     * @param email                contact email address
     * @param phoneNumber          contact phone number
     * @param dateOfBirth          date of birth (must be at least 18 years old)
     * @param address              registered address
     */
    public IndividualClient(String identificationNumber, String fullName,
                            Email email, PhoneNumber phoneNumber,
                            LocalDate dateOfBirth, Address address) {

        validateIdentificationNumber(identificationNumber);
        validateFullName(fullName);
        validateDateOfBirth(dateOfBirth);

        if (email == null) {
            throw new IllegalArgumentException("The email address is required.");
        }
        if (phoneNumber == null) {
            throw new IllegalArgumentException("The phone number is required.");
        }
        if (address == null) {
            throw new IllegalArgumentException("The address is required.");
        }

        this.identificationNumber = identificationNumber.trim();
        this.fullName = fullName.trim();
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    // ==================== BUSINESS METHODS ====================

    /**
     * Calculates the client's current age in years.
     *
     * @return age in years
     */
    public int calculateAge() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    /**
     * Checks whether the client is of legal age.
     *
     * @return true if at least 18 years old
     */
    public boolean isOfLegalAge() {
        return calculateAge() >= MINIMUM_AGE;
    }

    /**
     * Updates the client's contact information.
     *
     * @param newEmail       new email address
     * @param newPhoneNumber new phone number
     * @param newAddress     new address
     */
    public void updateContactInfo(Email newEmail, PhoneNumber newPhoneNumber, Address newAddress) {
        if (newEmail != null) this.email = newEmail;
        if (newPhoneNumber != null) this.phoneNumber = newPhoneNumber;
        if (newAddress != null) this.address = newAddress;
    }

    // ==================== PRIVATE VALIDATIONS ====================

    private void validateIdentificationNumber(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("The identification number is required.");
        }
    }

    private void validateFullName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("The full name is required.");
        }
    }

    /**
     * Validates that the date of birth is not null and that the client is at least 18 years old.
     */
    private void validateDateOfBirth(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("The date of birth is required.");
        }
        int age = Period.between(date, LocalDate.now()).getYears();
        if (age < MINIMUM_AGE) {
            throw new IllegalArgumentException(
                "The client must be of legal age (at least " + MINIMUM_AGE
                + " years old). Calculated age: " + age + " years."
            );
        }
    }

    // ==================== GETTERS ====================

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public String getFullName() {
        return fullName;
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

    public String getRole() {
        return "Individual Client";
    }

    // ==================== EQUALS, HASHCODE, TOSTRING ====================

    /**
     * Two IndividualClient instances are equal if they have the same identification number.
     * This reflects the business rule of identification uniqueness.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndividualClient that = (IndividualClient) o;
        return Objects.equals(identificationNumber, that.identificationNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identificationNumber);
    }

    @Override
    public String toString() {
        return "IndividualClient{" +
            "identificationNumber='" + identificationNumber + '\'' +
            ", fullName='" + fullName + '\'' +
            ", email=" + email +
            ", phoneNumber=" + phoneNumber +
            ", dateOfBirth=" + dateOfBirth +
            '}';
    }
}
