package th.ac.dusit.dorm.audit;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS audit_logs (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    actor VARCHAR(80) NOT NULL,
                    action VARCHAR(80) NOT NULL,
                    entity_type VARCHAR(80) NOT NULL,
                    entity_id VARCHAR(80),
                    reason VARCHAR(500),
                    ip_address VARCHAR(64),
                    trace_id VARCHAR(64),
                    details JSON,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
        jdbcTemplate.execute("ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS details JSON");
        jdbcTemplate.update("DELETE FROM audit_logs");
        insert("alice", "USER_CREATED", "USER", "10", "2026-07-29T02:00:00Z");
        insert("bob", "USER_DISABLED", "USER", "11", "2026-07-29T03:00:00Z");
    }

    @Test
    void adminCanFilterAuditHistoryAndReceiveAPage() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs")
                        .param("actor", "alice")
                        .param("action", "USER_CREATED")
                        .param("entityType", "USER")
                        .param("from", "2026-07-29T02:00:00Z")
                        .param("to", "2026-07-29T03:00:00Z")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "createdAt,desc")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].actor").value("alice"))
                .andExpect(jsonPath("$.content[0].action").value("USER_CREATED"))
                .andExpect(jsonPath("$.content[0].entityType").value("USER"))
                .andExpect(jsonPath("$.content[0].entityId").value("10"))
                .andExpect(jsonPath("$.content[0].details.source").value("integration-test"));
    }

    @Test
    void nonAdminCannotReadAuditHistory() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs")
                        .with(user("staff").roles("DORM_STAFF")))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsAnEmptyOrReversedTimeRange() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs")
                        .param("from", "2026-07-29T03:00:00Z")
                        .param("to", "2026-07-29T02:00:00Z")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void readsJsonDetailsWrittenByAuditService() throws Exception {
        auditService.record(
                "system",
                "SERVICE_RECORDED",
                "TEST",
                "1",
                null,
                "127.0.0.1",
                Map.of("source", "audit-service"));

        mockMvc.perform(get("/api/v1/audit-logs")
                        .param("action", "SERVICE_RECORDED")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].details.source").value("audit-service"));
    }

    private void insert(String actor, String action, String entityType, String entityId, String createdAt) {
        jdbcTemplate.update("""
                        INSERT INTO audit_logs
                            (actor, action, entity_type, entity_id, details, created_at)
                        VALUES (?, ?, ?, ?, '{"source":"integration-test"}' FORMAT JSON, ?)
                        """,
                actor, action, entityType, entityId, Timestamp.from(Instant.parse(createdAt)));
    }
}
