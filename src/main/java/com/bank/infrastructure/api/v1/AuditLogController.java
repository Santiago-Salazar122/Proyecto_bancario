package com.bank.infrastructure.api.v1;

import com.bank.domain.model.audit.OperationLog;
import com.bank.domain.ports.OperationLogRepositoryPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller REST para consultar la Bitácora de MongoDB.
 * URL base: /api/v1/audit
 * Solo lectura — las escrituras ocurren automáticamente en los servicios.
 */
@RestController
@RequestMapping("/api/v1/audit")
public class AuditLogController {

    private final OperationLogRepositoryPort auditLogRepository;
    public AuditLogController(OperationLogRepositoryPort auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public ResponseEntity<List<OperationLog>> listAll() {
        return ResponseEntity.ok(auditLogRepository.findAll());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<OperationLog>> findByProduct(@PathVariable String productId) {
        return ResponseEntity.ok(auditLogRepository.findByAffectedProductId(productId));
    }

    @GetMapping("/type/{operationType}")
    public ResponseEntity<List<OperationLog>> findByType(@PathVariable String operationType) {
        return ResponseEntity.ok(auditLogRepository.findByOperationType(operationType));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OperationLog>> findByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(auditLogRepository.findByUserId(userId));
    }
}
