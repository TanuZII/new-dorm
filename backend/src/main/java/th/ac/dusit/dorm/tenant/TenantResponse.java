package th.ac.dusit.dorm.tenant;

import th.ac.dusit.dorm.tenant.persistence.TenantEntity;

public record TenantResponse(
        Long id,
        String tenantCode,
        String tenantType,
        String fullName,
        String email,
        String phone,
        boolean active) {

    static TenantResponse from(TenantEntity tenant) {
        return new TenantResponse(
                tenant.getId(), tenant.getTenantCode(), tenant.getTenantType(),
                tenant.getFirstName() + " " + tenant.getLastName(),
                tenant.getEmail(), tenant.getPhone(), tenant.isActive());
    }
}

