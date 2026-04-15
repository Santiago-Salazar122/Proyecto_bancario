package com.bank.domain.service;

import com.bank.domain.enums.UserRole;
import com.bank.domain.model.account.BankAccount;
import com.bank.domain.model.transfer.Transfer;
import com.bank.domain.model.user.User;
import com.bank.domain.ports.BankAccountRepositoryPort;
import com.bank.domain.ports.TransferRepositoryPort;
import com.bank.domain.ports.UserRepositoryPort;
import com.bank.domain.valueobject.Money;

public class CreateTransferService {

    private final TransferRepositoryPort transferRepository;
    private final BankAccountRepositoryPort accountRepository;
    private final UserRepositoryPort userRepository;
    private final Money approvalThreshold;

    public CreateTransferService(
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

    public Transfer execute(int transferId, String sourceAccount, String targetAccount, Money amount, int creatorUserId) {
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

    private User requireUser(int userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
    }
}