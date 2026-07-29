package th.ac.dusit.dorm.masterdata;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateMasterDataRequest(
        @NotBlank @Size(max = 40) String code,
        @NotBlank @Size(max = 200) String nameTh,
        @Size(max = 200) String nameEn,
        Long parentId,
        @NotNull LocalDate effectiveFrom,
        LocalDate effectiveTo,
        @NotNull Long version) {
}
