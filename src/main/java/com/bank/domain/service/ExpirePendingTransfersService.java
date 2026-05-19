package com.bank.domain.service;

import com.bank.domain.model.audit.OperationLog;
import com.bank.domain.model.transfer.Transfer;
import com.bank.domain.ports.OperationLogRepositoryPort;
import com.bank.domain.ports.TransferRepositoryPort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Servicio de dominio: Vencer transferencias que llevan 60+ minutos en PENDING_APPROVAL.
 *
 * @Scheduled(fixedDelay=300000) = se ejecuta automáticamente cada 5 minutos.
 * Requiere @EnableScheduling en BankApplication.
 */
@Service
public class ExpirePendingTransfersService {
    private final TransferRepositoryPort transferRepository;
    private final OperationLogRepositoryPort auditLog;
    public ExpirePendingTransfersService(TransferRepositoryPort tr, OperationLogRepositoryPort al) {
        this.transferRepository = tr; this.auditLog = al;
    }

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void execute() {
        List<Transfer> pendingTransfers = transferRepository.findPendingApproval();
        for (Transfer transfer : pendingTransfers) {
            if (transfer.isExpired()) {
                transfer.markAsExpired();
                transferRepository.save(transfer);
                auditLog.save(OperationLog.create("TRANSFER_EXPIRED",
                    transfer.getCreatorUserId(), "SYSTEM",
                    String.valueOf(transfer.getTransferId()),
                    Map.of("reason", "Approval time limit exceeded (60 minutes)",
                        "creationDate", transfer.getCreationDate().toString(),
                        "expiryDate", LocalDateTime.now().toString(),
                        "amount", transfer.getAmount())));
            }
        }
    }
}
