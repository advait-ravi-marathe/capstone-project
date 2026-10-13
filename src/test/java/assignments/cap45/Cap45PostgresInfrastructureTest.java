package assignments.cap45;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Green infrastructure checks: actual PostgreSQL, exact fixture counts, and JDBC binding. */
class Cap45PostgresInfrastructureTest {
    @Test
    void loadsFiveEventsThreeAttemptsAndExecutesParameterizedQuery() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
             Connection connection = postgres.getPostgresDatabase().getConnection()) {
            executeScript(connection, "/assignments/cap45/schema.sql");
            executeScript(connection, "/assignments/cap45/fixtures.sql");
            assertEquals(5, count(connection, "events"));
            assertEquals(3, count(connection, "delivery_attempts"));

            String sql = resource("/assignments/cap45/learner-query.sql");
            try (PreparedStatement query = connection.prepareStatement(sql)) {
                query.setString(1, "A");
                query.setObject(2, OffsetDateTime.parse("2026-10-13T00:00:00Z"));
                query.setObject(3, OffsetDateTime.parse("2026-10-14T00:00:00Z"));
                try (ResultSet result = query.executeQuery()) {
                    assertEquals(6, result.getMetaData().getColumnCount());
                    assertNotNull(result.getMetaData().getColumnLabel(1));
                    assertEquals("owner_id", result.getMetaData().getColumnLabel(1));
                    assertEquals("event_id", result.getMetaData().getColumnLabel(2));
                    assertEquals("amount", result.getMetaData().getColumnLabel(3));
                    assertEquals("attempt_count", result.getMetaData().getColumnLabel(4));
                    assertEquals("last_attempt_at", result.getMetaData().getColumnLabel(5));
                    assertEquals("has_attempt", result.getMetaData().getColumnLabel(6));
                }
            }
        }
    }

    private static long count(Connection connection, String table) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rows.next();
            return rows.getLong(1);
        }
    }

    private static void executeScript(Connection connection, String path) throws Exception {
        try (Statement statement = connection.createStatement()) {
            for (String sql : resource(path).split(";")) {
                if (!sql.isBlank()) statement.execute(sql);
            }
        }
    }

    private static String resource(String path) throws Exception {
        try (var input = Cap45PostgresInfrastructureTest.class.getResourceAsStream(path)) {
            if (input == null) throw new IllegalStateException("Missing instructor resource " + path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
