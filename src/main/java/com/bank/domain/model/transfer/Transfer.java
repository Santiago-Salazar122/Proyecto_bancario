package com.bank.domain.model.transfer;

import com.bank.domain.enums.TransferStatus;
import com.bank.domain.valueobject.Money;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate Root Entity representing a Bank Transfer.
 *
 * Manages the movement of funds between accounts, including the approval flow
 * for high-value business transfers.
 *
 * Field mappings:
 * - Transfer_ID           → transferId
 * - Source_Account        → sourceAccount
 * - Target_Account        → targetAccount
 * - Amount                → amount (Value Object Money)
 * - Creation_Date         → creationDate
 * - Approval_Date         → approvalDate
 * - Transfer_Status       → status (Enum)
 * - Creator_User_ID       → creatorUserId
 * - Approver_User_ID      → approverUserId
 *
 * Approval flow (high-value business transfers):
 * 1. If amount > threshold → PENDING_APPROVAL
 * 2. If amount <= threshold → EXECUTED directly
 * 3. From PENDING_APPROVAL:
 *    - Supervisor approves → validates balance → EXECUTED
 *    - Supervisor rejects → REJECTED
 *    - 60+ minutes pass → EXPIRED (automatic)
 *
 * Business rules:
 * - The amount must be > 0.
 * - The source account cannot be blocked or cancelled (validated in service).
 * - Sufficient balance in the source account must be validated before execution.
 * - On execution: debit source account, credit target account (in service).
 * - Each execution/approval/rejection/expiry is recorded in the Audit Log.
 */
public class Transfer {

    /**
     * Maximum time (in minutes) a transfer can remain in PENDING_APPROVAL
     * before expiring automatically.
     */
    private static final long EXPIRY_MINUTES = 60;

    private final int transferId;
    private final String sourceAccount;
    private final String targetAccount;
    private final Money amount;
    private final LocalDateTime creationDate;
    private LocalDateTime approvalDate;
    private TransferStatus status;
    private final int creatorUserId;
    private int approverUserId;

    /**
     * Creates a new transfer.
     *
     * The initial status is determined by whether approval is required:
     * - If requiresApproval = true  → PENDING_APPROVAL
     * - If requiresApproval = false → EXECUTED
     *
     * NOTE: The determination of whether approval is required (comparison with threshold)
     * is performed in the service layer, which knows the business rules
     * about thresholds and client type.
     *
     * @param transferId        unique identifier
     * @param sourceAccount     account number from which the money is sent
     * @param targetAccount     account number to which the money is sent
     * @param amount            amount to transfer (must be > 0)
     * @param creatorUserId     ID of the user initiating the transfer
     * @param requiresApproval  true if the transfer exceeds the business threshold
     */
    public Transfer(int transferId, String sourceAccount, String targetAccount,
                    Money amount, int creatorUserId, boolean requiresApproval) {

        validateId(transferId);
        validateAccount(sourceAccount, "source");
        validateAccount(targetAccount, "target");
        validateAmount(amount);

        this.transferId = transferId;
        this.sourceAccount = sourceAccount.trim();
        this.targetAccount = targetAccount.trim();
        this.amount = amount;
        this.creatorUserId = creatorUserId;
        this.creationDate = LocalDateTime.now();

        // Initial status depends on whether approval is required
        this.status = requiresApproval
            ? TransferStatus.PENDING_APPROVAL
            : TransferStatus.EXECUTED;
    }

    // ==================== BUSINESS METHODS (APPROVAL FLOW) ====================

    /**
     * Approves the transfer. Can only be executed by a Company Supervisor.
     *
     * Transition: PENDING_APPROVAL → EXECUTED
     *
     * IMPORTANT: This method only changes the transfer status.
     * The fund movement (debit/credit accounts) must be performed
     * in the service layer, since it involves other entities (BankAccount).
     *
     * @param approverId ID of the Company Supervisor approving
     * @throws IllegalStateException if the transfer is not PENDING_APPROVAL or has expired
     */
    public void approve(int approverId) {
        validateNotExpired();
        validateTransition(TransferStatus.EXECUTED);

        this.approverUserId = approverId;
        this.approvalDate = LocalDateTime.now();
        this.status = TransferStatus.EXECUTED;
    }

