package th.ac.dusit.dorm.audit;

import java.time.Instant;
import java.util.Map;

public record AuditLogResponse(
        Long id,
        String actor,
        String action,
        String entityType,
        String entityId,
        String reason,
        String ipAddress,
        String traceId,
        Map<String, Object> details,
        Instant createdAt) {

    static AuditLogResponse from(AuditLogEntity entity) {
        return new AuditLogResponse(
                entity.getId(),
                entity.getActor(),
                entity.getAction(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getReason(),
                entity.getIpAddress(),
                entity.getTraceId(),
                entity.getDetails() == null ? Map.of() : entity.getDetails(),
                entity.getCreatedAt());
    }
}
