package th.ac.dusit.dorm.identity;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {
    private final RoleService service;

    public RoleController(RoleService service) {
        this.service = service;
    }

    @GetMapping
    public List<RoleResponse> findAll() {
        return service.findAll();
    }

    @PutMapping("/{roleCode}/permissions")
    public RoleResponse replacePermissions(
            @PathVariable String roleCode,
            @Valid @RequestBody ReplaceRolePermissionsRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        return service.replacePermissions(
                roleCode, request, authentication.getName(), servletRequest.getRemoteAddr());
    }
}
