package com.bank.domain.service;

import com.bank.domain.enums.UserRole;
import com.bank.domain.model.account.BankAccount;
import com.bank.domain.model.loan.Loan;
import com.bank.domain.model.user.User;
import com.bank.domain.ports.BankAccountRepositoryPort;
import com.bank.domain.ports.LoanRepositoryPort;
import com.bank.domain.ports.UserRepositoryPort;

public class DisburseLoanService {

    private final LoanRepositoryPort loanRepository;
    private final BankAccountRepositoryPort accountRepository;
    private final UserRepositoryPort userRepository;

    public DisburseLoanService(
        LoanRepositoryPort loanRepository,
        BankAccountRepositoryPort accountRepository,
        UserRepositoryPort userRepository
    ) {
        this.loanRepository = loanRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public Loan execute(int loanId, int analystUserId) {
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