package com.bank.domain.ports;

import com.bank.domain.model.loan.Loan;
import com.bank.domain.enums.LoanStatus;
import java.util.List;
import java.util.Optional;

public interface LoanRepositoryPort {
    Loan save(Loan loan);
    Optional<Loan> findById(Long loanId);
    List<Loan> findByClientId(String clientId);
    List<Loan> findByStatus(LoanStatus status);
    List<Loan> findAll();
}
