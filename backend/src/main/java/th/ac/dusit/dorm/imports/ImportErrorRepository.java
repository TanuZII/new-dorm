package th.ac.dusit.dorm.imports;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface ImportErrorRepository extends JpaRepository<ImportErrorEntity, Long> {
    List<ImportErrorEntity> findByImportSessionIdOrderByRowNumberAscIdAsc(Long sessionId);
}
