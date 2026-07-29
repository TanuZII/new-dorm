package th.ac.dusit.dorm.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import th.ac.dusit.dorm.platform.DormProperties;

@ExtendWith(MockitoExtension.class)
class AuditRetentionJobTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void removesOnlyAuditRowsOlderThanConfiguredRetention() {
        var now = Instant.parse("2026-07-29T10:00:00Z");
        var cutoff = Timestamp.from(Instant.parse("2025-07-29T10:00:00Z"));
        when(jdbcTemplate.update(
                "DELETE FROM audit_logs WHERE created_at < ?", cutoff)).thenReturn(4);
        var properties = new DormProperties(Path.of("storage"), 365, 200);
        var job = new AuditRetentionJob(
                jdbcTemplate, properties, Clock.fixed(now, ZoneOffset.UTC));

        int deleted = job.purgeExpiredAuditLogs();

        assertThat(deleted).isEqualTo(4);
        verify(jdbcTemplate).update(
                "DELETE FROM audit_logs WHERE created_at < ?", cutoff);
    }
}
