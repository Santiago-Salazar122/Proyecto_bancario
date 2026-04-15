package com.bank.domain.service;

import com.bank.domain.enums.UserRole;
import com.bank.domain.model.transfer.Transfer;
import com.bank.domain.model.user.User;
import com.bank.domain.ports.TransferRepositoryPort;
import com.bank.domain.ports.UserRepositoryPort;

public class RejectTransferService {

    private final TransferRepositoryPort transferRepository;
    private final UserRepositoryPort userRepository;

    public RejectTransferService(TransferRepositoryPort transferRepository, UserRepositoryPort userRepository) {
        this.transferRepository = transferRepository;
        this.userRepository = userRepository;
    }

    public Transfer execute(int transferId, int approverUserId) {
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

    private Transfer loadTransfer(int transferId) {
        return transferRepository.findById(transferId)
            .orElseThrow(() -> new IllegalStateException("Transfer not found: " + transferId));
    }

    private User requireUser(int userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
    }
}