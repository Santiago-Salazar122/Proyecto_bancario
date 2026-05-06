package app.domain.service;

import app.domain.enums.UserRole;
import app.domain.model.account.BankAccount;
import app.domain.model.loan.Loan;
import app.domain.model.user.User;
import app.domain.ports.BankAccountRepositoryPort;
import app.domain.ports.LoanRepositoryPort;
import app.domain.ports.UserRepositoryPort;

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
