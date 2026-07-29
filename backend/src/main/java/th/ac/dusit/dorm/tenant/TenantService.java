package th.ac.dusit.dorm.tenant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import th.ac.dusit.dorm.tenant.persistence.TenantEntity;
import th.ac.dusit.dorm.tenant.persistence.TenantRepository;

@Service
@Transactional(readOnly = true)
public class TenantService {
    private final TenantRepository repository;

    public TenantService(TenantRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TenantResponse create(CreateTenantRequest request) {
        String code = request.tenantCode().trim().toUpperCase();
        if (repository.existsByTenantCodeIgnoreCase(code)) {
            throw new IllegalStateException("Tenant " + code + " already exists");
        }
        var tenant = new TenantEntity(
                code, request.tenantType(), request.firstName().trim(), request.lastName().trim(),
                normalize(request.email()), normalize(request.phone()));
        return TenantResponse.from(repository.save(tenant));
    }

    public Page<TenantResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(TenantResponse::from);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

