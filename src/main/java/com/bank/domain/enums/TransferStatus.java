package com.bank.domain.enums;

/**
 * States of the lifecycle of a bank transfer.
 *
 * Flow for transfers that do NOT require approval:
 *   → EXECUTED (directly upon creation)
 *
 * Flow for business transfers that exceed the threshold:
 *   PENDING_APPROVAL → EXECUTED
 *   PENDING_APPROVAL → REJECTED
 *   PENDING_APPROVAL → EXPIRED (automatic after 60 minutes)
 *
 * - EXECUTED: The transfer completed successfully (balances updated).
 * - PENDING_APPROVAL: Awaiting authorization by the Company Supervisor.
 * - REJECTED: The Company Supervisor rejected the transfer.
 * - EXPIRED: More than 60 minutes passed without approval.
 */
public enum TransferStatus {

    EXECUTED("Executed"),
    PENDING_APPROVAL("Pending approval"),
    REJECTED("Rejected"),
    EXPIRED("Expired");

    private final String description;

    TransferStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Validates whether the state transition is allowed according to business rules.
     *
     * Valid transitions:
     *   PENDING_APPROVAL → EXECUTED
     *   PENDING_APPROVAL → REJECTED
     *   PENDING_APPROVAL → EXPIRED
     *
     * @param newStatus the status to transition to
     * @return true if the transition is valid, false otherwise
     */
    public boolean canTransitionTo(TransferStatus newStatus) {
        if (this == PENDING_APPROVAL) {
            return newStatus == EXECUTED
                || newStatus == REJECTED
                || newStatus == EXPIRED;
        }
        return false; // EXECUTED, REJECTED and EXPIRED are terminal states
    }
}
