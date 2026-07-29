package th.ac.dusit.dorm.identity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BootstrapAdmin implements CommandLineRunner {
    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String password;

    public BootstrapAdmin(
            AppUserRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${DORM_BOOTSTRAP_PASSWORD:}") String password) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (!password.isBlank() && !repository.existsByUsernameIgnoreCase("admin")) {
            repository.save(new AppUserEntity(
                    "admin", passwordEncoder.encode(password), "ผู้ดูแลระบบ", UserRole.ADMIN));
        }
    }
}
