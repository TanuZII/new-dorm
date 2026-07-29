package th.ac.dusit.dorm.audit;

import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public Page<AuditLogResponse> findAll(
            String actor,
            String action,
            String entityType,
            Instant from,
            Instant to,
            Pageable pageable) {
        if (from != null && to != null && !from.isBefore(to)) {
            throw new IllegalArgumentException("from must be before to");
        }
        return repository.findAll(
                        AuditLogSpecifications.matches(actor, action, entityType, from, to),
                        pageable)
                .map(AuditLogResponse::from);
    }
}
