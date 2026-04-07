package com.bank.application.service;

import com.bank.application.port.in.TransferUseCase;
import com.bank.application.port.out.BankAccountRepositoryPort;
import com.bank.application.port.out.TransferRepositoryPort;
import com.bank.application.port.out.UserRepositoryPort;
import com.bank.domain.enums.UserRole;
import com.bank.domain.model.account.BankAccount;
import com.bank.domain.model.transfer.Transfer;
import com.bank.domain.model.user.User;
import com.bank.domain.valueobject.Money;

import java.util.List;

public class TransferApplicationService implements TransferUseCase {

    private final TransferRepositoryPort transferRepository;
    private final BankAccountRepositoryPort accountRepository;
    private final UserRepositoryPort userRepository;
    private final Money approvalThreshold;

    public TransferApplicationService(
        TransferRepositoryPort transferRepository,
        BankAccountRepositoryPort accountRepository,
        UserRepositoryPort userRepository,
        Money approvalThreshold
    ) {
        this.transferRepository = transferRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.approvalThreshold = approvalThreshold;
    }

    @Override
    public Transfer createTransfer(int transferId, String sourceAccount, String targetAccount, Money amount, int creatorUserId) {
        if (transferRepository.existsById(transferId)) {
            throw new IllegalStateException("Transfer ID already exists: " + transferId);
        }

        User creator = requireUser(creatorUserId);
        if (!creator.canOperate()) {
            throw new IllegalStateException("The creator user cannot operate.");
        }

        boolean requiresApproval = requiresSupervisorApproval(creator, amount);
        Transfer transfer = new Transfer(transferId, sourceAccount, targetAccount, amount, creatorUserId, requiresApproval);

        if (!requiresApproval) {
            executeFundsMovement(sourceAccount, targetAccount, amount);
        }

        transferRepository.save(transfer);
        return transfer;
    }

    @Override
    public Transfer approveTransfer(int transferId, int approverUserId) {
        User approver = requireUser(approverUserId);
        if (!approver.hasRole(UserRole.COMPANY_SUPERVISOR)) {
            throw new IllegalStateException("Only users with Company Supervisor role can approve transfers.");
        }
        if (!approver.canOperate()) {
            throw new IllegalStateException("The approver user cannot operate.");
        }

        Transfer transfer = loadTransfer(transferId);
        transfer.approve(approverUserId);
        executeFundsMovement(transfer.getSourceAccount(), transfer.getTargetAccount(), transfer.getAmount());
        transferRepository.save(transfer);
        return transfer;
    }

    @Override
    public Transfer rejectTransfer(int transferId, int approverUserId) {
        User approver = requireUser(approverUserId);
        if (!approver.hasRole(UserRole.COMPANY_SUPERVISOR)) {
            throw new IllegalStateException("Only users with Company Supervisor role can reject transfers.");
        }
        if (!approver.canOperate()) {
            throw new IllegalStateException("The approver user cannot operate.");
        }

        Transfer transfer = loadTransfer(transferId);
        transfer.reject(approverUserId);
        transferRepository.save(transfer);
        return transfer;
    }

    @Override
    public void expirePendingTransfers() {
        List<Transfer> pendingTransfers = transferRepository.findPendingApproval();
        for (Transfer transfer : pendingTransfers) {
            if (transfer.isExpired()) {
                transfer.markAsExpired();
                transferRepository.save(transfer);
            }
        }
    }

    private void executeFundsMovement(String sourceAccountNumber, String targetAccountNumber, Money amount) {
        BankAccount sourceAccount = accountRepository.findByAccountNumber(sourceAccountNumber)
            .orElseThrow(() -> new IllegalStateException("Source account not found: " + sourceAccountNumber));

        BankAccount targetAccount = accountRepository.findByAccountNumber(targetAccountNumber)
            .orElseThrow(() -> new IllegalStateException("Target account not found: " + targetAccountNumber));

        sourceAccount.debit(amount);
        targetAccount.credit(amount);

        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);
    }

    private boolean requiresSupervisorApproval(User creator, Money amount) {
        boolean companyFlow = creator.hasRole(UserRole.COMPANY_CLIENT)
            || creator.hasRole(UserRole.COMPANY_EMPLOYEE)
            || creator.hasRole(UserRole.COMPANY_SUPERVISOR);

        return companyFlow && amount.isGreaterThan(approvalThreshold);
    }

    private Transfer loadTransfer(int transferId) {
        return transferRepository.findById(transferId)
            .orElseThrow(() -> new IllegalStateException("Transfer not found: " + transferId));
    }

    private User requireUser(int userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
    }
}
