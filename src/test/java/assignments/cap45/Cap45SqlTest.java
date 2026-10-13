package assignments.cap45;

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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** CAP-45 learner acceptance tests; expected to fail until learner-query.sql is completed. */
@Tag("learnerAcceptance")
class Cap45SqlTest {
    private static EmbeddedPostgres postgres;
    private static DataSource dataSource;

    @BeforeAll
    static void startPostgres() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        dataSource = postgres.getPostgresDatabase();
        try (Connection connection = dataSource.getConnection()) {
            executeScript(connection, "/assignments/cap45/schema.sql");
        }
    }

    @AfterAll
    static void stopPostgres() throws IOException {
        if (postgres != null) postgres.close();
    }

    @BeforeEach
    void resetFixtureForIsolation() throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("TRUNCATE TABLE delivery_attempts, events");
            executeScript(connection, "/assignments/cap45/fixtures.sql");
        }
    }

    @Test
    void returnsOneRowPerAcceptedEventAndCountsAttemptsWithoutAmountFanout() throws Exception {
        List<Row> rows = query("A");
        assertEquals(2, rows.size());
        assertRow(rows.get(0), "A", "e1", "100", 2,
                "2026-10-13T11:00:00Z", true);
        assertRow(rows.get(1), "A", "e2", "-20", 0, null, false);
        assertEquals(0, sumAmounts(rows).compareTo(new BigDecimal("80")));
        assertEquals(2, rows.size(), "total must count events, not joined attempt rows");
    }

    @Test
    void doesNotMatchAnotherOwnersAttemptWithTheSameEventId() throws Exception {
        execute("INSERT INTO delivery_attempts VALUES ('B', 'e2', 't9', TIMESTAMPTZ '2026-10-13 13:00:00+00')");
        List<Row> rows = query("A");
        assertEquals(2, rows.size());
        assertRow(rows.get(1), "A", "e2", "-20", 0, null, false);
    }

    @Test
    void addingAttemptDoesNotDuplicateEventOrInflateItsAmount() throws Exception {
        execute("INSERT INTO delivery_attempts VALUES ('A', 'e1', 't4', TIMESTAMPTZ '2026-10-13 12:30:00+00')");
        List<Row> rows = query("A");
        assertEquals(2, rows.size());
        assertRow(rows.get(0), "A", "e1", "100", 3,
                "2026-10-13T12:30:00Z", true);
        assertEquals(0, sumAmounts(rows).compareTo(new BigDecimal("80")));
    }

    @Test
    void emptyOwnerProducesNoRows() throws Exception {
        assertTrue(query("C").isEmpty());
    }

    @Test
    void eventInWindowRetainsAttemptMadeLater() throws Exception {
        execute("INSERT INTO delivery_attempts VALUES ('A', 'e2', 't5', TIMESTAMPTZ '2026-10-15 09:00:00+00')");
        List<Row> rows = query("A");
        assertEquals(2, rows.size());
        assertRow(rows.get(1), "A", "e2", "-20", 1,
                "2026-10-15T09:00:00Z", true);
    }

    private List<Row> query(String owner) throws Exception {
        String sql = resource("/assignments/cap45/learner-query.sql");
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, owner);
            statement.setObject(2, utc("2026-10-13T00:00:00Z"));
            statement.setObject(3, utc("2026-10-14T00:00:00Z"));
            try (ResultSet result = statement.executeQuery()) {
                List<Row> rows = new ArrayList<>();
                while (result.next()) {
                    String rowOwner = result.getString("owner_id");
                    String eventId = result.getString("event_id");
                    BigDecimal amount = result.getBigDecimal("amount");
                    long attempts = result.getLong("attempt_count");
                    boolean attemptsWasNull = result.wasNull();
                    OffsetDateTime lastAttempt = result.getObject("last_attempt_at", OffsetDateTime.class);
                    boolean hasAttempt = result.getBoolean("has_attempt");
                    boolean hasAttemptWasNull = result.wasNull();
                    rows.add(new Row(rowOwner, eventId, amount, attempts, attemptsWasNull,
                            lastAttempt, hasAttempt, hasAttemptWasNull));
                }
                return rows;
            }
        }
    }

    private static void assertRow(Row actual, String owner, String event, String amount,
                                  long attempts, String lastAttempt, boolean hasAttempt) {
        assertEquals(owner, actual.ownerId);
        assertEquals(event, actual.eventId);
        assertNotNull(actual.amount, "amount must not be SQL NULL");
        assertEquals(0, new BigDecimal(amount).compareTo(actual.amount));
        assertFalse(actual.attemptCountWasNull, "attempt_count must not be SQL NULL");
        assertEquals(attempts, actual.attemptCount);
        assertFalse(actual.hasAttemptWasNull, "has_attempt must not be SQL NULL");
        assertEquals(hasAttempt, actual.hasAttempt);
        if (lastAttempt == null) {
            assertNull(actual.lastAttemptAt);
        } else {
            assertEquals(utc(lastAttempt), actual.lastAttemptAt);
        }
    }

    private static BigDecimal sumAmounts(List<Row> rows) {
        return rows.stream().map(Row::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void execute(String sql) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static void executeScript(Connection connection, String path) throws Exception {
        try (Statement statement = connection.createStatement()) {
            for (String sql : resource(path).split(";")) {
                if (!sql.isBlank()) statement.execute(sql);
            }
        }
    }

    private static String resource(String path) throws IOException {
        try (var input = Cap45SqlTest.class.getResourceAsStream(path)) {
            if (input == null) throw new IOException("Missing instructor resource: " + path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static OffsetDateTime utc(String value) {
        return OffsetDateTime.parse(value);
    }

    private record Row(String ownerId, String eventId, BigDecimal amount,
                       long attemptCount, boolean attemptCountWasNull,
                       OffsetDateTime lastAttemptAt, boolean hasAttempt, boolean hasAttemptWasNull) {}
}
