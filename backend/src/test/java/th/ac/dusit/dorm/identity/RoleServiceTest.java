package th.ac.dusit.dorm.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import th.ac.dusit.dorm.audit.AuditService;

class RoleServiceTest {

    @Test
    void atomicallyReplacesRolePermissionsWithValidatedCodes() {
        var roleRepository = org.mockito.Mockito.mock(RoleRepository.class);
        var permissionRepository = org.mockito.Mockito.mock(PermissionRepository.class);
        var auditService = org.mockito.Mockito.mock(AuditService.class);
        var role = new RoleEntity("FINANCE", "เจ้าหน้าที่การเงิน");
        var read = new PermissionEntity("FINANCE:READ", "FINANCE", "READ");
        var write = new PermissionEntity("FINANCE:WRITE", "FINANCE", "WRITE");
        when(roleRepository.findByCodeIgnoreCase("FINANCE")).thenReturn(Optional.of(role));
        when(permissionRepository.findAllByCodeIn(Set.of("FINANCE:READ", "FINANCE:WRITE")))
                .thenReturn(List.of(read, write));
        var service = new RoleService(roleRepository, permissionRepository, auditService);

        var response = service.replacePermissions(
                "FINANCE",
                new ReplaceRolePermissionsRequest(
                        Set.of("FINANCE:READ", "FINANCE:WRITE"), "ปรับสิทธิ์ตามหน้าที่"),
                "admin",
                "127.0.0.1");

        assertThat(response.permissions())
                .containsExactlyInAnyOrder("FINANCE:READ", "FINANCE:WRITE");
        assertThat(role.getPermissions())
                .extracting(PermissionEntity::getCode)
                .containsExactlyInAnyOrder("FINANCE:READ", "FINANCE:WRITE");
    }
}
