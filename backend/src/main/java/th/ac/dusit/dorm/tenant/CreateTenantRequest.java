package th.ac.dusit.dorm.tenant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTenantRequest(
        @NotBlank @Size(max = 40) String tenantCode,
        @NotBlank @Pattern(regexp = "STUDENT|STAFF|ALUMNI|EXTERNAL|OTHER") String tenantType,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Email @Size(max = 160) String email,
        @Size(max = 30) String phone) {
}

