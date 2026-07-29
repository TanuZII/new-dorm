package th.ac.dusit.dorm.identity;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public Page<UserResponse> findAll(
            @RequestParam(required = false) String query,
            Pageable pageable) {
        return service.findAll(query, pageable);
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        var created = service.create(request, authentication.getName(), servletRequest.getRemoteAddr());
        return ResponseEntity
                .created(URI.create("/api/v1/users/" + created.id()))
                .body(created);
    }

    @PatchMapping("/{id}/status")
    public UserResponse changeStatus(
            @PathVariable long id,
            @Valid @RequestBody ChangeUserStatusRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        return service.changeStatus(
                id, request, authentication.getName(), servletRequest.getRemoteAddr());
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(
            @PathVariable long id,
            @Valid @RequestBody ResetUserPasswordRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        service.resetPassword(id, request, authentication.getName(), servletRequest.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}
