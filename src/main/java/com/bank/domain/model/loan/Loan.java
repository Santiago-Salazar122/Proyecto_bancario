package com.bank.domain.model.loan;

import com.bank.domain.enums.LoanStatus;
import com.bank.domain.valueobject.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate Root Entity representing a Bank Loan / Credit.
 *
 * Manages the full lifecycle of the loan: request → review → approval/rejection → disbursement.
 * State transitions are controlled by business methods that enforce the strict rules defined.
 *
 * Field mappings:
 * - Loan_ID                  → loanId
 * - Loan_Type                → loanType
 * - Requesting_Client_ID     → requestingClientId
 * - Requested_Amount         → requestedAmount (Value Object Money)
 * - Approved_Amount          → approvedAmount (Value Object Money)
 * - Interest_Rate            → interestRate
 * - Term_Months              → termMonths
 * - Loan_Status              → status (Enum)
 * - Approval_Date            → approvalDate
 * - Disbursement_Date        → disbursementDate
 * - Disbursement_Target_Account → disbursementTargetAccount
 *
 * Approval flow:
 * 1. Created with status UNDER_REVIEW.
 * 2. Only the Internal Analyst can approve (→ APPROVED) or reject (→ REJECTED).
 * 3. Only from APPROVED can the loan be disbursed (→ DISBURSED).
 *
 * Business rules:
 * - The requesting client must be valid and active.
 * - State transitions are strict (see LoanStatus).
 * - For disbursement: the target account must be defined and active.
 * - On disbursement: the Approved_Amount must be > 0 and is credited to the target account.
 */
public class Loan {

    private final int loanId;
    private final String loanType;
    private final String requestingClientId;
    private final Money requestedAmount;
    private Money approvedAmount;
    private BigDecimal interestRate;
    private final int termMonths;
    private LoanStatus status;
    private LocalDateTime approvalDate;
    private LocalDateTime disbursementDate;
    private String disbursementTargetAccount;

    // Audit fields for the approval flow
    private int approverAnalystId;
    private final LocalDateTime creationDate;

    /**
     * Creates a new loan request.
     * The initial status is always UNDER_REVIEW.
     *
     * @param loanId              unique identifier of the loan
     * @param loanType            category of the loan (Personal, Mortgage, etc.)
     * @param requestingClientId  identifier of the requesting client
     * @param requestedAmount     amount of money requested
     * @param termMonths          duration of the loan in months
     */
    public Loan(int loanId, String loanType, String requestingClientId,
                Money requestedAmount, int termMonths) {

        validateLoanId(loanId);
        validateLoanType(loanType);
        validateClient(requestingClientId);
        validateRequestedAmount(requestedAmount);
        validateTerm(termMonths);

        this.loanId = loanId;
        this.loanType = loanType.trim();
        this.requestingClientId = requestingClientId.trim();
        this.requestedAmount = requestedAmount;
        this.termMonths = termMonths;
        this.status = LoanStatus.UNDER_REVIEW; // Mandatory initial status
        this.creationDate = LocalDateTime.now();
    }

    // ==================== BUSINESS METHODS (APPROVAL FLOW) ====================

    /**
     * Approves the loan. Can only be executed by an Internal Analyst.
     *
     * Transition: UNDER_REVIEW → APPROVED
     *
     * @param approvedAmount        amount approved by the bank (may be <= requestedAmount)
     * @param interestRate          annual interest rate defined
     * @param analystId             ID of the Internal Analyst approving
     * @param targetAccount         account number where the loan will be disbursed
     * @throws IllegalStateException if the loan is not UNDER_REVIEW
     */
    public void approve(Money approvedAmount, BigDecimal interestRate,
                        int analystId, String targetAccount) {

        validateTransition(LoanStatus.APPROVED);

        if (approvedAmount == null || !approvedAmount.isGreaterThanZero()) {
            throw new IllegalArgumentException("The approved amount must be greater than zero.");
        }
        if (interestRate == null || interestRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("The interest rate must be greater than zero.");
        }

        this.approvedAmount = approvedAmount;
        this.interestRate = interestRate;
        this.approverAnalystId = analystId;
        this.disbursementTargetAccount = (targetAccount != null) ? targetAccount.trim() : null;
        this.approvalDate = LocalDateTime.now();
        this.status = LoanStatus.APPROVED;
    }

