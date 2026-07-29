package th.ac.dusit.dorm.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

@Testcontainers(disabledWithoutDocker = true)
class RbacMigrationIntegrationTest {

    @Container
    private static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("dbdorm")
            .withUsername("dorm_app")
            .withPassword("test-password");

    @Test
    void migrationSeedsSixRolesAndBackfillsExistingUserRole() throws Exception {
        Flyway.configure()
                .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .target("1")
                .load()
                .migrate();
        try (var connection = DriverManager.getConnection(
                MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
                var statement = connection.prepareStatement("""
                        INSERT INTO app_users
                            (username, password_hash, display_name, role)
                        VALUES ('legacy.admin', 'hash', 'Legacy Admin', 'ADMIN')
                        """)) {
            statement.executeUpdate();
        }
        var result = Flyway.configure()
                .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .load()
                .migrate();

        assertThat(result.success).isTrue();
        try (var connection = DriverManager.getConnection(
                MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
                var statement = connection.createStatement()) {
            try (var roles = statement.executeQuery("SELECT COUNT(*) FROM roles")) {
                assertThat(roles.next()).isTrue();
                assertThat(roles.getInt(1)).isEqualTo(6);
            }
            try (var permissions = statement.executeQuery("SELECT COUNT(*) FROM permissions")) {
                assertThat(permissions.next()).isTrue();
                assertThat(permissions.getInt(1)).isGreaterThanOrEqualTo(12);
            }
            try (var assignment = statement.executeQuery("""
                    SELECT COUNT(*)
                    FROM user_roles ur
                    JOIN app_users u ON u.id = ur.user_id
                    JOIN roles r ON r.id = ur.role_id
                    WHERE u.username = 'legacy.admin' AND r.code = 'ADMIN'
                    """)) {
                assertThat(assignment.next()).isTrue();
                assertThat(assignment.getInt(1)).isEqualTo(1);
            }
            try (var masterData = statement.executeQuery("""
                    SELECT COUNT(*)
                    FROM master_data_items
                    WHERE (data_type = 'TENANT_TYPE'
                           AND item_code IN ('STUDENT', 'STAFF', 'EXTERNAL'))
                       OR (data_type = 'FEE_TYPE'
                           AND item_code IN ('RENT', 'WATER', 'ELECTRICITY',
                                             'DEPOSIT', 'PENALTY', 'OTHER'))
                    """)) {
                assertThat(masterData.next()).isTrue();
                assertThat(masterData.getInt(1)).isEqualTo(9);
            }
        }
    }
}
