package th.ac.dusit.dorm.identity;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppUserRepository extends JpaRepository<AppUserEntity, Long> {
    Optional<AppUserEntity> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCase(String username);
    Page<AppUserEntity> findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
            String username, String displayName, Pageable pageable);

    @Query(value = """
            SELECT r.code FROM user_roles ur
            JOIN roles r ON r.id = ur.role_id
            WHERE ur.user_id = :userId AND r.active = TRUE
            """, nativeQuery = true)
    java.util.List<String> findRoleCodes(@Param("userId") Long userId);

    @Query(value = """
            SELECT DISTINCT p.code FROM user_roles ur
            JOIN roles r ON r.id = ur.role_id
            JOIN role_permissions rp ON rp.role_id = r.id
            JOIN permissions p ON p.id = rp.permission_id
            WHERE ur.user_id = :userId AND r.active = TRUE
            """, nativeQuery = true)
    java.util.List<String> findPermissionCodes(@Param("userId") Long userId);
}
