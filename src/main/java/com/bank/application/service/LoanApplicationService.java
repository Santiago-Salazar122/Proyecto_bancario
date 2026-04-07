package com.bank.application.service;

import com.bank.application.port.in.LoanUseCase;
import com.bank.application.port.out.BankAccountRepositoryPort;
import com.bank.application.port.out.LoanRepositoryPort;
import com.bank.application.port.out.UserRepositoryPort;
import com.bank.domain.enums.UserRole;
import com.bank.domain.model.account.BankAccount;
import com.bank.domain.model.loan.Loan;
import com.bank.domain.model.user.User;
import com.bank.domain.valueobject.Money;

import java.math.BigDecimal;

public class LoanApplicationService implements LoanUseCase {

    private final LoanRepositoryPort loanRepository;
    private final BankAccountRepositoryPort accountRepository;
    private final UserRepositoryPort userRepository;

    public LoanApplicationService(
        LoanRepositoryPort loanRepository,
        BankAccountRepositoryPort accountRepository,
        UserRepositoryPort userRepository
    ) {
        this.loanRepository = loanRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Loan requestLoan(int loanId, String loanType, String requestingClientId, Money requestedAmount, int termMonths) {
        if (loanRepository.existsById(loanId)) {
            throw new IllegalStateException("Loan ID already exists: " + loanId);
        }

        User clientUser = userRepository.findByRelatedId(requestingClientId)
            .orElseThrow(() -> new IllegalStateException("No user found for requesting client: " + requestingClientId));

        if (!clientUser.canOperate()) {
            throw new IllegalStateException("The requesting client cannot operate.");
        }

        Loan loan = new Loan(loanId, loanType, requestingClientId, requestedAmount, termMonths);
        loanRepository.save(loan);
        return loan;
    }

    @Override
    public Loan approveLoan(int loanId, Money approvedAmount, BigDecimal interestRate, int analystUserId, String targetAccount) {
        User analyst = requireAnalyst(analystUserId);

        Loan loan = loadLoan(loanId);
        loan.approve(approvedAmount, interestRate, analyst.getUserId(), targetAccount);
        loanRepository.save(loan);
        return loan;
    }

    @Override
    public Loan rejectLoan(int loanId, int analystUserId) {
        User analyst = requireAnalyst(analystUserId);

        Loan loan = loadLoan(loanId);
        loan.reject(analyst.getUserId());
        loanRepository.save(loan);
        return loan;
    }

    @Override
    public Loan disburseLoan(int loanId, int analystUserId) {
        requireAnalyst(analystUserId);

        Loan loan = loadLoan(loanId);
        String targetAccountNumber = loan.getDisbursementTargetAccount();

        BankAccount targetAccount = accountRepository.findByAccountNumber(targetAccountNumber)
            .orElseThrow(() -> new IllegalStateException("Disbursement target account not found: " + targetAccountNumber));

        loan.disburse();
        targetAccount.credit(loan.getApprovedAmount());

        accountRepository.save(targetAccount);
        loanRepository.save(loan);
        return loan;
    }

    private User requireAnalyst(int analystUserId) {
        User analyst = userRepository.findById(analystUserId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + analystUserId));

        if (!analyst.hasRole(UserRole.INTERNAL_ANALYST)) {
            throw new IllegalStateException("Only users with Internal Analyst role can perform loan decisions.");
        }

        if (!analyst.canOperate()) {
            throw new IllegalStateException("The analyst user cannot operate.");
        }

        return analyst;
    }

    private Loan loadLoan(int loanId) {
        return loanRepository.findById(loanId)
            .orElseThrow(() -> new IllegalStateException("Loan not found: " + loanId));
    }
}
