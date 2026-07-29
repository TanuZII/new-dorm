package th.ac.dusit.dorm.tenant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TenantContactRequest(
        @NotBlank @Pattern(regexp = "GUARDIAN|EMERGENCY") String contactType,
        @NotBlank @Size(max = 200) String fullName,
        @Size(max = 100) String relationshipName,
        @NotBlank @Size(max = 30) String phone,
        @Email @Size(max = 160) String email,
        boolean primaryContact) {
}
