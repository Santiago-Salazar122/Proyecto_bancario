package com.bank.infrastructure.repositorios.jpa;

import com.bank.domain.model.account.BankAccount;
import com.bank.domain.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** Repositorio JPA para BankAccount → tabla "bank_accounts" en MySQL. */
public interface JpaBankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByAccountNumber(String accountNumber);
    List<BankAccount> findByOwnerId(String ownerId);
    boolean existsByAccountNumber(String accountNumber);
    List<BankAccount> findByStatus(AccountStatus status);
}
