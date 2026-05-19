package com.bank.domain.ports;

import com.bank.domain.model.audit.OperationLog;
import java.util.List;

/** Puerto del repositorio de bitácora. La implementación usa MongoDB. */
public interface OperationLogRepositoryPort {
    OperationLog save(OperationLog log);
    List<OperationLog> findByAffectedProductId(String productId);
    List<OperationLog> findByOperationType(String operationType);
    List<OperationLog> findByUserId(Long userId);
    List<OperationLog> findAll();
}
