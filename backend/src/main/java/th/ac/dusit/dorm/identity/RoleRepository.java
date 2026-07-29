package th.ac.dusit.dorm.identity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByCodeIgnoreCase(String code);
    List<RoleEntity> findAllByOrderByCodeAsc();
}
