package app.domain.service;

import app.domain.model.account.BankAccount;
import app.domain.ports.BankAccountRepositoryPort;
import app.domain.valueobject.Money;

public class WithdrawFromBankAccountService {

    private final BankAccountRepositoryPort accountRepository;

    public WithdrawFromBankAccountService(BankAccountRepositoryPort accountRepository) {
        this.accountRepository = accountRepository;
    }

    public BankAccount execute(String accountNumber, Money amount) {
        BankAccount account = loadAccount(accountNumber);
        account.debit(amount);
        accountRepository.save(account);
        return account;
    }

    private BankAccount loadAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new IllegalStateException("Account not found: " + accountNumber));
    }
}
