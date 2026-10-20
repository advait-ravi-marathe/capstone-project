package assignments.local;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Real packaged JAR + Chromium + PostgreSQL lock acceptance; no test-only app route. */
class Day83PackagedLockBrowserAcceptanceTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String CLOCK = "2027-01-10T12:00:00Z";

    @Test
    @Tag("learnerAcceptance")
    void twoBlockedPreviewReadsRejectThirdBeforeJdbcAndManualBrowserReadRecovers() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
             LocalPackagedRuntimeProcess app = LocalPackagedRuntimeProcess.start(postgres, CLOCK)) {
            DataSource dataSource = postgres.getPostgresDatabase();
            DatabaseSnapshot before = snapshot(dataSource);
            Path control = Path.of("target/day83-lock-browser", UUID.randomUUID().toString()).toAbsolutePath();
            Files.createDirectories(control);
            Path browserLog = control.resolve("browser.log");
            Path output = control.resolve("result.json");
            Process browser = null;
            try (Connection lock = dataSource.getConnection(); Statement statement = lock.createStatement()) {
                lock.setAutoCommit(false);
                statement.execute("LOCK TABLE training_policy_lifecycle IN ACCESS EXCLUSIVE MODE");
                browser = startBrowser(app.port(), control, output, browserLog);
                boolean released = false;
                try {
                    awaitFile(control.resolve("first-started"), browser, browserLog, Duration.ofSeconds(20));
                    awaitLockWaiters(dataSource, 1, browser, browserLog);
                    signal(control, "start-second");
                    awaitFile(control.resolve("second-started"), browser, browserLog, Duration.ofSeconds(10));
                    awaitLockWaiters(dataSource, 2, browser, browserLog);

                    signal(control, "start-third");
                    Path third = control.resolve("third.json");
                    awaitFile(third, browser, browserLog, Duration.ofSeconds(5));
                    JsonNode busy = JSON.readTree(Files.readString(third));
                    assertEquals(503, busy.path("status").asInt(), "the third browser request must fail immediately");
                    assertEquals("PREVIEW_BUSY", busy.path("body").path("code").asText());
                    assertTrue(busy.path("elapsedMillis").asDouble() < 900,
                            "busy rejection must beat the configured one-second Hikari connection timeout");
                    assertEquals(2, busy.path("activeConnections").asInt(),
                            "the third request must not check out a third Hikari connection");
                    assertEquals(2, lockWaiterCount(dataSource),
                            "third request must be rejected before it starts a database read");

                    lock.commit(); // Release the real ACCESS EXCLUSIVE lock only after the 503 is observed.
                    released = true;
                    signal(control, "release-lock");
                    awaitFile(control.resolve("finished"), browser, browserLog, Duration.ofSeconds(15));
                    assertTrue(browser.waitFor(5, TimeUnit.SECONDS), "browser harness process must exit");
                    assertEquals(0, browser.exitValue(), Files.readString(browserLog));
                    JsonNode result = JSON.readTree(Files.readString(output));
                    assertEquals(200, result.path("firstStatus").asInt());
                    assertEquals(200, result.path("secondStatus").asInt());
                    assertEquals(200, result.path("manualRetryStatus").asInt());
                    assertTrue(result.path("manualRetryWasUserButtonClick").asBoolean());
                    assertTrue(result.path("readOnlyPreviewVisible").asBoolean());
                    assertEquals(1.0, result.path("boundedRouteMetrics").path("busy").asDouble());
                    assertEquals(3.0, result.path("boundedRouteMetrics").path("success").asDouble());
                    awaitLockWaiters(dataSource, 0, browser, browserLog, Duration.ofSeconds(5));
                    assertEquals(before, snapshot(dataSource),
                            "preview, busy rejection, and manual retry must leave policy, event, and idempotency rows unchanged");
                } finally {
                    if (!released) {
                        lock.rollback();
                        signal(control, "release-lock");
                    }
                    if (browser != null && browser.isAlive()) {
                        browser.destroy();
                        if (!browser.waitFor(3, TimeUnit.SECONDS)) browser.destroyForcibly();
                    }
                }
            }
        }
    }

    private static void signal(Path directory, String name) throws Exception {
        Files.writeString(directory.resolve(name), "ok");
    }

    private static Process startBrowser(int port, Path control, Path output, Path log) throws Exception {
        ProcessBuilder builder = new ProcessBuilder("node", "e2e/day83-lock-recovery.mjs",
                "http://127.0.0.1:" + port, control.toString(), output.toString())
                .directory(Path.of("frontend").toAbsolutePath().toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile());
        String chromium = System.getenv("CHROMIUM_BIN");
        if (chromium != null && !chromium.isBlank()) builder.environment().put("CHROMIUM_BIN", chromium);
        return builder.start();
    }

    private static void awaitFile(Path file, Process browser, Path log, Duration timeout) throws Exception {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (Files.exists(file)) return;
            if (!browser.isAlive()) throw new IllegalStateException("Browser process exited: " + Files.readString(log));
            Thread.sleep(20);
        }
        throw new IllegalStateException("Timed out waiting for " + file.getFileName() + ": " + Files.readString(log));
    }

    private static void awaitLockWaiters(DataSource dataSource, int expected, Process browser, Path log) throws Exception {
        awaitLockWaiters(dataSource, expected, browser, log, Duration.ofSeconds(10));
    }

    private static void awaitLockWaiters(DataSource dataSource, int expected, Process browser, Path log,
                                         Duration timeout) throws Exception {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (lockWaiterCount(dataSource) == expected) return;
            if (browser != null && !browser.isAlive()) {
                throw new IllegalStateException("Browser exited before PostgreSQL lock state was observed: " + Files.readString(log));
            }
            Thread.sleep(20); // bounded polling observes pg_stat_activity; it is not used to create the lock contention.
        }
        throw new IllegalStateException("Expected " + expected + " PostgreSQL lock waiters; saw " + lockWaiterCount(dataSource));
    }

    private static int lockWaiterCount(DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT count(DISTINCT a.pid) FROM pg_stat_activity a " +
                     "JOIN pg_locks l ON l.pid=a.pid WHERE a.datname=current_database() " +
                     "AND a.pid<>pg_backend_pid() AND a.state='active' AND a.wait_event_type='Lock' " +
                     "AND l.locktype='relation' AND l.relation='training_policy_lifecycle'::regclass " +
                     "AND l.mode='AccessShareLock' AND NOT l.granted")) {
            assertTrue(rows.next());
            return rows.getInt(1);
        }
    }

    private static DatabaseSnapshot snapshot(DataSource dataSource) throws Exception {
        return new DatabaseSnapshot(
                tableSnapshot(dataSource, "training_policy_lifecycle", "policy_id"),
                tableSnapshot(dataSource, "training_policy_lifecycle_event", "policy_id, version"),
                tableSnapshot(dataSource, "training_policy_idempotency", "owner_id, client_request_id"));
    }

    private static String tableSnapshot(DataSource dataSource, String table, String orderBy) throws Exception {
        String sql = "SELECT COALESCE(jsonb_agg(to_jsonb(row_data) ORDER BY " + orderBy + "), '[]'::jsonb)::text " +
                "FROM " + table + " AS row_data";
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            assertTrue(rows.next());
            return rows.getString(1);
        }
    }

    private record DatabaseSnapshot(String policies, String lifecycleEvents, String idempotency) { }
}
