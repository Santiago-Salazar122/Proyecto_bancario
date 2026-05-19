package com.bank.domain.service;

import com.bank.domain.enums.UserRole;
import com.bank.domain.model.audit.OperationLog;
import com.bank.domain.model.loan.Loan;
import com.bank.domain.model.user.User;
import com.bank.domain.ports.LoanRepositoryPort;
import com.bank.domain.ports.OperationLogRepositoryPort;
import com.bank.domain.ports.UserRepositoryPort;
import com.bank.domain.valueobject.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Servicio de dominio: Aprobar un préstamo.
 * Solo INTERNAL_ANALYST puede ejecutarlo.
 * Transición: UNDER_REVIEW → APPROVED.
 */
@Service
@Transactional
public class ApproveLoanService {
    private final LoanRepositoryPort loanRepository;
    private final UserRepositoryPort userRepository;
    private final OperationLogRepositoryPort auditLog;

    public ApproveLoanService(LoanRepositoryPort lr, UserRepositoryPort ur, OperationLogRepositoryPort al) {
        this.loanRepository = lr; this.userRepository = ur; this.auditLog = al;
    }

    public Loan execute(Long loanId, Money approvedAmount, BigDecimal interestRate,
                        Long analystUserId, String targetAccount) {
        User analyst = userRepository.findById(analystUserId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + analystUserId));
        if (!analyst.hasRole(UserRole.INTERNAL_ANALYST))
            throw new IllegalStateException("Only users with Internal Analyst role can perform loan decisions.");
        if (!analyst.canOperate())
            throw new IllegalStateException("The analyst user cannot operate.");

        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new IllegalStateException("Loan not found: " + loanId));
        loan.approve(approvedAmount, interestRate, analystUserId, targetAccount);
        loanRepository.save(loan);

        auditLog.save(OperationLog.create("LOAN_APPROVED", analystUserId,
            UserRole.INTERNAL_ANALYST.name(), String.valueOf(loanId),
            Map.of("approvedAmount", approvedAmount.getAmount(), "interestRate", interestRate,
                "targetAccount", targetAccount != null ? targetAccount : "not set",
                "previousStatus", "UNDER_REVIEW", "newStatus", "APPROVED")));
        return loan;
    }
}
