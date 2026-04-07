package com.bank.application.service;

import com.bank.application.port.in.AccountUseCase;
import com.bank.application.port.out.BankAccountRepositoryPort;
import com.bank.application.port.out.UserRepositoryPort;
import com.bank.domain.enums.AccountType;
import com.bank.domain.model.account.BankAccount;
import com.bank.domain.model.user.User;
import com.bank.domain.valueobject.Money;

public class AccountApplicationService implements AccountUseCase {

    private final BankAccountRepositoryPort accountRepository;
    private final UserRepositoryPort userRepository;

    public AccountApplicationService(BankAccountRepositoryPort accountRepository, UserRepositoryPort userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Override
    public BankAccount openAccount(String accountNumber, AccountType accountType, String ownerId, String currency) {
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new IllegalStateException("Account number already exists: " + accountNumber);
        }

        User ownerUser = userRepository.findByRelatedId(ownerId)
            .orElseThrow(() -> new IllegalStateException("No active user found for owner ID: " + ownerId));

        if (!ownerUser.canOperate()) {
            throw new IllegalStateException("The owner cannot operate. User status: " + ownerUser.getStatus().getDescription());
        }

        BankAccount account = new BankAccount(accountNumber, accountType, ownerId, currency);
        accountRepository.save(account);
        return account;
    }

    @Override
    public BankAccount deposit(String accountNumber, Money amount) {
        BankAccount account = loadAccount(accountNumber);
        account.credit(amount);
        accountRepository.save(account);
        return account;
    }

    @Override
    public BankAccount withdraw(String accountNumber, Money amount) {
        BankAccount account = loadAccount(accountNumber);
        account.debit(amount);
        accountRepository.save(account);
        return account;
    }

    @Override
    public BankAccount blockAccount(String accountNumber) {
        BankAccount account = loadAccount(accountNumber);
        account.block();
        accountRepository.save(account);
        return account;
    }

    @Override
    public BankAccount reactivateAccount(String accountNumber) {
        BankAccount account = loadAccount(accountNumber);
        account.reactivate();
        accountRepository.save(account);
        return account;
    }

    @Override
    public BankAccount cancelAccount(String accountNumber) {
        BankAccount account = loadAccount(accountNumber);
        account.cancel();
        accountRepository.save(account);
        return account;
    }

    private BankAccount loadAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new IllegalStateException("Account not found: " + accountNumber));
    }
}
