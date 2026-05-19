package com.bank.domain.ports;

import com.bank.domain.model.account.BankAccount;
import com.bank.domain.enums.AccountStatus;
import java.util.List;
import java.util.Optional;

public interface BankAccountRepositoryPort {
    BankAccount save(BankAccount account);
    Optional<BankAccount> findByAccountNumber(String accountNumber);
    List<BankAccount> findByOwnerId(String ownerId);
    boolean existsByAccountNumber(String accountNumber);
    List<BankAccount> findByStatus(AccountStatus status);
    List<BankAccount> findAll();
}
