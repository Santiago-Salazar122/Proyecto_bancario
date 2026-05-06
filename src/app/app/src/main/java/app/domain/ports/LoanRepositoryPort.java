package app.domain.ports;

import app.domain.model.loan.Loan;

import java.util.Optional;

public interface LoanRepositoryPort {

    Optional<Loan> findById(int loanId);

    boolean existsById(int loanId);

    void save(Loan loan);
}

