package com.bank.domain.model.account;

import com.bank.domain.enums.AccountStatus;
import com.bank.domain.enums.AccountType;
import com.bank.domain.valueobject.Money;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad raíz que representa una Cuenta Bancaria.
 * Se mapea a la tabla "bank_accounts" en MySQL.
 *
 * El saldo (balance) SIEMPRE se gestiona en MySQL.
 * NUNCA se usa la Bitácora de MongoDB para calcular saldos.
 *
 * Reglas de negocio:
 * - No se pueden hacer operaciones en cuentas BLOCKED o CANCELLED.
 * - El saldo se modifica SOLO mediante credit() y debit().
 * - No se puede debitar más de lo que hay en el saldo.
 */
@Entity
@Table(name = "bank_accounts")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Número de cuenta. ÚNICO en toda la base de datos. */
    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    /** Identificación del titular (CC o NIT). */
    @Column(name = "owner_id", nullable = false, length = 20)
    private String ownerId;

    /**
     * Saldo almacenado como BigDecimal en MySQL (precisión monetaria exacta).
     * BigDecimal evita errores de redondeo que tiene double/float.
     */
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "currency", nullable = false, length = 5)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status;

    @Column(name = "opening_date", nullable = false)
    private LocalDate openingDate;

    protected BankAccount() {}

    /**
     * Crea una nueva cuenta bancaria con saldo cero.
     */
    public BankAccount(String accountNumber, AccountType accountType,
                       String ownerId, String currency) {
        if (accountNumber == null || accountNumber.isBlank())
            throw new IllegalArgumentException("The account number is required.");
        if (ownerId == null || ownerId.isBlank())
            throw new IllegalArgumentException("The owner ID is required.");
        if (currency == null || currency.isBlank())
            throw new IllegalArgumentException("The currency is required.");
        if (accountType == null)
            throw new IllegalArgumentException("The account type is required.");

        this.accountNumber = accountNumber.trim();
        this.accountType = accountType;
        this.ownerId = ownerId.trim();
        this.balance = BigDecimal.ZERO;   // Toda cuenta nueva inicia con saldo 0
        this.currency = currency.trim().toUpperCase();
        this.status = AccountStatus.ACTIVE; // Toda cuenta nueva inicia ACTIVA
        this.openingDate = LocalDate.now();
    }

    // ═══════════════════ MÉTODOS DE NEGOCIO ═══════════════════

    /**
     * Acredita (deposita) un monto en la cuenta.
     * Usado en: desembolsos, transferencias entrantes, depósitos.
     */
    public void credit(Money amount) {
        validateAccountOperational("credit funds");
        validatePositiveAmount(amount);
        this.balance = this.balance.add(amount.getAmount());
    }

    /**
     * Debita (retira) un monto de la cuenta.
     * Usado en: transferencias salientes, retiros.
     */
    public void debit(Money amount) {
        validateAccountOperational("debit funds");
        validatePositiveAmount(amount);
        validateSufficientBalance(amount);
        this.balance = this.balance.subtract(amount.getAmount());
    }

    public boolean hasSufficientBalance(Money amount) {
        return getBalance().isGreaterThanOrEqualTo(amount);
    }

    public boolean isOperational() { return status.allowsOperations(); }

    /** Bloquea la cuenta. Solo se puede bloquear si está ACTIVE. */
    public void block() {
        if (status == AccountStatus.CANCELLED)
            throw new IllegalStateException("A cancelled account cannot be blocked.");
        this.status = AccountStatus.BLOCKED;
    }

    /** Cancela permanentemente la cuenta. Estado terminal. */
    public void cancel() { this.status = AccountStatus.CANCELLED; }

    /** Reactiva una cuenta bloqueada. */
    public void reactivate() {
        if (status == AccountStatus.CANCELLED)
            throw new IllegalStateException("A cancelled account cannot be reactivated.");
        this.status = AccountStatus.ACTIVE;
    }

    // ═══════════════════ VALIDACIONES PRIVADAS ═══════════════════

    private void validateAccountOperational(String operation) {
        if (!isOperational())
            throw new IllegalStateException(
                "Cannot " + operation + " on account " + accountNumber
                + ". Status: " + status.getDescription());
    }

    private void validatePositiveAmount(Money amount) {
        if (amount == null || !amount.isGreaterThanZero())
            throw new IllegalArgumentException("The amount must be greater than zero.");
    }

    private void validateSufficientBalance(Money amount) {
        if (!hasSufficientBalance(amount))
            throw new IllegalStateException(
                "Insufficient balance on account " + accountNumber
                + ". Balance: $" + balance + ", Requested: $" + amount.getAmount());
    }

    // ═══════════════════ GETTERS ═══════════════════

    public Long getId()               { return id; }
    public String getAccountNumber()  { return accountNumber; }
    public AccountType getAccountType() { return accountType; }
    public String getOwnerId()        { return ownerId; }
    public String getCurrency()       { return currency; }
    public AccountStatus getStatus()  { return status; }
    public LocalDate getOpeningDate() { return openingDate; }

    /** Saldo como Value Object Money (para lógica de dominio). */
    public Money getBalance()         { return new Money(this.balance); }

    /** Saldo como BigDecimal (para JSON / JPA). */
    public BigDecimal getBalanceAmount() { return balance; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(accountNumber, ((BankAccount) o).accountNumber);
    }

    @Override public int hashCode() { return Objects.hash(accountNumber); }

    @Override
    public String toString() {
        return "BankAccount{accountNumber='" + accountNumber + '\''
            + ", accountType=" + accountType + ", ownerId='" + ownerId + '\''
            + ", balance=$" + balance + ", status=" + status + '}';
    }
}
