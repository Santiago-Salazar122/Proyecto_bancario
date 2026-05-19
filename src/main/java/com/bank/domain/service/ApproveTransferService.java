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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Servicio de dominio: Aprobar una transferencia en espera.
 * Solo COMPANY_SUPERVISOR puede ejecutarlo.
 * Transición: PENDING_APPROVAL → EXECUTED. Mueve los fondos.
 */
@Service @Transactional
public class ApproveTransferService {
    private final TransferRepositoryPort transferRepository;
    private final BankAccountRepositoryPort accountRepository;
    private final UserRepositoryPort userRepository;
    private final OperationLogRepositoryPort auditLog;

    public ApproveTransferService(TransferRepositoryPort tr, BankAccountRepositoryPort ar,
                                   UserRepositoryPort ur, OperationLogRepositoryPort al) {
        this.transferRepository = tr; this.accountRepository = ar;
        this.userRepository = ur; this.auditLog = al;
    }

    public Transfer execute(Long transferId, Long approverUserId) {
        User approver = userRepository.findById(approverUserId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + approverUserId));
        if (!approver.hasRole(UserRole.COMPANY_SUPERVISOR))
            throw new IllegalStateException("Only users with Company Supervisor role can approve transfers.");
        if (!approver.canOperate())
            throw new IllegalStateException("The approver user cannot operate.");

        Transfer transfer = transferRepository.findById(transferId)
            .orElseThrow(() -> new IllegalStateException("Transfer not found: " + transferId));

        BankAccount sourceAccount = accountRepository.findByAccountNumber(transfer.getSourceAccount())
            .orElseThrow(() -> new IllegalStateException("Source account not found: " + transfer.getSourceAccount()));
        BankAccount targetAccount = accountRepository.findByAccountNumber(transfer.getTargetAccount())
            .orElseThrow(() -> new IllegalStateException("Target account not found: " + transfer.getTargetAccount()));

        BigDecimal beforeSource = sourceAccount.getBalanceAmount();
        BigDecimal beforeTarget = targetAccount.getBalanceAmount();

        transfer.approve(approverUserId);
        sourceAccount.debit(transfer.getAmountAsMoney());
        targetAccount.credit(transfer.getAmountAsMoney());
        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);
        transferRepository.save(transfer);

        auditLog.save(OperationLog.create("TRANSFER_APPROVED_AND_EXECUTED", approverUserId,
            UserRole.COMPANY_SUPERVISOR.name(), String.valueOf(transferId),
            Map.of("amount", transfer.getAmount(),
                "balanceBeforeSource", beforeSource, "balanceAfterSource", sourceAccount.getBalanceAmount(),
                "balanceBeforeTarget", beforeTarget, "balanceAfterTarget", targetAccount.getBalanceAmount())));
        return transfer;
    }
}
