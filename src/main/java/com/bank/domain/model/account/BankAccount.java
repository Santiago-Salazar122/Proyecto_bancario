package com.bank.domain.model.account;

import com.bank.domain.enums.AccountStatus;
import com.bank.domain.enums.AccountType;
import com.bank.domain.valueobject.Money;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Aggregate Root Entity representing a Bank Account.
 *
 * This is the client's deposit product. It manages its own balance
 * and applies business rules about allowed operations.
 *
 * Field mappings:
 * - Account_Number  → accountNumber (UNIQUE)
 * - Account_Type    → accountType (Enum)
 * - Owner_ID        → ownerId (reference to the client)
 * - Current_Balance → balance (Value Object Money)
 * - Currency        → currency
 * - Account_Status  → status (Enum)
 * - Opening_Date    → openingDate
 *
 * Business rules:
 * - Account_Number must be unique.
 * - An account cannot be opened for an Inactive or Blocked client
 *   (validated in the service layer, not here, since it does not know the user).
 * - Operations are not allowed on BLOCKED or CANCELLED accounts.
 * - The balance is updated ONLY through controlled business methods.
 * - The Current_Balance is managed in the Relational DB (NOT in the Audit Log).
 */
public class BankAccount {

    private final String accountNumber;
    private final AccountType accountType;
    private final String ownerId;
    private Money balance;
    private final String currency;
    private AccountStatus status;
    private final LocalDate openingDate;

    /**
     * Creates a new Bank Account.
     *
     * @param accountNumber unique identifier for the account
     * @param accountType   type of account (Savings, Checking, Personal, Business)
     * @param ownerId       identifier of the account owner
     * @param currency      currency in which the account operates (e.g. "USD", "EUR")
     */
    public BankAccount(String accountNumber, AccountType accountType,
                       String ownerId, String currency) {

        validateAccountNumber(accountNumber);
        validateOwner(ownerId);
        validateCurrency(currency);

        if (accountType == null) {
            throw new IllegalArgumentException("The account type is required.");
        }

        this.accountNumber = accountNumber.trim();
        this.accountType = accountType;
        this.ownerId = ownerId.trim();
        this.balance = Money.ZERO; // Every new account starts with $0.00 balance
        this.currency = currency.trim().toUpperCase();
        this.status = AccountStatus.ACTIVE; // Every new account starts as active
        this.openingDate = LocalDate.now();
    }

    // ==================== BUSINESS METHODS ====================

    /**
     * Credits (deposits) an amount into the account.
     * Used in: loan disbursements, incoming transfers, teller deposits.
     *
     * @param amount the amount to credit (must be > 0)
     * @throws IllegalStateException    if the account does not allow operations
     * @throws IllegalArgumentException if the amount is not greater than zero
     */
    public void credit(Money amount) {
        validateAccountOperational("credit funds");
        validatePositiveAmount(amount);
        this.balance = this.balance.add(amount);
    }

    /**
     * Debits (withdraws) an amount from the account.
     * Used in: outgoing transfers, teller withdrawals.
     *
     * @param amount the amount to debit (must be > 0)
     * @throws IllegalStateException    if the account does not allow operations or has insufficient funds
     * @throws IllegalArgumentException if the amount is not greater than zero
     */
    public void debit(Money amount) {
        validateAccountOperational("debit funds");
        validatePositiveAmount(amount);
        validateSufficientBalance(amount);
        this.balance = this.balance.subtract(amount);
    }

    /**
     * Checks whether the account has sufficient balance for an operation.
     *
     * @param amount the amount to be debited
     * @return true if the current balance is >= the amount
     */
    public boolean hasSufficientBalance(Money amount) {
        return balance.isGreaterThanOrEqualTo(amount);
    }

    /**
     * Checks whether the account allows transactional operations.
     *
     * @return true if the status is ACTIVE
     */
    public boolean isOperational() {
        return status.allowsOperations();
    }

    /**
     * Blocks the account. No more transactional operations are allowed.
     */
    public void block() {
        if (status == AccountStatus.CANCELLED) {
            throw new IllegalStateException("A cancelled account cannot be blocked.");
        }
        this.status = AccountStatus.BLOCKED;
    }

    /**
     * Permanently cancels the account.
     */
    public void cancel() {
        this.status = AccountStatus.CANCELLED;
    }

    /**
     * Reactivates a blocked account.
     */
    public void reactivate() {
        if (status == AccountStatus.CANCELLED) {
            throw new IllegalStateException("A cancelled account cannot be reactivated.");
        }
        this.status = AccountStatus.ACTIVE;
    }

    // ==================== PRIVATE VALIDATIONS ====================

    private void validateAccountNumber(String number) {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("The account number is required.");
        }
    }

    private void validateOwner(String ownerId) {
        if (ownerId == null || ownerId.isBlank()) {
            throw new IllegalArgumentException("The owner ID is required.");
        }
    }

    private void validateCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("The currency is required.");
        }
    }

    /**
     * Validates that the account is in an operational state before allowing a transaction.
     */
    private void validateAccountOperational(String operation) {
        if (!isOperational()) {
            throw new IllegalStateException(
                "Cannot " + operation + " on account " + accountNumber
                + ". Current status: " + status.getDescription()
            );
        }
    }

    private void validatePositiveAmount(Money amount) {
        if (amount == null || !amount.isGreaterThanZero()) {
            throw new IllegalArgumentException("The amount must be greater than zero.");
        }
    }

    /**
     * Validates that the account has sufficient balance to debit the given amount.
     */
    private void validateSufficientBalance(Money amount) {
        if (!hasSufficientBalance(amount)) {
            throw new IllegalStateException(
                "Insufficient balance on account " + accountNumber
                + ". Current balance: " + balance + ", Requested amount: " + amount
            );
        }
    }

    // ==================== GETTERS ====================

    public String getAccountNumber() {
        return accountNumber;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public Money getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public LocalDate getOpeningDate() {
        return openingDate;
    }

    // ==================== EQUALS, HASHCODE, TOSTRING ====================

    /**
     * Two BankAccount instances are equal if they have the same account number.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankAccount that = (BankAccount) o;
        return Objects.equals(accountNumber, that.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber);
    }

    @Override
    public String toString() {
        return "BankAccount{" +
            "accountNumber='" + accountNumber + '\'' +
            ", accountType=" + accountType +
            ", ownerId='" + ownerId + '\'' +
            ", balance=" + balance +
            ", currency='" + currency + '\'' +
            ", status=" + status +
            ", openingDate=" + openingDate +
            '}';
    }
}
