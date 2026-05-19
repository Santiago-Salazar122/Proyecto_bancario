package com.bank.domain.enums;

/**
 * Estados del ciclo de vida de un préstamo.
 *
 * Flujo permitido:
 *   UNDER_REVIEW → APPROVED  (solo INTERNAL_ANALYST)
 *   UNDER_REVIEW → REJECTED  (solo INTERNAL_ANALYST)
 *   APPROVED     → DISBURSED (solo INTERNAL_ANALYST, con cuenta destino válida)
 *
 * REJECTED y DISBURSED son estados terminales.
 */
public enum LoanStatus {

    UNDER_REVIEW("Under review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    DISBURSED("Disbursed");

    private final String description;
    LoanStatus(String description) { this.description = description; }
    public String getDescription() { return description; }

    public boolean canTransitionTo(LoanStatus newStatus) {
        return switch (this) {
            case UNDER_REVIEW -> newStatus == APPROVED || newStatus == REJECTED;
            case APPROVED     -> newStatus == DISBURSED;
            case REJECTED, DISBURSED -> false;
        };
    }
}
