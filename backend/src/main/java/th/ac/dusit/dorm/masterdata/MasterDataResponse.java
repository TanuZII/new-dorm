package th.ac.dusit.dorm.masterdata;

import java.time.LocalDate;

public record MasterDataResponse(
        Long id,
        MasterDataType type,
        String code,
        String nameTh,
        String nameEn,
        Long parentId,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        boolean active,
        String deactivationReason,
        Long version) {

    static MasterDataResponse from(MasterDataEntity entity) {
        return new MasterDataResponse(
                entity.getId(),
                entity.getType(),
                entity.getCode(),
                entity.getNameTh(),
                entity.getNameEn(),
                entity.getParentId(),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo(),
                entity.isActive(),
                entity.getDeactivationReason(),
                entity.getVersion());
    }
}
