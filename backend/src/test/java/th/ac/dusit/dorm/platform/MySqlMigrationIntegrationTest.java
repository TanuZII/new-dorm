package th.ac.dusit.dorm.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;

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

    @Test
    void flywayBuildsTenantPropertyAndOccupancySchemaOnMySql84() throws Exception {
        Flyway.configure()
                .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .load()
                .migrate();

        try (var connection = DriverManager.getConnection(
                MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())) {
            assertThat(tableNames(connection, "buildings", "floors", "beds", "room_meters",
                    "reservations", "reservation_beds", "occupancy_beds", "occupancy_events",
                    "bed_allocation_days"))
                    .containsExactlyInAnyOrder("buildings", "floors", "beds", "room_meters",
                            "reservations", "reservation_beds", "occupancy_beds", "occupancy_events",
                            "bed_allocation_days");
            try (var columns = connection.getMetaData().getColumns(
                    MYSQL.getDatabaseName(), null, "app_users", "tenant_id")) {
                assertThat(columns.next()).isTrue();
            }
        }
    }

    @Test
    void databaseRejectsDuplicateAllocationDayForTheSameBed() throws Exception {
        Flyway.configure()
                .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .load()
                .migrate();

        try (var connection = DriverManager.getConnection(
                MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())) {
            long tenantId = insert(connection,
                    "INSERT INTO tenants (tenant_code, tenant_type, first_name, last_name) VALUES (?, ?, ?, ?)",
                    "MIG-T1", "EXTERNAL", "Migration", "Tenant");
            long buildingId = insert(connection,
                    "INSERT INTO buildings (building_code, name_th) VALUES (?, ?)", "MIG-B1", "Migration Building");
            long floorId = insert(connection,
                    "INSERT INTO floors (building_id, floor_number, floor_code) VALUES (?, ?, ?)",
                    buildingId, 1, "F1");
            long roomId = insert(connection,
                    "INSERT INTO rooms (building_code, number, floor, capacity, floor_id) VALUES (?, ?, ?, ?, ?)",
                    "MIG-B1", "101", 1, 1, floorId);
            long bedId = insert(connection,
                    "INSERT INTO beds (room_id, bed_code, bed_number) VALUES (?, ?, ?)", roomId, "MIG-B1-101-B1", 1);
            long firstReservation = insertReservation(connection, tenantId, roomId,
                    LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));
            long secondReservation = insertReservation(connection, tenantId, roomId,
                    LocalDate.of(2026, 8, 15), LocalDate.of(2026, 9, 15));

            long firstReservationBed = insertReservationBed(connection, firstReservation, bedId,
                    LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));
            long secondReservationBed = insertReservationBed(connection, secondReservation, bedId,
                    LocalDate.of(2026, 8, 15), LocalDate.of(2026, 9, 15));

            insertAllocationDay(connection, bedId, LocalDate.of(2026, 8, 15), firstReservationBed);

            assertThatThrownBy(() -> insertAllocationDay(
                    connection, bedId, LocalDate.of(2026, 8, 15), secondReservationBed))
                    .hasMessageContaining("Duplicate entry");
        }
    }

    private java.util.List<String> tableNames(
            java.sql.Connection connection,
            String... expectedNames) throws Exception {
        var expected = java.util.Set.of(expectedNames);
        var actual = new java.util.ArrayList<String>();
        try (var tables = connection.getMetaData().getTables(
                MYSQL.getDatabaseName(), null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String name = tables.getString("TABLE_NAME");
                if (expected.contains(name)) actual.add(name);
            }
        }
        return actual;
    }

    private long insertReservation(
            java.sql.Connection connection,
            long tenantId,
            long roomId,
            LocalDate startDate,
            LocalDate endDate) throws Exception {
        return insert(connection, """
                INSERT INTO reservations
                    (tenant_id, room_id, allocation_scope, start_date, end_date, status, created_by)
                VALUES (?, ?, 'BED', ?, ?, 'CONFIRMED', 'migration-test')
                """, tenantId, roomId, startDate, endDate);
    }

    private long insertReservationBed(
            java.sql.Connection connection,
            long reservationId,
            long bedId,
            LocalDate startDate,
            LocalDate endDate) throws Exception {
        return insert(connection, """
                INSERT INTO reservation_beds (reservation_id, bed_id, start_date, end_date)
                VALUES (?, ?, ?, ?)
                """, reservationId, bedId, startDate, endDate);
    }

    private void insertAllocationDay(
            java.sql.Connection connection,
            long bedId,
            LocalDate allocationDate,
            long reservationBedId) throws Exception {
        try (var statement = connection.prepareStatement("""
                INSERT INTO bed_allocation_days (bed_id, allocation_date, reservation_bed_id)
                VALUES (?, ?, ?)
                """)) {
            statement.setLong(1, bedId);
            statement.setObject(2, allocationDate);
            statement.setLong(3, reservationBedId);
            statement.executeUpdate();
        }
    }

    private long insert(java.sql.Connection connection, String sql, Object... values) throws Exception {
        try (var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int index = 0; index < values.length; index++) statement.setObject(index + 1, values[index]);
            statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) {
                assertThat(keys.next()).isTrue();
                return keys.getLong(1);
            }
        }
    }
}
