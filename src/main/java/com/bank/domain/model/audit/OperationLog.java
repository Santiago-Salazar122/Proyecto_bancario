package com.bank.domain.model.audit;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Documento de la Bitácora de Operaciones.
 * Se almacena en MongoDB (NO en MySQL).
 *
 * @Document(collection = "operation_log") indica que esta clase
 * se guarda en la colección "operation_log" de MongoDB.
 *
 * ¿Por qué MongoDB aquí y no MySQL?
 * - El campo "details" tiene estructura variable:
 *   una transferencia registra saldos antes/después,
 *   una aprobación de préstamo registra tasa y analista,
 *   un vencimiento registra el motivo.
 * - MongoDB guarda JSON flexible. MySQL requiere columnas fijas.
 *
 * Propósito: SOLO auditoría e histórico.
 * NUNCA se usa para calcular saldos. Los saldos siempre vienen de MySQL.
 */
@Document(collection = "operation_log")
public class OperationLog {

    /**
     * ID del documento en MongoDB.
     * MongoDB genera automáticamente un ObjectId (24 chars hex) si no se asigna.
     */
    @Id
    private String id;

    /**
     * Tipo de operación. Ejemplos:
     * LOAN_REQUESTED, LOAN_APPROVED, LOAN_REJECTED, LOAN_DISBURSED,
     * TRANSFER_EXECUTED, TRANSFER_PENDING_APPROVAL,
     * TRANSFER_APPROVED_AND_EXECUTED, TRANSFER_REJECTED, TRANSFER_EXPIRED,
     * ACCOUNT_OPENED, ACCOUNT_BLOCKED, ACCOUNT_REACTIVATED, ACCOUNT_CANCELLED,
     * DEPOSIT, WITHDRAWAL
     */
    @Field("operation_type")
    private String operationType;

    /** Momento exacto en que ocurrió la operación. */
    @Field("timestamp")
    private LocalDateTime timestamp;

    /** ID del usuario que ejecutó la acción. */
    @Field("user_id")
    private Long userId;

    /** Rol del usuario en el momento de la operación. */
    @Field("user_role")
    private String userRole;

    /**
     * Referencia al producto afectado.
     * Puede ser: número de cuenta, ID de préstamo, ID de transferencia.
     */
    @Field("affected_product_id")
    private String affectedProductId;

    /**
     * Datos variables según el tipo de operación.
     * MongoDB guarda esto como sub-documento JSON flexible.
     *
     * Ejemplos de contenido:
     * - Transferencia: {amount, balanceBeforeSource, balanceAfterSource, ...}
     * - Préstamo:      {approvedAmount, interestRate, previousStatus, newStatus}
     * - Vencimiento:   {reason, expiryDate, creatorUserId}
     */
    @Field("details")
    private Map<String, Object> details;

    public OperationLog() {}

    public OperationLog(String operationType, LocalDateTime timestamp,
                        Long userId, String userRole,
                        String affectedProductId, Map<String, Object> details) {
        this.operationType = operationType;
        this.timestamp = timestamp;
        this.userId = userId;
        this.userRole = userRole;
        this.affectedProductId = affectedProductId;
        this.details = details;
    }

    /**
     * Método de fábrica estático para crear un registro de bitácora.
     * El timestamp se establece automáticamente con la hora actual.
     */
    public static OperationLog create(String operationType, Long userId,
                                       String userRole, String affectedProductId,
                                       Map<String, Object> details) {
        return new OperationLog(operationType, LocalDateTime.now(),
            userId, userRole, affectedProductId, details);
    }

    // ═══════════════════ GETTERS Y SETTERS ═══════════════════

    public String getId()                       { return id; }
    public void setId(String id)                { this.id = id; }
    public String getOperationType()            { return operationType; }
    public void setOperationType(String t)      { this.operationType = t; }
    public LocalDateTime getTimestamp()         { return timestamp; }
    public void setTimestamp(LocalDateTime t)   { this.timestamp = t; }
    public Long getUserId()                     { return userId; }
    public void setUserId(Long userId)          { this.userId = userId; }
    public String getUserRole()                 { return userRole; }
    public void setUserRole(String r)           { this.userRole = r; }
    public String getAffectedProductId()        { return affectedProductId; }
    public void setAffectedProductId(String id) { this.affectedProductId = id; }
    public Map<String, Object> getDetails()     { return details; }
    public void setDetails(Map<String, Object> d){ this.details = d; }
}
