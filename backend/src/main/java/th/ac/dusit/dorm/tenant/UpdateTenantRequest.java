package th.ac.dusit.dorm.tenant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateTenantRequest(
        @NotNull TenantType tenantType,
        @Size(max = 40) String institutionalId,
        @Size(max = 20) String citizenId,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Email @Size(max = 160) String email,
        @Size(max = 30) String phone,
        List<@Valid TenantAddressRequest> addresses,
        List<@Valid TenantContactRequest> contacts,
        @NotNull Long version) {

    public UpdateTenantRequest {
        addresses = addresses == null ? List.of() : List.copyOf(addresses);
        contacts = contacts == null ? List.of() : List.copyOf(contacts);
    }
}
