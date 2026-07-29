package th.ac.dusit.dorm.tenant.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<TenantEntity, Long> {
    boolean existsByTenantCodeIgnoreCase(String tenantCode);
}

