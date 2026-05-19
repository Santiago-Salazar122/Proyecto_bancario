package com.bank.infrastructure.repositorios.adapter;

import com.bank.domain.model.transfer.Transfer;
import com.bank.domain.enums.TransferStatus;
import com.bank.domain.ports.TransferRepositoryPort;
import com.bank.infrastructure.repositorios.jpa.JpaTransferRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

/** Adaptador: TransferRepositoryPort → JPA (MySQL). */
@Component
public class TransferRepositoryAdapter implements TransferRepositoryPort {
    private final JpaTransferRepository jpa;
    public TransferRepositoryAdapter(JpaTransferRepository jpa) { this.jpa = jpa; }

    @Override public Transfer save(Transfer t)                           { return jpa.save(t); }
    @Override public Optional<Transfer> findById(Long id)                { return jpa.findById(id); }
    @Override public List<Transfer> findPendingApproval()                { return jpa.findByStatus(TransferStatus.PENDING_APPROVAL); }
    @Override public List<Transfer> findByStatus(TransferStatus s)       { return jpa.findByStatus(s); }
    @Override public List<Transfer> findBySourceAccount(String a)        { return jpa.findBySourceAccount(a); }
    @Override public List<Transfer> findAll()                            { return jpa.findAll(); }
}
