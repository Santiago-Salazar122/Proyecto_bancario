package com.bank.domain.service;

import com.bank.domain.enums.UserRole;
import com.bank.domain.model.audit.OperationLog;
import com.bank.domain.model.transfer.Transfer;
import com.bank.domain.model.user.User;
import com.bank.domain.ports.OperationLogRepositoryPort;
import com.bank.domain.ports.TransferRepositoryPort;
import com.bank.domain.ports.UserRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

/** Servicio de dominio: Rechazar una transferencia. Transición: PENDING_APPROVAL → REJECTED. */
@Service @Transactional
public class RejectTransferService {
    private final TransferRepositoryPort transferRepository;
    private final UserRepositoryPort userRepository;
    private final OperationLogRepositoryPort auditLog;
    public RejectTransferService(TransferRepositoryPort tr, UserRepositoryPort ur, OperationLogRepositoryPort al) {
        this.transferRepository = tr; this.userRepository = ur; this.auditLog = al;
    }
    public Transfer execute(Long transferId, Long approverUserId) {
        User approver = userRepository.findById(approverUserId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + approverUserId));
        if (!approver.hasRole(UserRole.COMPANY_SUPERVISOR))
            throw new IllegalStateException("Only Company Supervisors can reject transfers.");
        Transfer transfer = transferRepository.findById(transferId)
            .orElseThrow(() -> new IllegalStateException("Transfer not found: " + transferId));
        transfer.reject(approverUserId);
        transferRepository.save(transfer);
        auditLog.save(OperationLog.create("TRANSFER_REJECTED", approverUserId,
            UserRole.COMPANY_SUPERVISOR.name(), String.valueOf(transferId),
            Map.of("reason", "Rejected by supervisor")));
        return transfer;
    }
}
