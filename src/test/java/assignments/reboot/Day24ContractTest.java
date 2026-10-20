package assignments.reboot;

import assignments.insurance.QuoteHistoryIndexMigration;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Real PostgreSQL plan/output evidence; no wall-clock benchmark or forced plan assertion. */
@Tag("learnerAcceptance")
class Day24ContractTest {
    private static EmbeddedPostgres postgres;
    private static DataSource dataSource;

    @BeforeAll
    static void startPostgresAndSeedDeterministicHistory() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        dataSource = postgres.getPostgresDatabase();
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE quote_history (journey_id TEXT NOT NULL, quote_version_id TEXT NOT NULL, created_at TIMESTAMPTZ NOT NULL, premium NUMERIC(14,2) NOT NULL)");
            statement.execute("CREATE TABLE history_seed(j INTEGER NOT NULL)");
            statement.execute("INSERT INTO history_seed SELECT generate_series(1, 20000)");
            statement.execute("INSERT INTO quote_history SELECT CASE WHEN j <= 20 THEN 'journey-A' ELSE 'journey-' || lpad((j % 100)::text, 3, '0') END, 'v-' || lpad(j::text, 6, '0'), TIMESTAMPTZ '2026-10-20 00:00:00+00' + j * INTERVAL '1 second', 1200.00 FROM history_seed");
            statement.execute("ANALYZE quote_history");
        }
    }

    @AfterAll
    static void stopPostgres() throws IOException {
        if (postgres != null) postgres.close();
    }

    @Test
    void indexPreservesOrderedQueryResultsAndRecordsExplainPlans() throws Exception {
        String query = "SELECT quote_version_id, created_at FROM quote_history WHERE journey_id = 'journey-A' ORDER BY created_at DESC, quote_version_id ASC LIMIT 50";
        List<String> before = readRows(query);
        String beforePlan = explain(query);

        new QuoteHistoryIndexMigration(dataSource).apply();

        String afterPlan = explain(query);
        List<String> after = readRows(query);
        assertEquals(before, after);
        assertTrue(indexExists("idx_quote_history_journey_created"));
        System.out.println("Day 24 EXPLAIN before index:\n" + beforePlan);
        System.out.println("Day 24 EXPLAIN after index:\n" + afterPlan);
    }

    private static List<String> readRows(String query) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(query)) {
            List<String> result = new ArrayList<>();
            while (rows.next()) result.add(rows.getString(1) + "@" + rows.getTimestamp(2).toInstant());
            return List.copyOf(result);
        }
    }

    private static String explain(String query) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("EXPLAIN " + query)) {
            StringBuilder plan = new StringBuilder();
            while (rows.next()) plan.append(rows.getString(1)).append('\n');
            return plan.toString();
        }
    }

    private static boolean indexExists(String indexName) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             ResultSet row = statement.executeQuery("SELECT 1 FROM pg_indexes WHERE schemaname = current_schema() AND indexname = '" + indexName + "'")) {
            return row.next();
        }
    }
}
