package th.ac.dusit.dorm.masterdata;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangeMasterDataStatusRequest(
        boolean active,
        @Size(max = 500) String reason,
        @NotNull Long version) {
}
