package com.bank.domain.service;

import com.bank.domain.enums.AccountType;
import com.bank.domain.model.account.BankAccount;
import com.bank.domain.model.audit.OperationLog;
import com.bank.domain.model.user.User;
import com.bank.domain.ports.BankAccountRepositoryPort;
import com.bank.domain.ports.OperationLogRepositoryPort;
import com.bank.domain.ports.UserRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

/**
 * Servicio de dominio: Abrir una cuenta bancaria.
 * Valida que el cliente exista y esté ACTIVE.
 * El número de cuenta se genera automáticamente.
 */
@Service
@Transactional
public class OpenBankAccountService {
    private final BankAccountRepositoryPort accountRepository;
    private final UserRepositoryPort userRepository;
    private final OperationLogRepositoryPort auditLog;

    public OpenBankAccountService(BankAccountRepositoryPort accountRepository,
                                   UserRepositoryPort userRepository,
                                   OperationLogRepositoryPort auditLog) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.auditLog = auditLog;
    }

    public BankAccount execute(String ownerIdentificationId, AccountType accountType,
                                String currency, Long operatorUserId) {
        User owner = userRepository.findByIdentificationId(ownerIdentificationId)
            .orElseThrow(() -> new IllegalStateException("Client not found: " + ownerIdentificationId));
        if (!owner.canOperate())
            throw new IllegalStateException(
                "Cannot open account. Client status is: " + owner.getStatus().getDescription()
                + ". Only ACTIVE clients can open accounts.");

        String accountNumber = generateUniqueAccountNumber();
        BankAccount account = new BankAccount(accountNumber, accountType, ownerIdentificationId, currency);
        accountRepository.save(account);

        auditLog.save(OperationLog.create("ACCOUNT_OPENED", operatorUserId, "OPERATOR",
            accountNumber, Map.of("accountType", accountType.name(),
                "currency", currency, "ownerId", ownerIdentificationId)));
        return account;
    }

    private String generateUniqueAccountNumber() {
        String candidate;
        do { candidate = String.valueOf(System.currentTimeMillis()).substring(3, 13); }
        while (accountRepository.existsByAccountNumber(candidate));
        return candidate;
    }
}
