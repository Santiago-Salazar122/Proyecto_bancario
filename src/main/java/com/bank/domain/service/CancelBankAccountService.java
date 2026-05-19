package com.bank.domain.service;

import com.bank.domain.model.account.BankAccount;
import com.bank.domain.model.audit.OperationLog;
import com.bank.domain.ports.BankAccountRepositoryPort;
import com.bank.domain.ports.OperationLogRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

/** Servicio de dominio: Cancelar permanentemente una cuenta. */
@Service @Transactional
public class CancelBankAccountService {
    private final BankAccountRepositoryPort accountRepository;
    private final OperationLogRepositoryPort auditLog;
    public CancelBankAccountService(BankAccountRepositoryPort ar, OperationLogRepositoryPort al) {
        this.accountRepository = ar; this.auditLog = al;
    }
    public BankAccount execute(String accountNumber, Long operatorUserId) {
        BankAccount account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new IllegalStateException("Account not found: " + accountNumber));
        account.cancel();
        accountRepository.save(account);
        auditLog.save(OperationLog.create("ACCOUNT_CANCELLED", operatorUserId, "OPERATOR",
            accountNumber, Map.of("operatorId", operatorUserId)));
        return account;
    }
}