    /**
     * Rejects the loan. Can only be executed by an Internal Analyst.
     *
     * Transition: UNDER_REVIEW → REJECTED
     *
     * @param analystId ID of the Internal Analyst rejecting
     * @throws IllegalStateException if the loan is not UNDER_REVIEW
     */
    public void reject(int analystId) {
        validateTransition(LoanStatus.REJECTED);

        this.approverAnalystId = analystId;
        this.approvalDate = LocalDateTime.now(); // Date of the decision
        this.status = LoanStatus.REJECTED;
    }

    /**
     * Marks the loan as disbursed. Only possible from APPROVED status.
     *
     * Transition: APPROVED → DISBURSED
     *
     * IMPORTANT: This method only changes the loan status.
     * The crediting of funds to the target account must be done
     * in the service layer, since it involves another entity (BankAccount).
     *
     * Validations:
     * - The target account must be defined.
     * - The approved amount must be > 0.
     *
     * @throws IllegalStateException if the loan is not APPROVED or the target account is missing
     */
    public void disburse() {
        validateTransition(LoanStatus.DISBURSED);

        if (disbursementTargetAccount == null || disbursementTargetAccount.isBlank()) {
            throw new IllegalStateException(
                "Cannot disburse loan " + loanId
                + ": the disbursement target account is not defined."
            );
        }

        if (approvedAmount == null || !approvedAmount.isGreaterThanZero()) {
            throw new IllegalStateException(
                "Cannot disburse loan " + loanId
                + ": the approved amount must be greater than zero."
            );
        }

        this.disbursementDate = LocalDateTime.now();
        this.status = LoanStatus.DISBURSED;
    }

    /**
     * Assigns the target account where the loan will be disbursed.
     * Can be assigned at the time of the request or before disbursement.
     *
     * @param accountNumber target account number
     */
    public void assignTargetAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("The target account number is required.");
        }
        this.disbursementTargetAccount = accountNumber.trim();
    }

    // ==================== PRIVATE VALIDATIONS ====================

    /**
     * Validates that the state transition is allowed according to business rules.
     */
    private void validateTransition(LoanStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                "State transition not allowed for loan " + loanId
                + ": " + status.getDescription() + " → " + newStatus.getDescription()
            );
        }
    }

    private void validateLoanId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("The loan ID must be greater than zero.");
        }
    }

    private void validateLoanType(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("The loan type is required.");
        }
    }

    private void validateClient(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("The requesting client ID is required.");
        }
    }

    private void validateRequestedAmount(Money amount) {
        if (amount == null || !amount.isGreaterThanZero()) {
            throw new IllegalArgumentException("The requested loan amount must be greater than zero.");
        }
    }

    private void validateTerm(int term) {
        if (term <= 0) {
            throw new IllegalArgumentException("The loan term must be greater than zero months.");
        }
    }

    // ==================== GETTERS ====================

    public int getLoanId() {
        return loanId;
    }

    public String getLoanType() {
        return loanType;
    }

    public String getRequestingClientId() {
        return requestingClientId;
    }

    public Money getRequestedAmount() {
        return requestedAmount;
    }

    public Money getApprovedAmount() {
        return approvedAmount;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public int getTermMonths() {
        return termMonths;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public LocalDateTime getDisbursementDate() {
        return disbursementDate;
    }

    public String getDisbursementTargetAccount() {
        return disbursementTargetAccount;
    }

    public int getApproverAnalystId() {
        return approverAnalystId;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    // ==================== EQUALS, HASHCODE, TOSTRING ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loan loan = (Loan) o;
        return loanId == loan.loanId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(loanId);
    }

    @Override
    public String toString() {
        return "Loan{" +
            "loanId=" + loanId +
            ", loanType='" + loanType + '\'' +
            ", clientId='" + requestingClientId + '\'' +
            ", requestedAmount=" + requestedAmount +
            ", approvedAmount=" + approvedAmount +
            ", status=" + status +
            '}';
    }
}
