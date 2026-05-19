package com.bank.domain.service;

import com.bank.domain.enums.LoanType;
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
 * Servicio de dominio: Solicitar un préstamo.
 * Estado inicial: UNDER_REVIEW.
 * Regla: el cliente debe existir y estar ACTIVE.
 */
@Service
@Transactional
public class RequestLoanService {
    private final LoanRepositoryPort loanRepository;
    private final UserRepositoryPort userRepository;
    private final OperationLogRepositoryPort auditLog;

    public RequestLoanService(LoanRepositoryPort lr, UserRepositoryPort ur, OperationLogRepositoryPort al) {
        this.loanRepository = lr; this.userRepository = ur; this.auditLog = al;
    }

    public Loan execute(String requestingClientId, LoanType loanType,
                        Money requestedAmount, BigDecimal interestRate,
                        int termMonths, Long creatorUserId) {
        User client = userRepository.findByIdentificationId(requestingClientId)
            .orElseThrow(() -> new IllegalStateException(
                "No user found for requesting client: " + requestingClientId));
        if (!client.canOperate())
            throw new IllegalStateException(
                "The requesting client cannot operate. Status: " + client.getStatus());

        Loan loan = new Loan(loanType, requestingClientId, requestedAmount, interestRate, termMonths);
        loanRepository.save(loan);

        auditLog.save(OperationLog.create("LOAN_REQUESTED", creatorUserId,
            client.getRole().name(), String.valueOf(loan.getLoanId()),
            Map.of("loanType", loanType.name(), "requestedAmount", requestedAmount.getAmount(),
                "interestRate", interestRate, "termMonths", termMonths)));
        return loan;
    }
}
