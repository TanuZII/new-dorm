package th.ac.dusit.dorm.identity;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record ReplaceRolePermissionsRequest(
        @NotEmpty Set<@NotBlank @Size(max = 80) String> permissions,
        @NotBlank @Size(max = 500) String reason) {
}
