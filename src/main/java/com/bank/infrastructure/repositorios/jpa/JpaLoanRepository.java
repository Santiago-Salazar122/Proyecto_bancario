package com.bank.infrastructure.repositorios.jpa;

import com.bank.domain.model.loan.Loan;
import com.bank.domain.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** Repositorio JPA para Loan → tabla "loans" en MySQL. */
public interface JpaLoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByRequestingClientId(String requestingClientId);
    List<Loan> findByStatus(LoanStatus status);
}
