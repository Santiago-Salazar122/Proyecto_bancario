package com.bank.domain.service;

import com.bank.domain.model.account.BankAccount;
import com.bank.domain.ports.BankAccountRepositoryPort;

public class BlockBankAccountService {

    private final BankAccountRepositoryPort accountRepository;

    public BlockBankAccountService(BankAccountRepositoryPort accountRepository) {
        this.accountRepository = accountRepository;
    }

    public BankAccount execute(String accountNumber) {
        BankAccount account = loadAccount(accountNumber);
        account.block();
        accountRepository.save(account);
        return account;
    }

    private BankAccount loadAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new IllegalStateException("Account not found: " + accountNumber));
    }
}