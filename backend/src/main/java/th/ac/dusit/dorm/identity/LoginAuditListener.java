package th.ac.dusit.dorm.identity;

import java.time.Instant;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import th.ac.dusit.dorm.audit.AuditService;

@Component
public class LoginAuditListener {
    private final AppUserRepository repository;
    private final AuditService auditService;

    public LoginAuditListener(AppUserRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @EventListener
    @Transactional
    public void failed(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        repository.findByUsernameIgnoreCase(event.getAuthentication().getName())
                .ifPresent(user -> user.recordFailure(5, 15, Instant.now()));
        auditService.record(
                username,
                "LOGIN_FAILURE",
                "USER",
                username,
                "Bad credentials",
                remoteAddress(event.getAuthentication().getDetails()),
                Map.of());
    }

    @EventListener
    @Transactional
    public void succeeded(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        repository.findByUsernameIgnoreCase(username)
                .ifPresent(AppUserEntity::recordSuccess);
        auditService.record(
                username,
                "LOGIN_SUCCESS",
                "USER",
                username,
                null,
                remoteAddress(event.getAuthentication().getDetails()),
                Map.of());
    }

    private String remoteAddress(Object details) {
        if (details instanceof WebAuthenticationDetails webDetails) {
            return webDetails.getRemoteAddress();
        }
        return null;
    }
}
