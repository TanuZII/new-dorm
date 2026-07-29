package th.ac.dusit.dorm.tenant.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TenantRepository
        extends JpaRepository<TenantEntity, Long>, JpaSpecificationExecutor<TenantEntity> {
    boolean existsByInstitutionalIdIgnoreCase(String institutionalId);
    boolean existsByInstitutionalIdIgnoreCaseAndIdNot(String institutionalId, Long id);
}