    /**
     * Rejects the transfer. Can only be executed by a Company Supervisor.
     *
     * Transition: PENDING_APPROVAL → REJECTED
     *
     * @param approverId ID of the Company Supervisor rejecting
     * @throws IllegalStateException if the transfer is not PENDING_APPROVAL
     */
    public void reject(int approverId) {
        validateTransition(TransferStatus.REJECTED);

        this.approverUserId = approverId;
        this.approvalDate = LocalDateTime.now();
        this.status = TransferStatus.REJECTED;
    }

    /**
     * Marks the transfer as expired due to lack of timely approval.
     *
     * Transition: PENDING_APPROVAL → EXPIRED
     *
     * This method is invoked by the automatic expiry process
     * that periodically reviews pending transfers.
     *
     * @throws IllegalStateException if the transfer is not PENDING_APPROVAL
     */
    public void markAsExpired() {
        validateTransition(TransferStatus.EXPIRED);
        this.status = TransferStatus.EXPIRED;
    }

    /**
     * Checks whether the transfer has exceeded the maximum wait time
     * for approval (60 minutes from creation).
     *
     * @return true if more than 60 minutes have passed since creation
     *         and the transfer is still PENDING_APPROVAL
     */
    public boolean isExpired() {
        if (status != TransferStatus.PENDING_APPROVAL) {
            return false;
        }
        long minutesElapsed = Duration.between(creationDate, LocalDateTime.now()).toMinutes();
        return minutesElapsed > EXPIRY_MINUTES;
    }

    /**
     * Checks whether the transfer is pending approval.
     *
     * @return true if the status is PENDING_APPROVAL
     */
    public boolean isPendingApproval() {
        return status == TransferStatus.PENDING_APPROVAL;
    }

    /**
     * Checks whether the transfer was executed successfully.
     *
     * @return true if the status is EXECUTED
     */
    public boolean wasExecuted() {
        return status == TransferStatus.EXECUTED;
    }

    // ==================== PRIVATE VALIDATIONS ====================

    private void validateId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("The transfer ID must be greater than zero.");
        }
    }

    private void validateAccount(String account, String type) {
        if (account == null || account.isBlank()) {
            throw new IllegalArgumentException(
                "The " + type + " account number is required."
            );
        }
    }

    /**
     * Validates that the amount is strictly greater than zero.
     * Business rule: "The amount to transfer must be strictly greater than zero."
     */
    private void validateAmount(Money amount) {
        if (amount == null || !amount.isGreaterThanZero()) {
            throw new IllegalArgumentException(
                "The transfer amount must be strictly greater than zero."
            );
        }
    }

    private void validateTransition(TransferStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                "State transition not allowed for transfer " + transferId
                + ": " + status.getDescription() + " → " + newStatus.getDescription()
            );
        }
    }

    /**
     * Before approving, verifies the transfer has not expired.
     * If more than 60 minutes have passed, it is marked automatically as expired.
     */
    private void validateNotExpired() {
        if (isExpired()) {
            markAsExpired();
            throw new IllegalStateException(
                "Transfer " + transferId
                + " has expired due to lack of approval within the established time (60 minutes)."
            );
        }
    }

    // ==================== GETTERS ====================

    public int getTransferId() {
        return transferId;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public String getTargetAccount() {
        return targetAccount;
    }

    public Money getAmount() {
        return amount;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public int getCreatorUserId() {
        return creatorUserId;
    }

    public int getApproverUserId() {
        return approverUserId;
    }

    // ==================== EQUALS, HASHCODE, TOSTRING ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transfer that = (Transfer) o;
        return transferId == that.transferId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(transferId);
    }

    @Override
    public String toString() {
        return "Transfer{" +
            "transferId=" + transferId +
            ", sourceAccount='" + sourceAccount + '\'' +
            ", targetAccount='" + targetAccount + '\'' +
            ", amount=" + amount +
            ", status=" + status +
            ", creationDate=" + creationDate +
            '}';
    }
}
