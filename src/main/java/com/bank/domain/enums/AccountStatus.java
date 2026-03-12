package com.bank.domain.enums;

/**
 * Operational states of a bank account.
 *
 * - ACTIVE: The account can send and receive funds normally.
 * - BLOCKED: The account does not allow operations (transfers, withdrawals).
 * - CANCELLED: The account has been permanently closed.
 *
 * Business rule: Operations (transfers, withdrawals) are not allowed on accounts
 * with status BLOCKED or CANCELLED, except for internal closing processes.
 */
public enum AccountStatus {

    ACTIVE("Active"),
    BLOCKED("Blocked"),
    CANCELLED("Cancelled");

    private final String description;

    AccountStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Indicates whether the account allows transactional operations (transfers, withdrawals, deposits).
     * Only ACTIVE accounts allow operations.
     */
    public boolean allowsOperations() {
        return this == ACTIVE;
    }
}
