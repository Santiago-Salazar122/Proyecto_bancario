package com.bank.domain.service;

import com.bank.domain.enums.UserRole;
import com.bank.domain.model.account.BankAccount;
import com.bank.domain.model.audit.OperationLog;
import com.bank.domain.model.loan.Loan;
import com.bank.domain.model.user.User;
import com.bank.domain.ports.BankAccountRepositoryPort;
import com.bank.domain.ports.LoanRepositoryPort;
import com.bank.domain.ports.OperationLogRepositoryPort;
import com.bank.domain.ports.UserRepositoryPort;
import com.bank.domain.valueobject.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Servicio de dominio: Desembolsar un préstamo aprobado.
 * Transición: APPROVED → DISBURSED.
 * @Transactional garantiza que el crédito en cuenta y el cambio
 * de estado del préstamo se hacen juntos en MySQL (o ambos fallan).
 */
@Service @Transactional
public class DisburseLoanService {
    private final LoanRepositoryPort loanRepository;
    private final BankAccountRepositoryPort accountRepository;
    private final UserRepositoryPort userRepository;
    private final OperationLogRepositoryPort auditLog;

    public DisburseLoanService(LoanRepositoryPort lr, BankAccountRepositoryPort ar,
                                UserRepositoryPort ur, OperationLogRepositoryPort al) {
        this.loanRepository = lr; this.accountRepository = ar;
        this.userRepository = ur; this.auditLog = al;
    }

    public Loan execute(Long loanId, String targetAccountNumber, Long analystUserId) {
        User analyst = userRepository.findById(analystUserId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + analystUserId));
        if (!analyst.hasRole(UserRole.INTERNAL_ANALYST))
            throw new IllegalStateException("Only an Internal Analyst can disburse loans.");
        if (!analyst.canOperate())
            throw new IllegalStateException("The analyst user cannot operate.");

        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new IllegalStateException("Loan not found: " + loanId));

        BankAccount targetAccount = accountRepository.findByAccountNumber(targetAccountNumber)
            .orElseThrow(() -> new IllegalStateException("Target account not found: " + targetAccountNumber));
        if (!targetAccount.isOperational())
            throw new IllegalStateException("Target account is not active: " + targetAccountNumber);
        if (!targetAccount.getOwnerId().equals(loan.getRequestingClientId()))
            throw new IllegalStateException("Target account does not belong to the requesting client.");

        loan.assignTargetAccount(targetAccountNumber);
        BigDecimal balanceBefore = targetAccount.getBalanceAmount();
        targetAccount.credit(new Money(loan.getApprovedAmount()));
        accountRepository.save(targetAccount);
        loan.disburse();
        loanRepository.save(loan);

        auditLog.save(OperationLog.create("LOAN_DISBURSED", analystUserId,
            UserRole.INTERNAL_ANALYST.name(), String.valueOf(loanId),
            Map.of("targetAccount", targetAccountNumber, "disbursedAmount", loan.getApprovedAmount(),
                "balanceBefore", balanceBefore, "balanceAfter", targetAccount.getBalanceAmount())));
        return loan;
    }
}
