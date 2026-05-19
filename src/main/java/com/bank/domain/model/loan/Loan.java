package com.bank.domain.model.loan;

import com.bank.domain.enums.LoanStatus;
import com.bank.domain.enums.LoanType;
import com.bank.domain.valueobject.Money;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad raíz que representa un Préstamo / Crédito bancario.
 * Se mapea a la tabla "loans" en MySQL.
 *
 * Flujo de aprobación:
 * 1. Se crea con estado UNDER_REVIEW.
 * 2. Solo el INTERNAL_ANALYST puede aprobar → APPROVED o rechazar → REJECTED.
 * 3. Solo desde APPROVED se puede desembolsar → DISBURSED.
 *
 * Al desembolsar: el servicio acredita el montoAprobado a la cuenta destino en MySQL.
 */
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_id")
    private Long loanId;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false, length = 20)
    private LoanType loanType;

    @Column(name = "requesting_client_id", nullable = false, length = 20)
    private String requestingClientId;

    /** Saldo almacenado como BigDecimal en MySQL para precisión exacta. */
    @Column(name = "requested_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "approved_amount", precision = 19, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "term_months", nullable = false)
    private int termMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LoanStatus status;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "disbursement_date")
    private LocalDateTime disbursementDate;

    @Column(name = "disbursement_target_account", length = 20)
    private String disbursementTargetAccount;

    @Column(name = "approver_analyst_id")
    private Long approverAnalystId;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    protected Loan() {}

    /**
     * Crea una nueva solicitud de préstamo con estado UNDER_REVIEW.
     */
    public Loan(LoanType loanType, String requestingClientId,
                Money requestedAmount, BigDecimal interestRate, int termMonths) {

        if (loanType == null) throw new IllegalArgumentException("The loan type is required.");
        if (requestingClientId == null || requestingClientId.isBlank())
            throw new IllegalArgumentException("The requesting client ID is required.");
        if (requestedAmount == null || !requestedAmount.isGreaterThanZero())
            throw new IllegalArgumentException("The requested amount must be greater than zero.");
        if (termMonths <= 0)
            throw new IllegalArgumentException("The term must be greater than zero months.");

        this.loanType = loanType;
        this.requestingClientId = requestingClientId.trim();
        this.requestedAmount = requestedAmount.getAmount();
        this.interestRate = interestRate;
        this.termMonths = termMonths;
        this.status = LoanStatus.UNDER_REVIEW; // Estado inicial obligatorio
        this.creationDate = LocalDateTime.now();
    }

    // ═══════════════════ MÉTODOS DE NEGOCIO ═══════════════════

    /**
     * Aprueba el préstamo. Transición: UNDER_REVIEW → APPROVED.
     * Solo INTERNAL_ANALYST puede llamar este método (validado en ApproveLoanService).
     */
    public void approve(Money approvedAmount, BigDecimal interestRate,
                        Long analystId, String targetAccount) {
        validateTransition(LoanStatus.APPROVED);
        if (approvedAmount == null || !approvedAmount.isGreaterThanZero())
            throw new IllegalArgumentException("The approved amount must be greater than zero.");
        if (interestRate == null || interestRate.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("The interest rate must be greater than zero.");

        this.approvedAmount = approvedAmount.getAmount();
        this.interestRate = interestRate;
        this.approverAnalystId = analystId;
        this.disbursementTargetAccount = (targetAccount != null) ? targetAccount.trim() : null;
        this.approvalDate = LocalDateTime.now();
        this.status = LoanStatus.APPROVED;
    }

    /**
     * Rechaza el préstamo. Transición: UNDER_REVIEW → REJECTED.
     */
    public void reject(Long analystId) {
        validateTransition(LoanStatus.REJECTED);
        this.approverAnalystId = analystId;
        this.approvalDate = LocalDateTime.now();
        this.status = LoanStatus.REJECTED;
    }

    /**
     * Marca el préstamo como desembolsado. Transición: APPROVED → DISBURSED.
     * El acreditamiento del saldo a la cuenta lo hace DisburseLoanService.
     */
    public void disburse() {
        validateTransition(LoanStatus.DISBURSED);
        if (disbursementTargetAccount == null || disbursementTargetAccount.isBlank())
            throw new IllegalStateException(
                "Cannot disburse loan " + loanId + ": target account is not defined.");
        if (approvedAmount == null || approvedAmount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalStateException(
                "Cannot disburse loan " + loanId + ": approved amount must be > 0.");
        this.disbursementDate = LocalDateTime.now();
        this.status = LoanStatus.DISBURSED;
    }

    /** Asigna la cuenta destino del desembolso. */
    public void assignTargetAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank())
            throw new IllegalArgumentException("The target account number is required.");
        this.disbursementTargetAccount = accountNumber.trim();
    }

    private void validateTransition(LoanStatus newStatus) {
        if (!status.canTransitionTo(newStatus))
            throw new IllegalStateException(
                "State transition not allowed for loan " + loanId
                + ": " + status.getDescription() + " → " + newStatus.getDescription());
    }

    // ═══════════════════ GETTERS ═══════════════════

    public Long getLoanId()                      { return loanId; }
    public LoanType getLoanType()                { return loanType; }
    public String getRequestingClientId()        { return requestingClientId; }
    public BigDecimal getRequestedAmount()       { return requestedAmount; }
    public BigDecimal getApprovedAmount()        { return approvedAmount; }
    public BigDecimal getInterestRate()          { return interestRate; }
    public int getTermMonths()                   { return termMonths; }
    public LoanStatus getStatus()                { return status; }
    public LocalDateTime getApprovalDate()       { return approvalDate; }
    public LocalDateTime getDisbursementDate()   { return disbursementDate; }
    public String getDisbursementTargetAccount() { return disbursementTargetAccount; }
    public Long getApproverAnalystId()           { return approverAnalystId; }
    public LocalDateTime getCreationDate()       { return creationDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(loanId, ((Loan) o).loanId);
    }

    @Override public int hashCode() { return Objects.hash(loanId); }

    @Override
    public String toString() {
        return "Loan{loanId=" + loanId + ", loanType=" + loanType
            + ", clientId='" + requestingClientId + '\''
            + ", requestedAmount=$" + requestedAmount + ", status=" + status + '}';
    }
}
