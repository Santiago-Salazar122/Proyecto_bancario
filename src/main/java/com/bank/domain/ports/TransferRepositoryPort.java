package com.bank.domain.ports;

import com.bank.domain.model.transfer.Transfer;
import com.bank.domain.enums.TransferStatus;
import java.util.List;
import java.util.Optional;

public interface TransferRepositoryPort {
    Transfer save(Transfer transfer);
    Optional<Transfer> findById(Long transferId);
    List<Transfer> findPendingApproval();
    List<Transfer> findByStatus(TransferStatus status);
    List<Transfer> findBySourceAccount(String accountNumber);
    List<Transfer> findAll();
}
