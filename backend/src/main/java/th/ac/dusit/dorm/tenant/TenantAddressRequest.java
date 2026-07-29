package th.ac.dusit.dorm.tenant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TenantAddressRequest(
        @NotBlank @Pattern(regexp = "CURRENT|REGISTERED") String addressType,
        @NotBlank @Size(max = 500) String addressLine,
        @Size(max = 40) String subdistrictCode,
        @Size(max = 40) String districtCode,
        @Size(max = 40) String provinceCode,
        @Size(max = 10) String postalCode,
        @NotBlank @Size(max = 40) String countryCode) {
}
