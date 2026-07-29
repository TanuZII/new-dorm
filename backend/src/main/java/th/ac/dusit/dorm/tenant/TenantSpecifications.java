package th.ac.dusit.dorm.tenant;

import java.util.ArrayList;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import th.ac.dusit.dorm.tenant.persistence.TenantEntity;

final class TenantSpecifications {
    private TenantSpecifications() {
    }

    static Specification<TenantEntity> matches(String queryText, TenantType type, Boolean active) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (queryText != null && !queryText.isBlank()) {
                String pattern = "%" + queryText.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("tenantCode")), pattern),
                        builder.like(builder.lower(root.get("institutionalId")), pattern),
                        builder.like(builder.lower(root.get("firstName")), pattern),
                        builder.like(builder.lower(root.get("lastName")), pattern)));
            }
            if (type != null) predicates.add(builder.equal(root.get("tenantType"), type));
            if (active != null) predicates.add(builder.equal(root.get("active"), active));
            return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
