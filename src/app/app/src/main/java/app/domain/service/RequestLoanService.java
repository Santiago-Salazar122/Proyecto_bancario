package app.domain.service;

import app.domain.model.loan.Loan;
import app.domain.model.user.User;
import app.domain.ports.LoanRepositoryPort;
import app.domain.ports.UserRepositoryPort;
import app.domain.valueobject.Money;

public class RequestLoanService {

    private final LoanRepositoryPort loanRepository;
    private final UserRepositoryPort userRepository;

    public RequestLoanService(LoanRepositoryPort loanRepository, UserRepositoryPort userRepository) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
    }

    public Loan execute(int loanId, String loanType, String requestingClientId, Money requestedAmount, int termMonths) {
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
}
