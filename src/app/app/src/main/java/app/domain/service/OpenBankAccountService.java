package app.domain.service;

import app.domain.enums.AccountType;
import app.domain.model.account.BankAccount;
import app.domain.model.user.User;
import app.domain.ports.BankAccountRepositoryPort;
import app.domain.ports.UserRepositoryPort;

public class OpenBankAccountService {

    private final BankAccountRepositoryPort accountRepository;
    private final UserRepositoryPort userRepository;

    public OpenBankAccountService(BankAccountRepositoryPort accountRepository, UserRepositoryPort userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public BankAccount execute(String accountNumber, AccountType accountType, String ownerId, String currency) {
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
}
