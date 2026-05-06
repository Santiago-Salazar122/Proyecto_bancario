package app.domain.service;

import app.domain.model.account.BankAccount;
import app.domain.ports.BankAccountRepositoryPort;

public class CancelBankAccountService {

    private final BankAccountRepositoryPort accountRepository;

    public CancelBankAccountService(BankAccountRepositoryPort accountRepository) {
        this.accountRepository = accountRepository;
    }

    public BankAccount execute(String accountNumber) {
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
