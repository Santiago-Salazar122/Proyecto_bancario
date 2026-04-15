package com.bank.domain.service;

import com.bank.domain.model.transfer.Transfer;
import com.bank.domain.ports.TransferRepositoryPort;

import java.util.List;

public class ExpirePendingTransfersService {

    private final TransferRepositoryPort transferRepository;

    public ExpirePendingTransfersService(TransferRepositoryPort transferRepository) {
        this.transferRepository = transferRepository;
    }

    public void execute() {
        List<Transfer> pendingTransfers = transferRepository.findPendingApproval();
        for (Transfer transfer : pendingTransfers) {
            if (transfer.isExpired()) {
                transfer.markAsExpired();
                transferRepository.save(transfer);
            }
        }
    }
}