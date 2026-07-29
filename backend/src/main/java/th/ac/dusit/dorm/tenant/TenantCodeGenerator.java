package th.ac.dusit.dorm.tenant;

import java.sql.Statement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

@Component
public class TenantCodeGenerator {
    private final JdbcTemplate jdbcTemplate;

    public TenantCodeGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String nextCode() {
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> connection.prepareStatement(
                "INSERT INTO tenant_code_sequences (created_at) VALUES (CURRENT_TIMESTAMP)",
                Statement.RETURN_GENERATED_KEYS), keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Unable to generate tenant code");
        }
        return "TEN-%06d".formatted(key.longValue());
    }
}
