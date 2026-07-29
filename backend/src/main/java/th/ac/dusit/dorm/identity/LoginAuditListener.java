package th.ac.dusit.dorm.identity;

import java.time.Instant;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LoginAuditListener {
    private final AppUserRepository repository;

    public LoginAuditListener(AppUserRepository repository) {
        this.repository = repository;
    }

    @EventListener
    @Transactional
    public void failed(AuthenticationFailureBadCredentialsEvent event) {
        repository.findByUsernameIgnoreCase(event.getAuthentication().getName())
                .ifPresent(user -> user.recordFailure(5, 15, Instant.now()));
    }

    @EventListener
    @Transactional
    public void succeeded(AuthenticationSuccessEvent event) {
        repository.findByUsernameIgnoreCase(event.getAuthentication().getName())
                .ifPresent(AppUserEntity::recordSuccess);
    }
}

