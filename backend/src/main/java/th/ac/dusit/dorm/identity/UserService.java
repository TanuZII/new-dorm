package th.ac.dusit.dorm.identity;

import java.util.Locale;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import th.ac.dusit.dorm.audit.AuditService;
import th.ac.dusit.dorm.common.ResourceNotFoundException;
import th.ac.dusit.dorm.common.DomainConflictException;
import th.ac.dusit.dorm.tenant.persistence.TenantRepository;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final TenantRepository tenantRepository;

    @Autowired
    public UserService(
            AppUserRepository repository,
            PasswordEncoder passwordEncoder,
            AuditService auditService,
            TenantRepository tenantRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.tenantRepository = tenantRepository;
    }

    UserService(
            AppUserRepository repository,
            PasswordEncoder passwordEncoder,
            AuditService auditService) {
        this(repository, passwordEncoder, auditService, null);
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
        linkTenant(user, request);
        var saved = repository.save(user);
        auditService.record(
                actor, "USER_CREATED", "USER", username, null, ipAddress, Map.of());
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
                Map.of());
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
                Map.of());
    }

    @Transactional
    public void changeOwnPassword(
            String username,
            ChangePasswordRequest request,
            String ipAddress) {
        var user = repository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User " + username + " not found"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Password confirmation does not match");
        }
        user.resetPassword(passwordEncoder.encode(request.newPassword()));
        auditService.record(
                username,
                "USER_PASSWORD_CHANGED",
                "USER",
                username,
                null,
                ipAddress,
                Map.of());
    }

    private AppUserEntity findRequired(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found"));
    }

    private void linkTenant(AppUserEntity user, CreateUserRequest request) {
        if (request.tenantId() == null) return;
        if (request.role() != UserRole.TENANT) {
            throw new IllegalArgumentException("Only TENANT users may link a tenant profile");
        }
        if (repository.existsByTenant_Id(request.tenantId())) {
            throw new DomainConflictException(
                    "TENANT_ACCOUNT_ALREADY_LINKED", "Tenant profile already has a user account");
        }
        var tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tenant " + request.tenantId() + " not found"));
        if (!tenant.isActive()) {
            throw new IllegalArgumentException("Tenant profile is inactive");
        }
        user.linkTenant(tenant);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
