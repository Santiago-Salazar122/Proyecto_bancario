package com.bank.domain.service;

import com.bank.domain.enums.UserRole;
import com.bank.domain.model.account.BankAccount;
import com.bank.domain.model.transfer.Transfer;
import com.bank.domain.model.user.User;
import com.bank.domain.ports.BankAccountRepositoryPort;
import com.bank.domain.ports.TransferRepositoryPort;
import com.bank.domain.ports.UserRepositoryPort;
import com.bank.domain.valueobject.Money;

public class ApproveTransferService {

    private final TransferRepositoryPort transferRepository;
    private final BankAccountRepositoryPort accountRepository;
    private final UserRepositoryPort userRepository;

    public ApproveTransferService(
        TransferRepositoryPort transferRepository,
        BankAccountRepositoryPort accountRepository,
        UserRepositoryPort userRepository
    ) {
        this.transferRepository = transferRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public Transfer execute(int transferId, int approverUserId) {
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

    private Transfer loadTransfer(int transferId) {
        return transferRepository.findById(transferId)
            .orElseThrow(() -> new IllegalStateException("Transfer not found: " + transferId));
    }

    private User requireUser(int userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
    }
}