package th.ac.dusit.dorm.audit;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

final class AuditLogSpecifications {

    private AuditLogSpecifications() {
    }

    static Specification<AuditLogEntity> matches(
            String actor,
            String action,
            String entityType,
            Instant from,
            Instant to) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (hasText(actor)) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("actor")),
                        actor.trim().toLowerCase(Locale.ROOT)));
            }
            if (hasText(action)) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("action")),
                        action.trim().toUpperCase(Locale.ROOT)));
            }
            if (hasText(entityType)) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("entityType")),
                        entityType.trim().toUpperCase(Locale.ROOT)));
            }
            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("createdAt"), to));
            }
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
