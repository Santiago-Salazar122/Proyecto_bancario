package com.bank.domain.ports;

import com.bank.domain.model.transfer.Transfer;

import java.util.List;
import java.util.Optional;

public interface TransferRepositoryPort {

    Optional<Transfer> findById(int transferId);

    boolean existsById(int transferId);

    List<Transfer> findPendingApproval();

    void save(Transfer transfer);
}

