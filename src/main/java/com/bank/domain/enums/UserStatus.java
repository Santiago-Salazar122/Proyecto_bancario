package com.bank.domain.enums;

/**
 * Estado del usuario en el sistema.
 * Solo ACTIVE puede realizar operaciones.
 */
public enum UserStatus {

    ACTIVE("Active"),
    INACTIVE("Inactive"),
    BLOCKED("Blocked");

    private final String description;

    UserStatus(String description) { this.description = description; }
    public String getDescription() { return description; }

    /** Solo los usuarios ACTIVE pueden operar (abrir cuentas, transferir, etc.). */
    public boolean canOperate() { return this == ACTIVE; }
}
