package th.ac.dusit.dorm.masterdata;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import th.ac.dusit.dorm.audit.AuditService;
import th.ac.dusit.dorm.common.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
public class MasterDataService {

    private static final Map<MasterDataType, MasterDataType> PARENT_TYPES = Map.of(
            MasterDataType.PROVINCE, MasterDataType.COUNTRY,
            MasterDataType.DISTRICT, MasterDataType.PROVINCE,
            MasterDataType.SUBDISTRICT, MasterDataType.DISTRICT,
            MasterDataType.POSTAL_CODE, MasterDataType.SUBDISTRICT,
            MasterDataType.MAJOR, MasterDataType.FACULTY);

    private final MasterDataRepository repository;
    private final AuditService auditService;

    public MasterDataService(MasterDataRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @Transactional
    public MasterDataResponse create(
            MasterDataType type,
            CreateMasterDataRequest request,
            String actor,
            String ipAddress) {
        String code = normalizeCode(request.code());
        validateDates(request.effectiveFrom(), request.effectiveTo());
        validateParent(type, request.parentId());
        validateNoOverlap(type, code, null, request.effectiveFrom(), request.effectiveTo());
        var saved = repository.save(new MasterDataEntity(
                type,
                code,
                request.nameTh().trim(),
                normalizeNullable(request.nameEn()),
                request.parentId(),
                request.effectiveFrom(),
                request.effectiveTo()));
        auditService.record(
                actor,
                "MASTER_DATA_CREATED",
                "MASTER_DATA",
                saved.getId().toString(),
                null,
                ipAddress,
                Map.of("type", type.name(), "code", code));
        return MasterDataResponse.from(saved);
    }

    @Transactional
    public int importAll(
            List<MasterDataImportItem> items,
            String actor,
            String ipAddress) {
        var effectiveKeys = new HashSet<String>();
        for (var item : items) {
            String code = normalizeCode(item.code());
            validateDates(item.effectiveFrom(), item.effectiveTo());
            validateParent(item.type(), item.parentId());
            validateNoOverlap(item.type(), code, null, item.effectiveFrom(), item.effectiveTo());
            String key = item.type() + "|" + code + "|" + item.effectiveFrom();
            if (!effectiveKeys.add(key)) {
                throw new IllegalStateException("Duplicate master data row " + key);
            }
        }
        var entities = items.stream().map(item -> new MasterDataEntity(
                item.type(),
                normalizeCode(item.code()),
                item.nameTh().trim(),
                normalizeNullable(item.nameEn()),
                item.parentId(),
                item.effectiveFrom(),
                item.effectiveTo())).toList();
        repository.saveAll(entities);
        auditService.record(
                actor,
                "MASTER_DATA_IMPORTED",
                "MASTER_DATA_IMPORT",
                null,
                null,
                ipAddress,
                Map.of("rowCount", entities.size()));
        return entities.size();
    }

    public Page<MasterDataResponse> findAll(
            MasterDataType type,
            String query,
            Boolean active,
            LocalDate effectiveOn,
            Long parentId,
            Pageable pageable) {
        return repository.findAll(
                        MasterDataSpecifications.matches(type, query, active, effectiveOn, parentId),
                        pageable)
                .map(MasterDataResponse::from);
    }

    @Transactional
    public MasterDataResponse update(
            MasterDataType type,
            long id,
            UpdateMasterDataRequest request,
            String actor,
            String ipAddress) {
        var entity = findRequired(type, id);
        verifyVersion(entity, request.version());
        String code = normalizeCode(request.code());
        validateDates(request.effectiveFrom(), request.effectiveTo());
        validateParent(type, request.parentId());
        validateNoOverlap(type, code, id, request.effectiveFrom(), request.effectiveTo());
        entity.update(
                code,
                request.nameTh().trim(),
                normalizeNullable(request.nameEn()),
                request.parentId(),
                request.effectiveFrom(),
                request.effectiveTo());
        repository.flush();
        auditService.record(
                actor,
                "MASTER_DATA_UPDATED",
                "MASTER_DATA",
                Long.toString(id),
                null,
                ipAddress,
                Map.of("type", type.name(), "code", code));
        return MasterDataResponse.from(entity);
    }

    @Transactional
    public MasterDataResponse changeStatus(
            MasterDataType type,
            long id,
            ChangeMasterDataStatusRequest request,
            String actor,
            String ipAddress) {
        var entity = findRequired(type, id);
        verifyVersion(entity, request.version());
        String reason = normalizeNullable(request.reason());
        if (!request.active() && reason == null) {
            throw new IllegalArgumentException("Deactivation reason is required");
        }
        entity.changeStatus(request.active(), reason);
        repository.flush();
        auditService.record(
                actor,
                request.active() ? "MASTER_DATA_ACTIVATED" : "MASTER_DATA_DEACTIVATED",
                "MASTER_DATA",
                Long.toString(id),
                reason,
                ipAddress,
                Map.of("type", type.name(), "code", entity.getCode()));
        return MasterDataResponse.from(entity);
    }

    private MasterDataEntity findRequired(MasterDataType type, long id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Master data " + id + " not found"));
        if (entity.getType() != type) {
            throw new ResourceNotFoundException("Master data " + id + " not found for " + type);
        }
        return entity;
    }

    private void validateParent(MasterDataType type, Long parentId) {
        MasterDataType expected = PARENT_TYPES.get(type);
        if (expected == null) {
            if (parentId != null) {
                throw new IllegalArgumentException(type + " does not support a parent");
            }
            return;
        }
        if (parentId == null) {
            throw new IllegalArgumentException(type + " requires parent type " + expected);
        }
        var parent = repository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent " + parentId + " not found"));
        if (parent.getType() != expected) {
            throw new IllegalArgumentException(type + " requires parent type " + expected);
        }
    }

    private void validateNoOverlap(
            MasterDataType type,
            String code,
            Long excludeId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo) {
        if (repository.countOverlapping(type, code, excludeId, effectiveFrom, effectiveTo) > 0) {
            throw new IllegalStateException(
                    "Effective dates overlap an existing " + type + " code " + code);
        }
    }

    private void validateDates(LocalDate effectiveFrom, LocalDate effectiveTo) {
        if (effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
            throw new IllegalArgumentException("effectiveTo must be on or after effectiveFrom");
        }
    }

    private void verifyVersion(MasterDataEntity entity, Long requestedVersion) {
        if (requestedVersion == null || !requestedVersion.equals(entity.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(MasterDataEntity.class, entity.getId());
        }
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
