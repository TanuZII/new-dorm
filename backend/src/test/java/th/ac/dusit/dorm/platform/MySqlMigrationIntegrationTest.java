package th.ac.dusit.dorm.platform;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

@Testcontainers(disabledWithoutDocker = true)
class MySqlMigrationIntegrationTest {

    @Container
    private static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("dbdorm")
            .withUsername("dorm_app")
            .withPassword("test-password");

    @Test
    void flywayBuildsTheBaselineSchemaOnMySql84() throws Exception {
        var result = Flyway.configure()
                .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .load()
                .migrate();

        assertThat(result.success).isTrue();
        try (var connection = DriverManager.getConnection(
                MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
                var tables = connection.getMetaData().getTables(
                        MYSQL.getDatabaseName(), null, "audit_logs", new String[]{"TABLE"})) {
            assertThat(tables.next()).isTrue();
        }
    }
}
