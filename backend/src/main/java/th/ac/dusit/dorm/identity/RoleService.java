package th.ac.dusit.dorm.identity;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import th.ac.dusit.dorm.audit.AuditService;
import th.ac.dusit.dorm.common.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditService auditService;

    public RoleService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            AuditService auditService) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.auditService = auditService;
    }

    public List<RoleResponse> findAll() {
        return roleRepository.findAllByOrderByCodeAsc().stream()
                .map(RoleResponse::from)
                .toList();
    }

    @Transactional
    public RoleResponse replacePermissions(
            String roleCode,
            ReplaceRolePermissionsRequest request,
            String actor,
            String ipAddress) {
        var role = roleRepository.findByCodeIgnoreCase(roleCode)
                .orElseThrow(() -> new ResourceNotFoundException("Role " + roleCode + " not found"));
        var permissions = permissionRepository.findAllByCodeIn(request.permissions());
        if (permissions.size() != request.permissions().size()) {
            throw new IllegalArgumentException("One or more permission codes are invalid");
        }
        role.replacePermissions(new LinkedHashSet<>(permissions));
        auditService.record(
                actor, "ROLE_PERMISSIONS_CHANGED", "ROLE", role.getCode(),
                request.reason().trim(), ipAddress, Map.of());
        return RoleResponse.from(role);
    }
}
