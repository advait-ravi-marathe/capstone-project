package assignments.reboot;

import assignments.insurance.QuoteSelectionKey;
import assignments.insurance.SelectionService;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Real PostgreSQL transaction tests with fixed quote fixtures; no Day 1 policy calls. */
@Tag("learnerAcceptance")
class Day10ContractTest {
    private static EmbeddedPostgres postgres;
    private static DataSource dataSource;
    private SelectionService service;

    @BeforeAll
    static void startPostgresAndInstallTestSchema() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        dataSource = postgres.getPostgresDatabase();
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE journeys (journey_id TEXT PRIMARY KEY)");
            statement.execute("""
                    CREATE TABLE quote_versions (
                      journey_id TEXT NOT NULL REFERENCES journeys(journey_id),
                      quote_version_id TEXT NOT NULL,
                      PRIMARY KEY (journey_id, quote_version_id)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE selections (
                      journey_id TEXT NOT NULL,
                      quote_version_id TEXT NOT NULL,
                      selection_order INTEGER NOT NULL,
                      PRIMARY KEY (journey_id, quote_version_id),
                      UNIQUE (journey_id, selection_order),
                      FOREIGN KEY (journey_id, quote_version_id)
                        REFERENCES quote_versions(journey_id, quote_version_id)
                    )
                    """);
            statement.execute("INSERT INTO journeys VALUES ('journey-A')");
            statement.execute("INSERT INTO quote_versions VALUES ('journey-A', 'v0'), ('journey-A', 'v1'), ('journey-A', 'v2')");
        }
    }

    @AfterAll
    static void stopPostgres() throws IOException {
        if (postgres != null) postgres.close();
    }

    @BeforeEach
    void resetAndCreateService() throws Exception {
        service = new SelectionService(dataSource);
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("TRUNCATE selections");
        }
    }

    @Test
    void persistsRowsInInputOrderWithContiguousZeroBasedPositions() throws Exception {
        service.persistBatch(List.of(key("v1"), key("v2")));
        assertEquals(List.of("v1:0", "v2:1"), readSelections());
    }

    @Test
    void emptyBatchCreatesNoRows() throws Exception {
        service.persistBatch(List.of());
        assertEquals(List.of(), readSelections());
    }

    @Test
    void secondRowFailureRollsBackBatchAndPreservesPreExistingRows() throws Exception {
        // Leave order 0 free so the valid first batch row is inserted; the second
        // row then fails its composite quote-version FK and must roll back that row.
        execute("INSERT INTO selections VALUES ('journey-A', 'v0', 9)");
        assertThrows(SQLException.class,
                () -> service.persistBatch(List.of(key("v1"), key("missing-quote"))));
        assertEquals(List.of("v0:9"), readSelections());
    }

    private static QuoteSelectionKey key(String quoteVersionId) {
        return new QuoteSelectionKey("journey-A", quoteVersionId);
    }

    private static List<String> readSelections() throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT quote_version_id, selection_order FROM selections ORDER BY selection_order")) {
            java.util.ArrayList<String> rows = new java.util.ArrayList<>();
            while (result.next()) rows.add(result.getString(1) + ":" + result.getInt(2));
            return List.copyOf(rows);
        }
    }

    private static void execute(String sql) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
