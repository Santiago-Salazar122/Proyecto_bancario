package com.bank.infrastructure.repositorios.adapter;

import com.bank.domain.model.account.BankAccount;
import com.bank.domain.enums.AccountStatus;
import com.bank.domain.ports.BankAccountRepositoryPort;
import com.bank.infrastructure.repositorios.jpa.JpaBankAccountRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

/** Adaptador: BankAccountRepositoryPort → JPA (MySQL). */
@Component
public class BankAccountRepositoryAdapter implements BankAccountRepositoryPort {
    private final JpaBankAccountRepository jpa;
    public BankAccountRepositoryAdapter(JpaBankAccountRepository jpa) { this.jpa = jpa; }

    @Override public BankAccount save(BankAccount a)                     { return jpa.save(a); }
    @Override public Optional<BankAccount> findByAccountNumber(String n) { return jpa.findByAccountNumber(n); }
    @Override public List<BankAccount> findByOwnerId(String id)          { return jpa.findByOwnerId(id); }
    @Override public boolean existsByAccountNumber(String n)             { return jpa.existsByAccountNumber(n); }
    @Override public List<BankAccount> findByStatus(AccountStatus s)     { return jpa.findByStatus(s); }
    @Override public List<BankAccount> findAll()                         { return jpa.findAll(); }
}
