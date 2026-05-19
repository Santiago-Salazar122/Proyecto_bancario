package com.bank.infrastructure.repositorios.adapter;

import com.bank.domain.model.loan.Loan;
import com.bank.domain.enums.LoanStatus;
import com.bank.domain.ports.LoanRepositoryPort;
import com.bank.infrastructure.repositorios.jpa.JpaLoanRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

/** Adaptador: LoanRepositoryPort → JPA (MySQL). */
@Component
public class LoanRepositoryAdapter implements LoanRepositoryPort {
    private final JpaLoanRepository jpa;
    public LoanRepositoryAdapter(JpaLoanRepository jpa) { this.jpa = jpa; }

    @Override public Loan save(Loan loan)                                { return jpa.save(loan); }
    @Override public Optional<Loan> findById(Long id)                    { return jpa.findById(id); }
    @Override public List<Loan> findByClientId(String clientId)          { return jpa.findByRequestingClientId(clientId); }
    @Override public List<Loan> findByStatus(LoanStatus s)               { return jpa.findByStatus(s); }
    @Override public List<Loan> findAll()                                { return jpa.findAll(); }
}
