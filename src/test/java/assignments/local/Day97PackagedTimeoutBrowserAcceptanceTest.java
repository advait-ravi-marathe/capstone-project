package assignments.local;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

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

/** Real packaged JAR, authenticated browser, and relation-lock acceptance; no fault route. */
class Day97PackagedTimeoutBrowserAcceptanceTest {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    @Tag("learnerAcceptance")
    void safeTimeoutIsVisibleBeforeReleaseThenOneExplicitRetrySucceeds() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
             LocalPackagedRuntimeProcess app = LocalPackagedRuntimeProcess.start(postgres, "2027-01-01T12:00:00Z");
             Connection holder = postgres.getPostgresDatabase().getConnection()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            DatabaseSnapshot before = snapshot(dataSource);
            Path control = Path.of("target/day97-packaged-timeout", UUID.randomUUID().toString()).toAbsolutePath();
            Files.createDirectories(control);
            Path log = control.resolve("browser.log");
            Path output = control.resolve("result.json");
            holder.setAutoCommit(false);
            try (Statement lock = holder.createStatement()) {
                lock.execute("LOCK TABLE training_policy_lifecycle IN ACCESS EXCLUSIVE MODE");
                // Bound the holder's verification reads as a test-fixture watchdog. This is not
                // an application timeout assertion; any fixture ordering defect fails instead
                // of hanging while holding the relation lock.
                lock.execute("SET LOCAL statement_timeout = '5s'");
            }
            Process browser = null;
            boolean released = false;
            try {
                browser = startBrowser(app.port(), control, output, log);
                awaitFile(control.resolve("first-started"), browser, log, Duration.ofSeconds(20));
                awaitRelationWaiter(dataSource, browser, log, Duration.ofSeconds(15));
                Path observedPath = control.resolve("timeout-observed.json");
                awaitFile(observedPath, browser, log, Duration.ofSeconds(20));

                JsonNode beforeRelease = JSON.readTree(Files.readString(observedPath));
                assertEquals(503, beforeRelease.path("status").asInt(), "the route reports a safe unavailable response");
                assertTrue(beforeRelease.path("visibleStatus").asText().matches("(?is).*(busy|unavailable|timeout).*"));
                assertTrue(beforeRelease.path("policyContextRetained").asBoolean());
                assertTrue(beforeRelease.path("manualRetryVisible").asBoolean());
                assertEquals(1, beforeRelease.path("previewResponses").asInt(),
                        "no automatic retry may run while the holder still owns the lock");
                String safeBody = beforeRelease.path("body").toString().toLowerCase();
                for (String detail : new String[]{"55p03", "57014", "lock_timeout", "statement_timeout",
                        "training_policy_lifecycle", "canceling statement"}) {
                    assertFalse(safeBody.contains(detail), "response must not disclose database detail: " + detail);
                }
                assertTrue(relationLockStillHeld(dataSource, holder),
                        "the safe timeout response must be observed before the test releases ACCESS EXCLUSIVE");
                // Read through the lock-owning session. A separate connection would block on
                // ACCESS EXCLUSIVE until commit, which intentionally happens only below.
                assertEquals(before, snapshot(holder), "read and timeout leave all lifecycle rows unchanged");

                holder.commit();
                released = true;
                Files.writeString(control.resolve("release-lock"), "ok");
                awaitFile(control.resolve("finished"), browser, log, Duration.ofSeconds(20));
                assertTrue(browser.waitFor(5, TimeUnit.SECONDS), "browser harness must terminate");
                assertEquals(0, browser.exitValue(), Files.readString(log));
                JsonNode result = JSON.readTree(Files.readString(output));
                assertEquals(200, result.path("retry").path("status").asInt());
                assertEquals(2, result.path("previewResponses").asInt());
                assertTrue(result.path("retryWasExplicitUserAction").asBoolean());
                assertEquals(before, snapshot(dataSource), "successful retry remains a read-only preview");
            } finally {
                if (!released) {
                    holder.rollback();
                    Files.writeString(control.resolve("release-lock"), "ok");
                }
                if (browser != null && browser.isAlive()) {
                    browser.destroy();
                    if (!browser.waitFor(3, TimeUnit.SECONDS)) browser.destroyForcibly();
                }
            }
        }
    }

    private static Process startBrowser(int port, Path control, Path output, Path log) throws Exception {
        ProcessBuilder builder = new ProcessBuilder("node", "e2e/day97-timeout-retry.mjs",
                "http://127.0.0.1:" + port, control.toString(), output.toString())
                .directory(Path.of("frontend").toAbsolutePath().toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile());
        String chromium = System.getenv("CHROMIUM_BIN");
        if (chromium != null && !chromium.isBlank()) builder.environment().put("CHROMIUM_BIN", chromium);
        return builder.start();
    }

    private static void awaitFile(Path path, Process browser, Path log, Duration watchdog) throws Exception {
        long deadline = System.nanoTime() + watchdog.toNanos();
        while (System.nanoTime() < deadline) {
            if (Files.exists(path)) return;
            if (!browser.isAlive()) throw new IllegalStateException("Browser exited: " + Files.readString(log));
            Thread.sleep(25);
        }
        throw new IllegalStateException("Watchdog waiting for " + path.getFileName() + ": " + Files.readString(log));
    }

    private static void awaitRelationWaiter(DataSource dataSource, Process browser, Path log, Duration watchdog)
            throws Exception {
        long deadline = System.nanoTime() + watchdog.toNanos();
        while (System.nanoTime() < deadline) {
            if (relationWaiterCount(dataSource) > 0) return;
            if (!browser.isAlive()) throw new IllegalStateException("Browser exited before PostgreSQL wait was observed: " + Files.readString(log));
            Thread.sleep(25);
        }
        throw new IllegalStateException("Watchdog waiting to observe relation-lock wait: " + Files.readString(log));
    }

    private static int relationWaiterCount(DataSource dataSource) throws Exception {
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

    private static boolean relationLockStillHeld(DataSource dataSource, Connection holder) throws Exception {
        int holderPid;
        try (Statement statement = holder.createStatement(); ResultSet row = statement.executeQuery("SELECT pg_backend_pid()")) {
            assertTrue(row.next()); holderPid = row.getInt(1);
        }
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             ResultSet row = statement.executeQuery("SELECT count(*) FROM pg_locks WHERE pid=" + holderPid +
                     " AND locktype='relation' AND relation='training_policy_lifecycle'::regclass " +
                     "AND mode='AccessExclusiveLock' AND granted")) {
            assertTrue(row.next()); return row.getInt(1) == 1;
        }
    }

    private static DatabaseSnapshot snapshot(DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            return snapshot(connection);
        }
    }

    private static DatabaseSnapshot snapshot(Connection connection) throws Exception {
        return new DatabaseSnapshot(tableSnapshot(connection, "training_policy_lifecycle", "policy_id"),
                tableSnapshot(connection, "training_policy_lifecycle_event", "policy_id, version"),
                tableSnapshot(connection, "training_policy_idempotency", "owner_id, client_request_id"));
    }

    private static String tableSnapshot(Connection connection, String table, String orderBy) throws Exception {
        String sql = "SELECT COALESCE(jsonb_agg(to_jsonb(row_data) ORDER BY " + orderBy + "), '[]'::jsonb)::text " +
                "FROM " + table + " AS row_data";
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            assertTrue(rows.next()); return rows.getString(1);
        }
    }

    private record DatabaseSnapshot(String policies, String lifecycleEvents, String idempotency) { }
}
