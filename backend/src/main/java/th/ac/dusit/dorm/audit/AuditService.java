package th.ac.dusit.dorm.audit;

import java.time.Instant;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import th.ac.dusit.dorm.platform.TraceIdFilter;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(
            String actor,
            String action,
            String entityType,
            String entityId,
            String reason,
            String ipAddress,
            Map<String, Object> details) {
        repository.save(new AuditLogEntity(
                actor,
                action,
                entityType,
                entityId,
                reason,
                ipAddress,
                MDC.get(TraceIdFilter.MDC_KEY),
                details,
                Instant.now()));
    }
}
