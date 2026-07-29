package th.ac.dusit.dorm.audit;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.temporal.ChronoUnit;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import th.ac.dusit.dorm.platform.DormProperties;

@Component
public class AuditRetentionJob {

    private final JdbcTemplate jdbcTemplate;
    private final DormProperties properties;
    private final Clock clock;

    public AuditRetentionJob(JdbcTemplate jdbcTemplate, DormProperties properties, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
        this.clock = clock;
    }

    @Scheduled(cron = "${dorm.audit-purge-cron:0 15 2 * * *}")
    @Transactional
    public int purgeExpiredAuditLogs() {
        var cutoff = Timestamp.from(clock.instant().minus(
                properties.auditRetentionDays(), ChronoUnit.DAYS));
        return jdbcTemplate.update(
                "DELETE FROM audit_logs WHERE created_at < ?", cutoff);
    }
}
