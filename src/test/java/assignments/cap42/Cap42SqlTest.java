package assignments.cap42;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** PostgreSQL acceptance tests; expected to fail until learner-query.sql is completed. */
@Tag("learnerAcceptance")
class Cap42SqlTest {
    private static EmbeddedPostgres postgres;
    private static DataSource dataSource;

    @BeforeAll
    static void startPostgresAndLoadFixtures() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        dataSource = postgres.getPostgresDatabase();
        executeScript("/assignments/cap42/schema.sql");
        executeScript("/assignments/cap42/fixtures.sql");
    }

    @AfterAll
    static void stopPostgres() throws IOException {
        if (postgres != null) postgres.close();
    }

    @Test
    void filtersOwnerStatusAndHalfOpenRangeAndSumsSignedAmounts() throws Exception {
        Result result = query("owner-A", utc("2026-10-13T00:00:00Z"), utc("2026-10-14T00:00:00Z"));
        assertEquals(2, result.count);
        assertEquals(0, new BigDecimal("80.0000").compareTo(result.total));
    }

    @Test
    void includesLowerBoundary() throws Exception {
        Result result = query("owner-A", utc("2026-10-13T00:00:00Z"), utc("2026-10-13T00:00:01Z"));
        assertEquals(1, result.count);
        assertEquals(0, new BigDecimal("100.0000").compareTo(result.total));
    }

    @Test
    void excludesUpperBoundary() throws Exception {
        Result result = query("owner-A", utc("2026-10-13T00:00:00Z"), utc("2026-10-14T00:00:00Z"));
        assertEquals(2, result.count);
        assertEquals(0, new BigDecimal("80.0000").compareTo(result.total));
    }

    @Test
    void includesEventAtNextWindowLowerBoundary() throws Exception {
        Result result = query("owner-A", utc("2026-10-14T00:00:00Z"), utc("2026-10-14T00:00:01Z"));
        assertEquals(1, result.count);
        assertEquals(0, new BigDecimal("50.0000").compareTo(result.total));
    }

    @Test
    void scopesResultsToOwner() throws Exception {
        Result result = query("owner-B", utc("2026-10-13T00:00:00Z"), utc("2026-10-14T00:00:00Z"));
        assertEquals(1, result.count);
        assertEquals(0, new BigDecimal("500.0000").compareTo(result.total));
    }

    @Test
    void emptyRangeReturnsOneZeroAggregateRow() throws Exception {
        Result result = query("owner-A", utc("2026-11-01T00:00:00Z"), utc("2026-11-02T00:00:00Z"));
        assertEquals(0, result.count);
        assertEquals(0, new BigDecimal("0.0000").compareTo(result.total));
    }

    private static Result query(String owner, OffsetDateTime from, OffsetDateTime to) throws Exception {
        String sql = resource("/assignments/cap42/learner-query.sql").trim();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, owner);
            statement.setObject(2, from);
            statement.setObject(3, to);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) throw new AssertionError("Query must return one aggregate row, including for empty matches");
                long count = rows.getLong("accepted_count");
                assertFalse(rows.wasNull(), "accepted_count must not be SQL NULL");
                BigDecimal total = rows.getBigDecimal("signed_total");
                assertNotNull(total, "signed_total must not be SQL NULL");
                Result result = new Result(count, total);
                if (rows.next()) throw new AssertionError("Query must return exactly one aggregate row");
                return result;
            }
        }
    }

    private static void executeScript(String path) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            for (String sql : resource(path).split(";")) {
                if (!sql.isBlank()) statement.execute(sql);
            }
        }
    }

    private static String resource(String path) throws IOException {
        try (var input = Cap42SqlTest.class.getResourceAsStream(path)) {
            if (input == null) throw new IOException("Missing instructor-owned resource: " + path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static OffsetDateTime utc(String value) {
        return OffsetDateTime.parse(value).withOffsetSameInstant(ZoneOffset.UTC);
    }

    private record Result(long count, BigDecimal total) {}
}
