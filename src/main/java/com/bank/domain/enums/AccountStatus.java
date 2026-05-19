package com.bank.domain.enums;

/**
 * Estado operativo de una cuenta bancaria.
 * Solo ACTIVE permite depósitos, retiros y transferencias.
 */
public enum AccountStatus {

    ACTIVE("Active"),
    BLOCKED("Blocked"),
    CANCELLED("Cancelled");

    private final String description;
    AccountStatus(String description) { this.description = description; }
    public String getDescription() { return description; }

    /** Solo las cuentas ACTIVE permiten operaciones transaccionales. */
    public boolean allowsOperations() { return this == ACTIVE; }
}
