package com.bank.domain.model.client;

import com.bank.domain.valueobject.Address;
import com.bank.domain.valueobject.Email;
import com.bank.domain.valueobject.PhoneNumber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing a Company Client of the bank.
 *
 * This is the legal entity (corporation) that is a bank client.
 * The legal representative is a reference to an existing IndividualClient.
 *
 * Field mappings:
 * - Company name             → companyName
 * - Tax identification number→ taxId (UNIQUE across the system)
 * - Email address            → email (Value Object)
 * - Phone number             → phoneNumber (Value Object)
 * - Fiscal address           → fiscalAddress (Value Object)
 * - Legal representative     → legalRepresentativeId (reference to IndividualClient)
 * - Role                     → "Company Client" (fixed value)
 *
 * Business rules:
 * - All fields are required.
 * - The tax ID is unique across the entire application.
 * - The legal representative must be a valid Individual Client.
 * - Can delegate permissions to operational users (Company Employees).
 * - Manages the list of authorized employees and supervisors.
 */
public class CompanyClient {

    private final String taxId;
    private String companyName;
    private Email email;
    private PhoneNumber phoneNumber;
    private Address fiscalAddress;
    private String legalRepresentativeId;

    /**
     * List of IDs of operational employees authorized to act
     * on behalf of this company (Company Employees and Supervisors).
     */
    private final List<String> authorizedEmployees;

    /**
     * Creates a new Company Client with all business validations.
     *
     * @param taxId                 tax identification number - UNIQUE
     * @param companyName           legal name of the company
     * @param email                 corporate email address
     * @param phoneNumber           company contact phone number
     * @param fiscalAddress         fiscal address
     * @param legalRepresentativeId identifier of the legal representative (IndividualClient)
     */
    public CompanyClient(String taxId, String companyName, Email email,
                         PhoneNumber phoneNumber, Address fiscalAddress,
                         String legalRepresentativeId) {

        validateTaxId(taxId);
        validateCompanyName(companyName);
        validateLegalRepresentative(legalRepresentativeId);

        if (email == null) {
            throw new IllegalArgumentException("The corporate email address is required.");
        }
        if (phoneNumber == null) {
            throw new IllegalArgumentException("The phone number is required.");
        }
        if (fiscalAddress == null) {
            throw new IllegalArgumentException("The fiscal address is required.");
        }

        this.taxId = taxId.trim();
        this.companyName = companyName.trim();
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.fiscalAddress = fiscalAddress;
        this.legalRepresentativeId = legalRepresentativeId.trim();
        this.authorizedEmployees = new ArrayList<>();
    }

    // ==================== BUSINESS METHODS ====================

    /**
     * Authorizes an employee to operate on behalf of the company.
     * Only the legal representative or a supervisor can delegate permissions.
     *
     * @param employeeId identifier of the employee to authorize
     */
    public void authorizeEmployee(String employeeId) {
        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("The employee ID is required.");
        }
        if (authorizedEmployees.contains(employeeId.trim())) {
            throw new IllegalStateException(
                "The employee with ID '" + employeeId + "' is already authorized for this company."
            );
        }
        authorizedEmployees.add(employeeId.trim());
    }

    /**
     * Revokes an employee's authorization to operate on behalf of the company.
     *
     * @param employeeId identifier of the employee to revoke
     */
    public void revokeEmployee(String employeeId) {
        if (!authorizedEmployees.remove(employeeId.trim())) {
            throw new IllegalStateException(
                "The employee with ID '" + employeeId + "' is not authorized for this company."
            );
        }
    }

    /**
     * Checks whether an employee is authorized to operate on behalf of the company.
     *
     * @param employeeId identifier of the employee to check
     * @return true if the employee is authorized
     */
    public boolean isEmployeeAuthorized(String employeeId) {
        return authorizedEmployees.contains(employeeId.trim());
    }

    /**
     * Updates the company's contact information.
     */
    public void updateContactInfo(Email newEmail, PhoneNumber newPhoneNumber, Address newAddress) {
        if (newEmail != null) this.email = newEmail;
        if (newPhoneNumber != null) this.phoneNumber = newPhoneNumber;
        if (newAddress != null) this.fiscalAddress = newAddress;
    }

    // ==================== PRIVATE VALIDATIONS ====================

    private void validateTaxId(String taxId) {
        if (taxId == null || taxId.isBlank()) {
            throw new IllegalArgumentException("The tax identification number (Tax ID) is required.");
        }
    }

    private void validateCompanyName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("The company name is required.");
        }
    }

    private void validateLegalRepresentative(String representativeId) {
        if (representativeId == null || representativeId.isBlank()) {
            throw new IllegalArgumentException(
                "The legal representative is required. Must be a reference to an IndividualClient."
            );
        }
    }

    // ==================== GETTERS ====================

    public String getTaxId() {
        return taxId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public Email getEmail() {
        return email;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public Address getFiscalAddress() {
        return fiscalAddress;
    }

    public String getLegalRepresentativeId() {
        return legalRepresentativeId;
    }

    /**
     * Returns an immutable list of authorized employees.
     * Returned as immutable to protect aggregate integrity.
     */
    public List<String> getAuthorizedEmployees() {
        return Collections.unmodifiableList(authorizedEmployees);
    }

    public String getRole() {
        return "Company Client";
    }

    // ==================== EQUALS, HASHCODE, TOSTRING ====================

    /**
     * Two CompanyClient instances are equal if they have the same tax ID.
     * Reflects the uniqueness rule of the tax ID.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyClient that = (CompanyClient) o;
        return Objects.equals(taxId, that.taxId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taxId);
    }

    @Override
    public String toString() {
        return "CompanyClient{" +
            "taxId='" + taxId + '\'' +
            ", companyName='" + companyName + '\'' +
            ", email=" + email +
            ", legalRepresentative='" + legalRepresentativeId + '\'' +
            ", authorizedEmployees=" + authorizedEmployees.size() +
            '}';
    }
}
