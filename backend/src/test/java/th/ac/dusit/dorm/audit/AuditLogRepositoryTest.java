package th.ac.dusit.dorm.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class AuditLogRepositoryTest {

    @Autowired
    private AuditLogRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM audit_logs");
    }

    @Test
    void timeRangeIncludesFromAndExcludesTo() {
        insert("before", "2026-07-29T01:59:59Z");
        insert("at-from", "2026-07-29T02:00:00Z");
        insert("before-to", "2026-07-29T02:59:59Z");
        insert("at-to", "2026-07-29T03:00:00Z");

        var page = repository.findAll(
                AuditLogSpecifications.matches(
                        null,
                        null,
                        null,
                        Instant.parse("2026-07-29T02:00:00Z"),
                        Instant.parse("2026-07-29T03:00:00Z")),
                PageRequest.of(0, 20, Sort.by("createdAt")));

        assertThat(page.getContent())
                .extracting(AuditLogEntity::getActor)
                .containsExactly("at-from", "before-to");
    }

    private void insert(String actor, String createdAt) {
        jdbcTemplate.update("""
                        INSERT INTO audit_logs
                            (actor, action, entity_type, entity_id, created_at)
                        VALUES (?, 'LOGIN_SUCCESS', 'USER', '1', ?)
                        """,
                actor, Timestamp.from(Instant.parse(createdAt)));
    }
}
