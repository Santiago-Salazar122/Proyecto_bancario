package com.bank.domain.enums;

/**
 * States of the lifecycle of a loan / credit.
 *
 * Valid transition flow:
 *   UNDER_REVIEW → APPROVED → DISBURSED
 *   UNDER_REVIEW → REJECTED
 *
 * - UNDER_REVIEW: Request registered, pending review by the Internal Analyst.
 * - APPROVED: The Internal Analyst has approved the loan.
 * - REJECTED: The Internal Analyst has rejected the loan.
 * - DISBURSED: The approved amount has been credited to the client's target account.
 *
 * Business rules:
 * - Only the Internal Analyst can change from UNDER_REVIEW to APPROVED or REJECTED.
 * - The loan can only move to DISBURSED from the APPROVED state.
 */
public enum LoanStatus {

    UNDER_REVIEW("Under review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    DISBURSED("Disbursed");

    private final String description;

    LoanStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Validates whether the state transition is allowed according to business rules.
     *
     * Valid transitions:
     *   UNDER_REVIEW → APPROVED
     *   UNDER_REVIEW → REJECTED
     *   APPROVED     → DISBURSED
     *
     * @param newStatus the status to transition to
     * @return true if the transition is valid, false otherwise
     */
    public boolean canTransitionTo(LoanStatus newStatus) {
        return switch (this) {
            case UNDER_REVIEW -> newStatus == APPROVED || newStatus == REJECTED;
            case APPROVED -> newStatus == DISBURSED;
            case REJECTED, DISBURSED -> false; // Terminal states
        };
    }
}
