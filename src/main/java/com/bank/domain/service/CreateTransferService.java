package com.bank.domain.service;

import com.bank.domain.enums.UserRole;
import com.bank.domain.model.account.BankAccount;
import com.bank.domain.model.audit.OperationLog;
import com.bank.domain.model.transfer.Transfer;
import com.bank.domain.model.user.User;
import com.bank.domain.ports.BankAccountRepositoryPort;
import com.bank.domain.ports.OperationLogRepositoryPort;
import com.bank.domain.ports.TransferRepositoryPort;
import com.bank.domain.ports.UserRepositoryPort;
import com.bank.domain.valueobject.Money;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Servicio de dominio: Crear una transferencia bancaria.
 *
 * Si el creador es COMPANY_EMPLOYEE/COMPANY_CLIENT/COMPANY_SUPERVISOR
 * Y el monto > umbral → PENDING_APPROVAL (espera aprobación, NO mueve fondos).
 * Caso contrario → EXECUTED (fondos movidos inmediatamente).
 */
@Service
@Transactional
public class CreateTransferService {
    private final TransferRepositoryPort transferRepository;
    private final BankAccountRepositoryPort accountRepository;
    private final UserRepositoryPort userRepository;
    private final OperationLogRepositoryPort auditLog;

    @Value("${bank.transfer.approval-threshold:5000000}")
    private BigDecimal approvalThreshold;

    public CreateTransferService(TransferRepositoryPort tr, BankAccountRepositoryPort ar,
                                  UserRepositoryPort ur, OperationLogRepositoryPort al) {
        this.transferRepository = tr; this.accountRepository = ar;
        this.userRepository = ur; this.auditLog = al;
    }

    public Transfer execute(String sourceAccountNumber, String targetAccountNumber,
                             Money amount, Long creatorUserId) {
        User creator = userRepository.findById(creatorUserId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + creatorUserId));
        if (!creator.canOperate())
            throw new IllegalStateException("The creator user cannot operate.");

        BankAccount sourceAccount = accountRepository.findByAccountNumber(sourceAccountNumber)
            .orElseThrow(() -> new IllegalStateException("Source account not found: " + sourceAccountNumber));
        if (!sourceAccount.isOperational())
            throw new IllegalStateException("Source account is not operational: " + sourceAccountNumber);

        BankAccount targetAccount = accountRepository.findByAccountNumber(targetAccountNumber)
            .orElseThrow(() -> new IllegalStateException("Target account not found: " + targetAccountNumber));

        boolean requiresApproval = requiresSupervisorApproval(creator, amount);
        Transfer transfer = new Transfer(sourceAccountNumber, targetAccountNumber, amount, creatorUserId, requiresApproval);

        if (!requiresApproval) {
            sourceAccount.debit(amount);
            targetAccount.credit(amount);
            accountRepository.save(sourceAccount);
            accountRepository.save(targetAccount);
            auditLog.save(OperationLog.create("TRANSFER_EXECUTED", creatorUserId,
                creator.getRole().name(), String.valueOf(transfer.getTransferId()),
                Map.of("amount", amount.getAmount(), "sourceAccount", sourceAccountNumber,
                    "targetAccount", targetAccountNumber)));
        } else {
            auditLog.save(OperationLog.create("TRANSFER_PENDING_APPROVAL", creatorUserId,
                creator.getRole().name(), String.valueOf(transfer.getTransferId()),
                Map.of("amount", amount.getAmount(), "approvalThreshold", approvalThreshold)));
        }

        transferRepository.save(transfer);
        return transfer;
    }

    private boolean requiresSupervisorApproval(User creator, Money amount) {
        boolean companyFlow = creator.hasRole(UserRole.COMPANY_CLIENT)
            || creator.hasRole(UserRole.COMPANY_EMPLOYEE)
            || creator.hasRole(UserRole.COMPANY_SUPERVISOR);
        return companyFlow && amount.isGreaterThan(new Money(approvalThreshold));
    }
}
