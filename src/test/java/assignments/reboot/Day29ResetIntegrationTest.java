package assignments.reboot;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Positive reset verification on fresh disposable PostgreSQL, without psql or a remote DB. */
class Day29ResetIntegrationTest {
    @Test
    void safelyRecreatesOnlyTheSyntheticTrainingRows() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            var dataSource = postgres.getPostgresDatabase();
            String schema = Files.readString(Path.of("src/main/resources/db/migration/V001__homesecure_quotes.sql"));
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                for (String sql : schema.split(";")) if (!sql.isBlank()) statement.execute(sql);
            }
            SyntheticHomeSecureReset.reset(dataSource);
            SyntheticHomeSecureReset.reset(dataSource);
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                try (var rows = statement.executeQuery("SELECT journey_id, quote_version_id, annual_premium_rupees FROM quote_versions")) {
                    assertTrue(rows.next());
                    assertEquals("homesecure-training-journey", rows.getString(1));
                    assertEquals("home-secure-training-v1", rows.getString(2));
                    assertEquals("125.00", rows.getBigDecimal(3).toPlainString());
                    assertTrue(!rows.next(), "reset must leave exactly one quote row");
                }
                try (var rows = statement.executeQuery("SELECT count(*) FROM journeys")) {
                    rows.next();
                    assertEquals(1, rows.getInt(1));
                }
            }
        }
    }
}
