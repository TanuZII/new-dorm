package th.ac.dusit.dorm.identity;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import th.ac.dusit.dorm.audit.AuditService;
import th.ac.dusit.dorm.common.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(
            AppUserRepository repository,
            PasswordEncoder passwordEncoder,
            AuditService auditService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request, String actor, String ipAddress) {
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        if (repository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalStateException("Username " + username + " already exists");
        }
        var user = new AppUserEntity(
                username,
                passwordEncoder.encode(request.password()),
                request.displayName().trim(),
                normalize(request.email()),
                request.role());
        var saved = repository.save(user);
        auditService.record(
                actor, "USER_CREATED", "USER", username, null, ipAddress, "{}");
        return UserResponse.from(saved);
    }

    public Page<UserResponse> findAll(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return repository.findAll(pageable).map(UserResponse::from);
        }
        String normalized = query.trim();
        return repository
                .findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
                        normalized, normalized, pageable)
                .map(UserResponse::from);
    }

    @Transactional
    public UserResponse changeStatus(
            long id,
            ChangeUserStatusRequest request,
            String actor,
            String ipAddress) {
        var user = findRequired(id);
        user.changeStatus(request.active());
        auditService.record(
                actor,
                request.active() ? "USER_ACTIVATED" : "USER_DEACTIVATED",
                "USER",
                user.getUsername(),
                request.reason().trim(),
                ipAddress,
                "{}");
        return UserResponse.from(user);
    }

    @Transactional
    public void resetPassword(
            long id,
            ResetUserPasswordRequest request,
            String actor,
            String ipAddress) {
        var user = findRequired(id);
        user.resetPassword(passwordEncoder.encode(request.password()));
        auditService.record(
                actor,
                "USER_PASSWORD_RESET",
                "USER",
                user.getUsername(),
                request.reason().trim(),
                ipAddress,
                "{}");
    }

    private AppUserEntity findRequired(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found"));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
