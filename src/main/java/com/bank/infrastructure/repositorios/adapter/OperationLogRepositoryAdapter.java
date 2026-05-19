package com.bank.infrastructure.repositorios.adapter;

import com.bank.domain.model.audit.OperationLog;
import com.bank.domain.ports.OperationLogRepositoryPort;
import com.bank.infrastructure.repositorios.mongodb.MongoOperationLogRepository;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Adaptador: OperationLogRepositoryPort → MongoDB.
 * Este es el ÚNICO adaptador que usa MongoDB en lugar de JPA.
 * La Bitácora va a MongoDB porque sus "details" tienen estructura variable.
 */
@Component
public class OperationLogRepositoryAdapter implements OperationLogRepositoryPort {
    private final MongoOperationLogRepository mongo;
    public OperationLogRepositoryAdapter(MongoOperationLogRepository mongo) { this.mongo = mongo; }

    @Override public OperationLog save(OperationLog log)                     { return mongo.save(log); }
    @Override public List<OperationLog> findByAffectedProductId(String id)   { return mongo.findByAffectedProductId(id); }
    @Override public List<OperationLog> findByOperationType(String type)     { return mongo.findByOperationType(type); }
    @Override public List<OperationLog> findByUserId(Long userId)            { return mongo.findByUserId(userId); }
    @Override public List<OperationLog> findAll()                            { return mongo.findAll(); }
}
