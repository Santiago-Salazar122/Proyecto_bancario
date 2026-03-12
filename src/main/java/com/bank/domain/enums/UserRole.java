package com.bank.domain.enums;

/**
 * Defines the roles a user can have within the banking system.
 * Each role determines the visibility of information and allowed operations.
 *
 * - INDIVIDUAL_CLIENT: Individual bank user.
 * - COMPANY_CLIENT: Legal representative or administrator of a client company.
 * - TELLER_EMPLOYEE: Cashier or service advisor (direct contact with the public).
 * - COMMERCIAL_EMPLOYEE: Products advisor / account executive.
 * - COMPANY_EMPLOYEE: Operational user authorized by a client company.
 * - COMPANY_SUPERVISOR: Approver of critical operations for a company.
 * - INTERNAL_ANALYST: Risk, compliance and back-office staff of the bank.
 */
public enum UserRole {

    INDIVIDUAL_CLIENT("Individual Client"),
    COMPANY_CLIENT("Company Client"),
    TELLER_EMPLOYEE("Teller Employee"),
    COMMERCIAL_EMPLOYEE("Commercial Employee"),
    COMPANY_EMPLOYEE("Company Employee"),
    COMPANY_SUPERVISOR("Company Supervisor"),
    INTERNAL_ANALYST("Internal Bank Analyst");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
