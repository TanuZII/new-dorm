package th.ac.dusit.dorm.tenant;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import th.ac.dusit.dorm.audit.AuditService;
import th.ac.dusit.dorm.common.DomainConflictException;
import th.ac.dusit.dorm.common.ResourceNotFoundException;
import th.ac.dusit.dorm.tenant.persistence.TenantEntity;
import th.ac.dusit.dorm.tenant.persistence.TenantRepository;

@Service
@Transactional(readOnly = true)
public class TenantService {
    private final TenantRepository repository;
    private final TenantCodeGenerator codeGenerator;
    private final AuditService auditService;

    public TenantService(
            TenantRepository repository,
            TenantCodeGenerator codeGenerator,
            AuditService auditService) {
        this.repository = repository;
        this.codeGenerator = codeGenerator;
        this.auditService = auditService;
    }

    @Transactional
    public TenantResponse create(CreateTenantRequest request, String actor, String ipAddress) {
        String identifier = validateIdentifier(request.tenantType(), request.institutionalId(), null);
        String code = codeGenerator.nextCode();
        var tenant = new TenantEntity(
                code, request.tenantType(), identifier, normalize(request.citizenId()),
                request.firstName(), request.lastName(), request.email(), request.phone());
        tenant.update(
                request.tenantType(), identifier, normalize(request.citizenId()),
                request.firstName(), request.lastName(), request.email(), request.phone(),
                request.addresses(), request.contacts());
        try {
            repository.save(tenant);
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw duplicateIdentifier(exception);
        }
        auditService.record(
                actor, "TENANT_CREATED", "TENANT", code, null, ipAddress,
                Map.of("tenantType", request.tenantType().name()));
        return TenantResponse.from(tenant);
    }

    public Page<TenantResponse> search(
            String query, TenantType type, Boolean active, Pageable pageable) {
        return repository.findAll(TenantSpecifications.matches(query, type, active), pageable)
                .map(TenantResponse::from);
    }

    public TenantResponse findById(long id) {
        return TenantResponse.from(findRequired(id));
    }

    @Transactional
    public TenantResponse update(
            long id, UpdateTenantRequest request, String actor, String ipAddress) {
        var tenant = findRequired(id);
        verifyVersion(tenant, request.version());
        String identifier = validateIdentifier(request.tenantType(), request.institutionalId(), id);
        tenant.update(
                request.tenantType(), identifier, normalize(request.citizenId()),
                request.firstName(), request.lastName(), request.email(), request.phone(),
                request.addresses(), request.contacts());
        try {
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw duplicateIdentifier(exception);
        }
        auditService.record(
                actor, "TENANT_UPDATED", "TENANT", tenant.getTenantCode(), null, ipAddress, Map.of());
        return TenantResponse.from(tenant);
    }

    @Transactional
    public TenantResponse changeStatus(
            long id, ChangeTenantStatusRequest request, String actor, String ipAddress) {
        var tenant = findRequired(id);
        verifyVersion(tenant, request.version());
        String reason = normalize(request.reason());
        if (!request.active() && reason == null) {
            throw new IllegalArgumentException("Deactivation reason is required");
        }
        tenant.changeStatus(request.active());
        repository.flush();
        auditService.record(
                actor, request.active() ? "TENANT_ACTIVATED" : "TENANT_DEACTIVATED",
                "TENANT", tenant.getTenantCode(), reason, ipAddress, Map.of());
        return TenantResponse.from(tenant);
    }

    private String validateIdentifier(TenantType type, String value, Long excludeId) {
        String identifier = normalize(value);
        if (type != TenantType.EXTERNAL && identifier == null) {
            throw new IllegalArgumentException("institutionalId is required for " + type);
        }
        boolean duplicate = identifier != null && (excludeId == null
                ? repository.existsByInstitutionalIdIgnoreCase(identifier)
                : repository.existsByInstitutionalIdIgnoreCaseAndIdNot(identifier, excludeId));
        if (duplicate) throw duplicateIdentifier(null);
        return identifier;
    }

    private TenantEntity findRequired(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant " + id + " not found"));
    }

    private void verifyVersion(TenantEntity tenant, Long requestedVersion) {
        if (requestedVersion == null || !requestedVersion.equals(tenant.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(TenantEntity.class, tenant.getId());
        }
    }

    private DomainConflictException duplicateIdentifier(Exception cause) {
        var conflict = new DomainConflictException(
                "TENANT_IDENTIFIER_DUPLICATE", "Institutional identifier already exists");
        if (cause != null) conflict.initCause(cause);
        return conflict;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
