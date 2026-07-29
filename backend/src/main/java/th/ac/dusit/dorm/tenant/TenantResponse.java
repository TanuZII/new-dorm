package th.ac.dusit.dorm.tenant;

import java.util.List;
import th.ac.dusit.dorm.tenant.persistence.TenantAddressEntity;
import th.ac.dusit.dorm.tenant.persistence.TenantContactEntity;
import th.ac.dusit.dorm.tenant.persistence.TenantEntity;

public record TenantResponse(
        Long id,
        String tenantCode,
        TenantType tenantType,
        String institutionalId,
        String citizenId,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        boolean active,
        Long version,
        List<AddressResponse> addresses,
        List<ContactResponse> contacts) {

    static TenantResponse from(TenantEntity tenant) {
        return new TenantResponse(
                tenant.getId(), tenant.getTenantCode(), tenant.getTenantType(),
                tenant.getInstitutionalId(), tenant.getCitizenId(), tenant.getFirstName(),
                tenant.getLastName(), tenant.getFirstName() + " " + tenant.getLastName(),
                tenant.getEmail(), tenant.getPhone(), tenant.isActive(), tenant.getVersion(),
                tenant.getAddresses().stream().map(AddressResponse::from).toList(),
                tenant.getContacts().stream().map(ContactResponse::from).toList());
    }

    public record AddressResponse(
            String addressType, String addressLine, String subdistrictCode,
            String districtCode, String provinceCode, String postalCode, String countryCode) {
        static AddressResponse from(TenantAddressEntity address) {
            return new AddressResponse(
                    address.getAddressType(), address.getAddressLine(), address.getSubdistrictCode(),
                    address.getDistrictCode(), address.getProvinceCode(), address.getPostalCode(),
                    address.getCountryCode());
        }
    }

    public record ContactResponse(
            String contactType, String fullName, String relationshipName,
            String phone, String email, boolean primaryContact) {
        static ContactResponse from(TenantContactEntity contact) {
            return new ContactResponse(
                    contact.getContactType(), contact.getFullName(), contact.getRelationshipName(),
                    contact.getPhone(), contact.getEmail(), contact.isPrimaryContact());
        }
    }
}
