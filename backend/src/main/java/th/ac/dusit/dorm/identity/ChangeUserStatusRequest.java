package th.ac.dusit.dorm.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeUserStatusRequest(
        boolean active,
        @NotBlank @Size(max = 500) String reason) {
}
