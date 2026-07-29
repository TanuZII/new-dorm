package th.ac.dusit.dorm.identity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank
        @Size(max = 80)
        @Pattern(regexp = "[A-Za-z0-9._-]+", message = "must contain only letters, numbers, dot, underscore, or dash")
        String username,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 160) String displayName,
        @Email @Size(max = 160) String email,
        @NotNull UserRole role,
        Long tenantId) {

    public CreateUserRequest(
            String username, String password, String displayName, String email, UserRole role) {
        this(username, password, displayName, email, role, null);
    }
}
