package com.bank.domain.enums;

/**
 * Roles que puede tener un usuario dentro del sistema bancario.
 * El rol determina qué puede ver y qué operaciones puede realizar.
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

    UserRole(String description) { this.description = description; }
    public String getDescription() { return description; }
}
