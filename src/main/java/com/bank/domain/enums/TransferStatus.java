package com.bank.domain.enums;

/**
 * Estados del ciclo de vida de una transferencia.
 *
 * Flujo monto bajo (≤ umbral):
 *   → EXECUTED directamente
 *
 * Flujo empresa monto alto (> umbral):
 *   PENDING_APPROVAL → EXECUTED  (aprobada por COMPANY_SUPERVISOR)
 *   PENDING_APPROVAL → REJECTED  (rechazada por COMPANY_SUPERVISOR)
 *   PENDING_APPROVAL → EXPIRED   (automático si pasan 60+ minutos)
 */
public enum TransferStatus {

    EXECUTED("Executed"),
    PENDING_APPROVAL("Pending approval"),
    REJECTED("Rejected"),
    EXPIRED("Expired");

    private final String description;
    TransferStatus(String description) { this.description = description; }
    public String getDescription() { return description; }

    public boolean canTransitionTo(TransferStatus newStatus) {
        if (this == PENDING_APPROVAL)
            return newStatus == EXECUTED || newStatus == REJECTED || newStatus == EXPIRED;
        return false;
    }
}
