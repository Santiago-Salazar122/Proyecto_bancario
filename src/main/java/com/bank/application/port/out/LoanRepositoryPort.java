package com.bank.application.port.out;

import com.bank.domain.model.loan.Loan;

import java.util.Optional;

public interface LoanRepositoryPort {

    Optional<Loan> findById(int loanId);

    boolean existsById(int loanId);

    void save(Loan loan);
}
