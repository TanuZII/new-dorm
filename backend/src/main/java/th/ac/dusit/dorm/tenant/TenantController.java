package th.ac.dusit.dorm.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {
    private final TenantService service;

    public TenantController(TenantService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DORM_STAFF','FINANCE')")
    public Page<TenantResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) TenantType type,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "tenantCode") Pageable pageable) {
        return service.search(query, type, active, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DORM_STAFF','FINANCE')")
    public TenantResponse findById(@PathVariable long id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DORM_STAFF')")
    public ResponseEntity<TenantResponse> create(
            @Valid @RequestBody CreateTenantRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        var created = service.create(request, authentication.getName(), servletRequest.getRemoteAddr());
        return ResponseEntity.created(URI.create("/api/v1/tenants/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DORM_STAFF')")
    public TenantResponse update(
            @PathVariable long id,
            @Valid @RequestBody UpdateTenantRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        return service.update(id, request, authentication.getName(), servletRequest.getRemoteAddr());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','DORM_STAFF')")
    public TenantResponse changeStatus(
            @PathVariable long id,
            @Valid @RequestBody ChangeTenantStatusRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        return service.changeStatus(
                id, request, authentication.getName(), servletRequest.getRemoteAddr());
    }
}
