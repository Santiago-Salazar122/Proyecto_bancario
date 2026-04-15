package com.bank.domain.service;

import com.bank.domain.model.account.BankAccount;
import com.bank.domain.ports.BankAccountRepositoryPort;
import com.bank.domain.valueobject.Money;

public class DepositToBankAccountService {

    private final BankAccountRepositoryPort accountRepository;

    public DepositToBankAccountService(BankAccountRepositoryPort accountRepository) {
        this.accountRepository = accountRepository;
    }

    public BankAccount execute(String accountNumber, Money amount) {
        BankAccount account = loadAccount(accountNumber);
        account.credit(amount);
        accountRepository.save(account);
        return account;
    }

    private BankAccount loadAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new IllegalStateException("Account not found: " + accountNumber));
    }
}