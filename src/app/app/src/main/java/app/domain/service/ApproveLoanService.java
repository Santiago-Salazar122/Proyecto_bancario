package app.domain.service;

import app.domain.enums.UserRole;
import app.domain.model.loan.Loan;
import app.domain.model.user.User;
import app.domain.ports.LoanRepositoryPort;
import app.domain.ports.UserRepositoryPort;
import app.domain.valueobject.Money;

import java.math.BigDecimal;

public class ApproveLoanService {

    private final LoanRepositoryPort loanRepository;
    private final UserRepositoryPort userRepository;

    public ApproveLoanService(LoanRepositoryPort loanRepository, UserRepositoryPort userRepository) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
    }

    public Loan execute(int loanId, Money approvedAmount, BigDecimal interestRate, int analystUserId, String targetAccount) {
        User analyst = requireAnalyst(analystUserId);

        Loan loan = loadLoan(loanId);
        loan.approve(approvedAmount, interestRate, analyst.getUserId(), targetAccount);
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
