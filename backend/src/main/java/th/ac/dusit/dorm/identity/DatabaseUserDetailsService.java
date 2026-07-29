package th.ac.dusit.dorm.identity;

import java.time.Instant;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DatabaseUserDetailsService implements UserDetailsService {
    private final AppUserRepository repository;

    public DatabaseUserDetailsService(AppUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        var user = repository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));
        boolean locked = user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now());
        var roleCodes = repository.findRoleCodes(user.getId());
        if (roleCodes.isEmpty()) {
            roleCodes = java.util.List.of(user.getRole().name());
        }
        var authorities = new java.util.LinkedHashSet<SimpleGrantedAuthority>();
        roleCodes.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        repository.findPermissionCodes(user.getId()).forEach(permission -> authorities.add(
                new SimpleGrantedAuthority("PERM_" + permission.replace(':', '_'))));
        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .disabled(!user.isActive())
                .accountLocked(locked)
                .build();
    }
}
