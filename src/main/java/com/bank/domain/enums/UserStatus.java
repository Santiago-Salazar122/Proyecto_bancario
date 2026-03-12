package com.bank.domain.enums;

/**
 * Represents the status of a user within the system.
 *
 * - ACTIVE: The user can operate normally.
 * - INACTIVE: The user cannot perform operations nor open products.
 * - BLOCKED: The user has been blocked (for security, non-compliance, etc.).
 *
 * Business rule: A bank account cannot be opened for a client whose status is
 * INACTIVE or BLOCKED.
 */
public enum UserStatus {

    ACTIVE("Active"),
    INACTIVE("Inactive"),
    BLOCKED("Blocked");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Indicates whether the user can operate (open accounts, request loans, etc.).
     * Only ACTIVE users can operate.
     */
    public boolean canOperate() {
        return this == ACTIVE;
    }
}
