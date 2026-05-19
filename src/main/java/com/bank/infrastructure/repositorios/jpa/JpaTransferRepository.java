package com.bank.infrastructure.repositorios.jpa;

import com.bank.domain.model.transfer.Transfer;
import com.bank.domain.enums.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** Repositorio JPA para Transfer → tabla "transfers" en MySQL. */
public interface JpaTransferRepository extends JpaRepository<Transfer, Long> {
    List<Transfer> findByStatus(TransferStatus status);
    List<Transfer> findBySourceAccount(String sourceAccount);
}
