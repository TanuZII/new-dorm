package th.ac.dusit.dorm.identity;

import java.util.Set;
import java.util.TreeSet;

public record RoleResponse(
        String code,
        String nameTh,
        String description,
        boolean active,
        long version,
        Set<String> permissions) {

    static RoleResponse from(RoleEntity role) {
        var codes = new TreeSet<String>();
        role.getPermissions().forEach(permission -> codes.add(permission.getCode()));
        return new RoleResponse(
                role.getCode(), role.getNameTh(), role.getDescription(),
                role.isActive(), role.getVersion(), codes);
    }
}
