package com.bank.domain.ports;

import com.bank.domain.model.account.BankAccount;

import java.util.Optional;

public interface BankAccountRepositoryPort {

    Optional<BankAccount> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    void save(BankAccount account);
}

