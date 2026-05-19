package com.bank.domain.service;

import com.bank.domain.enums.UserRole;
import com.bank.domain.model.audit.OperationLog;
import com.bank.domain.model.loan.Loan;
import com.bank.domain.model.user.User;
import com.bank.domain.ports.LoanRepositoryPort;
import com.bank.domain.ports.OperationLogRepositoryPort;
import com.bank.domain.ports.UserRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

/** Servicio de dominio: Rechazar un préstamo. Transición: UNDER_REVIEW → REJECTED. */
@Service @Transactional
public class RejectLoanService {
    private final LoanRepositoryPort loanRepository;
    private final UserRepositoryPort userRepository;
    private final OperationLogRepositoryPort auditLog;
    public RejectLoanService(LoanRepositoryPort lr, UserRepositoryPort ur, OperationLogRepositoryPort al) {
        this.loanRepository = lr; this.userRepository = ur; this.auditLog = al;
    }
    public Loan execute(Long loanId, Long analystUserId) {
        User analyst = userRepository.findById(analystUserId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + analystUserId));
        if (!analyst.hasRole(UserRole.INTERNAL_ANALYST))
            throw new IllegalStateException("Only users with Internal Analyst role can perform loan decisions.");
        if (!analyst.canOperate())
            throw new IllegalStateException("The analyst user cannot operate.");

        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new IllegalStateException("Loan not found: " + loanId));
        loan.reject(analystUserId);
        loanRepository.save(loan);
        auditLog.save(OperationLog.create("LOAN_REJECTED", analystUserId,
            UserRole.INTERNAL_ANALYST.name(), String.valueOf(loanId),
            Map.of("previousStatus", "UNDER_REVIEW", "newStatus", "REJECTED")));
        return loan;
    }
}
