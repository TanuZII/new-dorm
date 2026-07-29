package th.ac.dusit.dorm.audit;

import org.slf4j.MDC;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import th.ac.dusit.dorm.platform.TraceIdFilter;

@Service
public class AuditService {

    private final JdbcTemplate jdbcTemplate;

    public AuditService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(
            String actor,
            String action,
            String entityType,
            String entityId,
            String reason,
            String ipAddress,
            String details) {
        jdbcTemplate.update("""
                INSERT INTO audit_logs
                    (actor, action, entity_type, entity_id, reason, ip_address, trace_id, details)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                actor,
                action,
                entityType,
                entityId,
                reason,
                ipAddress,
                MDC.get(TraceIdFilter.MDC_KEY),
                details == null ? "{}" : details);
    }
}
