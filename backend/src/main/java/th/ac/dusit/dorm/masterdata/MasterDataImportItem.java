package th.ac.dusit.dorm.masterdata;

import java.time.LocalDate;

public record MasterDataImportItem(
        MasterDataType type,
        String code,
        String nameTh,
        String nameEn,
        Long parentId,
        LocalDate effectiveFrom,
        LocalDate effectiveTo) {
}
