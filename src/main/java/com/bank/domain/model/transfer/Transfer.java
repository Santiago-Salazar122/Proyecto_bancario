package com.bank.domain.model.transfer;

import com.bank.domain.enums.TransferStatus;
import com.bank.domain.valueobject.Money;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad raíz que representa una Transferencia bancaria.
 * Se mapea a la tabla "transfers" en MySQL.
 *
 * Flujo monto bajo (≤ umbral): EXECUTED directamente al crear.
 * Flujo empresa monto alto (> umbral):
 *   PENDING_APPROVAL → EXECUTED (aprobada por COMPANY_SUPERVISOR)
 *   PENDING_APPROVAL → REJECTED (rechazada por COMPANY_SUPERVISOR)
 *   PENDING_APPROVAL → EXPIRED  (automático si pasan 60+ minutos)
 */
@Entity
@Table(name = "transfers")
public class Transfer {

    /** Tiempo máximo en minutos antes de que una transferencia expire. */
    private static final long EXPIRY_MINUTES = 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id")
    private Long transferId;

    @Column(name = "source_account", nullable = false, length = 20)
    private String sourceAccount;

    @Column(name = "target_account", nullable = false, length = 20)
    private String targetAccount;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransferStatus status;

    @Column(name = "creator_user_id", nullable = false)
    private Long creatorUserId;

    @Column(name = "approver_user_id")
    private Long approverUserId;

    protected Transfer() {}

    /**
     * Crea una transferencia. El estado inicial depende de requiresApproval:
     * - false → EXECUTED (fondos movidos en CreateTransferService)
     * - true  → PENDING_APPROVAL (fondos se mueven al aprobar)
     */
    public Transfer(String sourceAccount, String targetAccount,
                    Money amount, Long creatorUserId, boolean requiresApproval) {
        if (sourceAccount == null || sourceAccount.isBlank())
            throw new IllegalArgumentException("The source account number is required.");
        if (targetAccount == null || targetAccount.isBlank())
            throw new IllegalArgumentException("The target account number is required.");
        if (amount == null || !amount.isGreaterThanZero())
            throw new IllegalArgumentException("The transfer amount must be > 0.");

        this.sourceAccount = sourceAccount.trim();
        this.targetAccount = targetAccount.trim();
        this.amount = amount.getAmount();
        this.creatorUserId = creatorUserId;
        this.creationDate = LocalDateTime.now();
        this.status = requiresApproval
            ? TransferStatus.PENDING_APPROVAL
            : TransferStatus.EXECUTED;
    }

    // ═══════════════════ MÉTODOS DE NEGOCIO ═══════════════════

    /**
     * Aprueba la transferencia. Transición: PENDING_APPROVAL → EXECUTED.
     * Verifica que no hayan pasado 60+ minutos antes de aprobar.
     */
    public void approve(Long approverId) {
        validateNotExpired();
        validateTransition(TransferStatus.EXECUTED);
        this.approverUserId = approverId;
        this.approvalDate = LocalDateTime.now();
        this.status = TransferStatus.EXECUTED;
    }

    /**
     * Rechaza la transferencia. Transición: PENDING_APPROVAL → REJECTED.
     */
    public void reject(Long approverId) {
        validateTransition(TransferStatus.REJECTED);
        this.approverUserId = approverId;
        this.approvalDate = LocalDateTime.now();
        this.status = TransferStatus.REJECTED;
    }

    /**
     * Marca como EXPIRED. Llamado por ExpirePendingTransfersService.
     * Transición: PENDING_APPROVAL → EXPIRED.
     */
    public void markAsExpired() {
        validateTransition(TransferStatus.EXPIRED);
        this.status = TransferStatus.EXPIRED;
    }

    /**
     * Verifica si han pasado más de 60 minutos desde la creación
     * y la transferencia sigue en PENDING_APPROVAL.
     */
    public boolean isExpired() {
        if (status != TransferStatus.PENDING_APPROVAL) return false;
        return Duration.between(creationDate, LocalDateTime.now()).toMinutes() > EXPIRY_MINUTES;
    }

    public boolean isPendingApproval() { return status == TransferStatus.PENDING_APPROVAL; }
    public boolean wasExecuted()       { return status == TransferStatus.EXECUTED; }

    private void validateTransition(TransferStatus newStatus) {
        if (!status.canTransitionTo(newStatus))
            throw new IllegalStateException(
                "State transition not allowed for transfer " + transferId
                + ": " + status.getDescription() + " → " + newStatus.getDescription());
    }

    /** Antes de aprobar, verifica que no venció. Si venció, la marca automáticamente. */
    private void validateNotExpired() {
        if (isExpired()) {
            markAsExpired();
            throw new IllegalStateException(
                "Transfer " + transferId + " has expired: 60-minute approval limit exceeded.");
        }
    }

    // ═══════════════════ GETTERS ═══════════════════

    public Long getTransferId()         { return transferId; }
    public String getSourceAccount()    { return sourceAccount; }
    public String getTargetAccount()    { return targetAccount; }
    public BigDecimal getAmount()       { return amount; }
    public Money getAmountAsMoney()     { return new Money(amount); }
    public LocalDateTime getCreationDate()  { return creationDate; }
    public LocalDateTime getApprovalDate()  { return approvalDate; }
    public TransferStatus getStatus()   { return status; }
    public Long getCreatorUserId()      { return creatorUserId; }
    public Long getApproverUserId()     { return approverUserId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(transferId, ((Transfer) o).transferId);
    }

    @Override public int hashCode() { return Objects.hash(transferId); }

    @Override
    public String toString() {
        return "Transfer{transferId=" + transferId
            + ", sourceAccount='" + sourceAccount + '\''
            + ", targetAccount='" + targetAccount + '\''
            + ", amount=$" + amount + ", status=" + status + '}';
    }
}
