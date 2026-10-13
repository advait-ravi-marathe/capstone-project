package assignments.cap42;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Instructor smoke test: proves the native PostgreSQL test server can start. */
class PostgresInfrastructureTest {
    @Test
    void startsPostgres14OrNewerAndAcceptsJdbcConnections() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
             Connection connection = postgres.getPostgresDatabase().getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SHOW server_version_num")) {
            assertTrue(result.next());
            assertTrue(result.getInt(1) >= 140000, "expected PostgreSQL 14 or newer");
        }
    }
}
