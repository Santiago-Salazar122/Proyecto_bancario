package com.bank.domain.enums;

/**
 * Types of bank account offered by the bank.
 *
 * - SAVINGS: Personal savings account.
 * - CHECKING: Checking account (allows overdrafts per rules).
 * - PERSONAL: Generic personal-use account.
 * - BUSINESS: Account associated with a client company.
 */
public enum AccountType {

    SAVINGS("Savings"),
    CHECKING("Checking"),
    PERSONAL("Personal"),
    BUSINESS("Business");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
