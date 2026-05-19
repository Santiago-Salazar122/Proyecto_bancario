package com.bank.infrastructure.repositorios.mongodb;

import com.bank.domain.model.audit.OperationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

/**
 * Repositorio MongoDB para la Bitácora de Operaciones.
 *
 * MongoRepository funciona igual que JpaRepository pero para MongoDB.
 * Spring Data genera la implementación automáticamente.
 *
 * Diferencia clave con JPA:
 * - JpaRepository   → MySQL (tablas relacionales con esquema fijo)
 * - MongoRepository → MongoDB (colecciones con documentos JSON flexibles)
 *
 * El tipo del ID es String porque MongoDB usa ObjectId (hex de 24 chars).
 */
public interface MongoOperationLogRepository extends MongoRepository<OperationLog, String> {
    List<OperationLog> findByAffectedProductId(String affectedProductId);
    List<OperationLog> findByOperationType(String operationType);
    List<OperationLog> findByUserId(Long userId);
}
