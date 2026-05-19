package com.bank.domain.service;

import com.bank.domain.model.account.BankAccount;
import com.bank.domain.model.audit.OperationLog;
import com.bank.domain.ports.BankAccountRepositoryPort;
import com.bank.domain.ports.OperationLogRepositoryPort;
import com.bank.domain.valueobject.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Map;

/** Servicio de dominio: Depositar en una cuenta. */
@Service
@Transactional
public class DepositToBankAccountService {
    private final BankAccountRepositoryPort accountRepository;
    private final OperationLogRepositoryPort auditLog;
    public DepositToBankAccountService(BankAccountRepositoryPort ar, OperationLogRepositoryPort al) {
        this.accountRepository = ar; this.auditLog = al;
    }
    public BankAccount execute(String accountNumber, Money amount, Long operatorUserId) {
        BankAccount account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new IllegalStateException("Account not found: " + accountNumber));
        BigDecimal before = account.getBalanceAmount();
        account.credit(amount);
        accountRepository.save(account);
        auditLog.save(OperationLog.create("DEPOSIT", operatorUserId, "TELLER_EMPLOYEE",
            accountNumber, Map.of("amount", amount.getAmount(), "balanceBefore", before,
                "balanceAfter", account.getBalanceAmount())));
        return account;
    }
}
