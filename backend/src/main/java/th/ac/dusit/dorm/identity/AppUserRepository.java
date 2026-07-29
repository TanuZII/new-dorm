package th.ac.dusit.dorm.identity;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUserEntity, Long> {
    Optional<AppUserEntity> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCase(String username);
    Page<AppUserEntity> findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
            String username, String displayName, Pageable pageable);
}
