package com.bank.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object representing a monetary amount.
 *
 * Immutable: once created, it cannot be modified. Arithmetic operations
 * return new instances.
 *
 * Uses BigDecimal internally to avoid precision issues with decimals
 * (double and float are unsuitable for representing money).
 *
 * Validations:
 * - The amount cannot be null.
 * - For operations that require it, it is validated to be greater than zero.
 *
 * Scale: 2 decimal places with HALF_UP rounding (standard bank rounding).
 */
public class Money {

    public static final Money ZERO = new Money(BigDecimal.ZERO);
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final BigDecimal amount;

    /**
     * Creates a Money instance from a BigDecimal.
     *
     * @param amount the monetary quantity
     * @throws IllegalArgumentException if the amount is null
     */
    public Money(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("The amount cannot be null.");
        }
        this.amount = amount.setScale(SCALE, ROUNDING);
    }

    /**
     * Creates a Money instance from a double value.
     * Convenience for creation from numeric literals.
     *
     * @param amount the monetary quantity as a double
     */
    public Money(double amount) {
        this(BigDecimal.valueOf(amount));
    }

    /**
     * Creates a Money instance from a String.
     *
     * @param amount the monetary quantity as text
     * @throws IllegalArgumentException if the text is not a valid number
     */
    public Money(String amount) {
        this(new BigDecimal(amount));
    }

    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Adds another Money to this one and returns a new instance.
     *
     * @param other the amount to add
     * @return new instance with the result
     */
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    /**
     * Subtracts another Money from this one and returns a new instance.
     *
     * @param other the amount to subtract
     * @return new instance with the result
     */
    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }

    /**
     * Checks whether this amount is strictly greater than zero.
     * Business rule: amounts for transfers and loans must be > 0.
     */
    public boolean isGreaterThanZero() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Checks whether this amount is greater than or equal to another.
     * Useful for validating fund availability (balance >= amount to transfer).
     *
     * @param other the amount to compare against
     * @return true if this amount is >= other
     */
    public boolean isGreaterThanOrEqualTo(Money other) {
        return this.amount.compareTo(other.amount) >= 0;
    }

    /**
     * Checks whether this amount is strictly greater than another.
     * Useful for validating transfer approval thresholds.
     *
     * @param other the amount to compare against
     * @return true if this amount is > other
     */
    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * Checks whether this amount is negative.
     * A balance should never be negative unless an authorized overdraft is in place.
     */
    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return "$" + amount.toPlainString();
    }
}
