package th.ac.dusit.dorm.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetUserPasswordRequest(
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 500) String reason) {
}
