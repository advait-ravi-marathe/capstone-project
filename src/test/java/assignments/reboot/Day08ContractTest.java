package assignments.reboot;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Real PostgreSQL checks for the learner-owned Day 8 migration constraints. */
@Tag("learnerAcceptance")
class Day08ContractTest {
    private static EmbeddedPostgres postgres;
    private static DataSource dataSource;

    @BeforeAll
    static void startPostgresAndApplyLearnerMigration() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        dataSource = postgres.getPostgresDatabase();
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            for (String sql : resource("/db/migration/V001__homesecure_quotes.sql").split(";")) {
                if (!sql.isBlank()) statement.execute(sql);
            }
        }
    }

    @AfterAll
    static void stopPostgres() throws IOException {
        if (postgres != null) postgres.close();
    }

    @BeforeEach
    void clearTables() throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("TRUNCATE selections, quote_versions, journeys CASCADE");
        }
    }

    @Test
    void acceptsValidJourneyQuoteAndSelection() throws Exception {
        execute("INSERT INTO journeys VALUES ('journey-A')");
        execute("INSERT INTO quote_versions (journey_id, quote_version_id) VALUES ('journey-A', 'v1')");
        execute("INSERT INTO selections VALUES ('journey-A', 'v1', 0)");
    }

    @Test
    void rejectsNullRequiredFields() throws Exception {
        assertSqlState("23502", "INSERT INTO journeys VALUES (NULL)");
        execute("INSERT INTO journeys VALUES ('journey-null-quote')");
        assertSqlState("23502", "INSERT INTO quote_versions (journey_id, quote_version_id) VALUES ('journey-null-quote', NULL)");
        execute("INSERT INTO journeys VALUES ('journey-null-order')");
        execute("INSERT INTO quote_versions (journey_id, quote_version_id) VALUES ('journey-null-order', 'v1')");
        assertSqlState("23502", "INSERT INTO selections (journey_id, quote_version_id, selection_order) VALUES ('journey-null-order', 'v1', NULL)");
    }

    @Test
    void rejectsDuplicateCompositeQuoteVersion() throws Exception {
        execute("INSERT INTO journeys VALUES ('journey-A')");
        execute("INSERT INTO quote_versions (journey_id, quote_version_id) VALUES ('journey-A', 'v1')");
        assertSqlState("23505",
                "INSERT INTO quote_versions (journey_id, quote_version_id) VALUES ('journey-A', 'v1')");
    }

    @Test
    void rejectsOrphanAndCrossJourneySelectionReferences() throws Exception {
        execute("INSERT INTO journeys VALUES ('journey-A'), ('journey-B')");
        execute("INSERT INTO quote_versions (journey_id, quote_version_id) VALUES ('journey-A', 'v1')");
        assertSqlState("23503", "INSERT INTO selections VALUES ('journey-A', 'missing', 0)");
        assertSqlState("23503", "INSERT INTO selections VALUES ('journey-B', 'v1', 0)");
    }

    @Test
    void permitsDifferentVersionsWithinOneJourneyAndUniqueSelectionPairs() throws Exception {
        execute("INSERT INTO journeys VALUES ('journey-A')");
        execute("INSERT INTO quote_versions (journey_id, quote_version_id) VALUES ('journey-A', 'v1'), ('journey-A', 'v2')");
        execute("INSERT INTO selections VALUES ('journey-A', 'v1', 0), ('journey-A', 'v2', 1)");
        assertSqlState("23505", "INSERT INTO selections VALUES ('journey-A', 'v1', 2)");
    }

    private static void assertSqlState(String expected, String sql) {
        SQLException error = assertThrows(SQLException.class, () -> execute(sql));
        assertEquals(expected, error.getSQLState(), "wrong PostgreSQL constraint class for: " + sql);
    }

    private static void execute(String sql) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static String resource(String path) throws IOException {
        try (InputStream stream = Day08ContractTest.class.getResourceAsStream(path)) {
            if (stream == null) throw new IOException("Missing learner migration: " + path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
