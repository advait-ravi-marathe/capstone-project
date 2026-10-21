package assignments.teaching;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/** Independent Flyway/history reference using a separate synthetic library schema. */
class ShelfMigrationReferenceTest {
    @Test
    void secondMigratePreservesSentinelAndAppliedHistory() throws Exception {
        try (var postgres = EmbeddedPostgres.builder().start()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            Flyway flyway = Flyway.configure().dataSource(dataSource)
                    .locations("classpath:teaching/library-migrations").target("2").load();
            assertEquals(2, flyway.migrate().migrationsExecuted);
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO shelf VALUES (11, 'Reader-created sentinel')");
            }
            String rowsBefore = rows(dataSource);
            String historyBefore = history(dataSource);
            assertEquals("10:Synthetic handbook\n11:Reader-created sentinel\n", rowsBefore);
            assertEquals(0, flyway.migrate().migrationsExecuted);
            assertEquals(rowsBefore, rows(dataSource));
            assertEquals(historyBefore, history(dataSource));
        }
    }

    private static String rows(DataSource dataSource) throws Exception {
        return scalarRows(dataSource, "SELECT id || ':' || title FROM shelf ORDER BY id");
    }

    private static String history(DataSource dataSource) throws Exception {
        return scalarRows(dataSource, "SELECT version || ':' || checksum || ':' || success FROM flyway_schema_history "
                + "WHERE version IS NOT NULL ORDER BY installed_rank");
    }

    private static String scalarRows(DataSource dataSource, String sql) throws Exception {
        StringBuilder output = new StringBuilder();
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             var result = statement.executeQuery(sql)) {
            while (result.next()) output.append(result.getString(1)).append('\n');
        }
        return output.toString();
    }
}
