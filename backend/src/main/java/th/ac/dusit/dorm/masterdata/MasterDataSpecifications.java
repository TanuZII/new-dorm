package th.ac.dusit.dorm.masterdata;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

final class MasterDataSpecifications {

    private MasterDataSpecifications() {
    }

    static Specification<MasterDataEntity> matches(
            MasterDataType type,
            String queryText,
            Boolean active,
            LocalDate effectiveOn,
            Long parentId) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(criteriaBuilder.equal(root.get("type"), type));
            if (queryText != null && !queryText.isBlank()) {
                String pattern = "%" + queryText.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("nameTh")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("nameEn")), pattern)));
            }
            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }
            if (effectiveOn != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("effectiveFrom"), effectiveOn));
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("effectiveTo")),
                        criteriaBuilder.greaterThanOrEqualTo(root.get("effectiveTo"), effectiveOn)));
            }
            if (parentId != null) {
                predicates.add(criteriaBuilder.equal(root.get("parentId"), parentId));
            }
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
