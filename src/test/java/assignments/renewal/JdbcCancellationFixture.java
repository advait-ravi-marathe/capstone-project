package assignments.renewal;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/** Fixed instructor fixture for learning the distinction between Future waiting and JDBC cancellation. */
final class JdbcCancellationFixture implements AutoCloseable {
    private final EmbeddedPostgres postgres;
    private final HikariDataSource pool;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final AtomicReference<Statement> statement = new AtomicReference<>();
    private final AtomicReference<Integer> pid = new AtomicReference<>();
    private final AtomicBoolean workerRolledBack = new AtomicBoolean();
    private final AtomicBoolean workerClosed = new AtomicBoolean();
    private final String baselineLock;
    private final String baselineStatement;
    private final Future<Integer> query;

    private JdbcCancellationFixture(EmbeddedPostgres postgres, HikariDataSource pool) throws Exception {
        this.postgres = postgres;
        this.pool = pool;
        try (Connection connection = pool.getConnection(); Statement s = connection.createStatement();
             ResultSet rows = s.executeQuery("SELECT current_setting('lock_timeout'), current_setting('statement_timeout')")) {
            rows.next(); baselineLock = rows.getString(1); baselineStatement = rows.getString(2);
        }
        query = worker.submit(() -> {
            Connection connection = null;
            PreparedStatement active = null;
            ResultSet rows = null;
            boolean completed = false;
            try {
                connection = pool.getConnection();
                connection.setAutoCommit(false);
                active = connection.prepareStatement("SELECT pg_backend_pid(), pg_sleep(30)");
                statement.set(active);
                try (ResultSet pidRows = connection.createStatement().executeQuery("SELECT pg_backend_pid()")) {
                    pidRows.next(); pid.set(pidRows.getInt(1));
                }
                rows = active.executeQuery();
                completed = true;
                return pid.get();
            } finally {
                if (rows != null) try { rows.close(); } catch (Exception ignored) { }
                if (active != null) try { active.close(); } catch (Exception ignored) { }
                if (connection != null) {
                    try { connection.rollback(); workerRolledBack.set(true); }
                    finally { connection.close(); workerClosed.set(true); }
                }
                if (!completed && connection == null) workerClosed.set(true);
            }
        });
    }

    static JdbcCancellationFixture start() throws Exception {
        EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl("postgres", "postgres"));
        config.setUsername("postgres"); config.setPassword("");
        config.setMaximumPoolSize(1); config.setMinimumIdle(1); config.setConnectionTimeout(1000);
        HikariDataSource pool = new HikariDataSource(config);
        try { return new JdbcCancellationFixture(postgres, pool); }
        catch (Exception failure) { pool.close(); postgres.close(); throw failure; }
    }

    Future<Integer> future() { return query; }
    Statement activeStatement() { return statement.get(); }
    int backendPid() { return pid.get(); }
    boolean workerRolledBack() { return workerRolledBack.get(); }
    boolean workerClosed() { return workerClosed.get(); }

    boolean serverStillExecuting(Duration watchdog) throws Exception {
        long deadline = System.nanoTime() + watchdog.toNanos();
        while (System.nanoTime() < deadline) {
            Integer observedPid = pid.get();
            if (observedPid == null) {
                TimeUnit.MILLISECONDS.sleep(25);
                continue;
            }
            try (Connection connection = postgres.getPostgresDatabase().getConnection();
                 var query = connection.prepareStatement("SELECT state, wait_event_type, wait_event " +
                         "FROM pg_stat_activity WHERE pid=?")) {
                query.setInt(1, observedPid);
                try (ResultSet rows = query.executeQuery()) {
                    if (rows.next() && "active".equals(rows.getString(1))
                            && "Timeout".equals(rows.getString(2)) && "PgSleep".equals(rows.getString(3))) return true;
                }
            }
            TimeUnit.MILLISECONDS.sleep(25);
        }
        return false;
    }

    Reborrow reborrowAfterWorker() throws Exception {
        if (!query.isDone() || !workerClosed.get()) throw new IllegalStateException("worker must complete cleanup before reborrow");
        try (Connection connection = pool.getConnection(); Statement s = connection.createStatement();
             ResultSet rows = s.executeQuery("SELECT pg_backend_pid(), current_setting('lock_timeout'), " +
                     "current_setting('statement_timeout')")) {
            rows.next(); return new Reborrow(rows.getInt(1), rows.getString(2), rows.getString(3));
        }
    }

    String baselineLock() { return baselineLock; }
    String baselineStatement() { return baselineStatement; }

    @Override public void close() throws Exception {
        if (!query.isDone()) {
            // A learner TODO may exit before it reaches its cancellation barrier. Keep cleanup
            // bounded while repeatedly cancelling only this fixture's exact test Statement; a
            // cancel sent during the tiny window before executeQuery reaches PostgreSQL is harmless.
            long cleanupDeadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
            while (!query.isDone() && System.nanoTime() < cleanupDeadline) {
                Statement active = statement.get();
                if (active != null) try { active.cancel(); } catch (Exception ignored) { }
                TimeUnit.MILLISECONDS.sleep(100);
            }
            try {
                query.get(5, TimeUnit.SECONDS);
            } catch (ExecutionException expectedCancellationFailure) {
                // PostgreSQL reports the test-only cancel through the worker's future.
            } catch (TimeoutException notCleanedUp) {
                throw new IllegalStateException("test worker did not finish after Statement.cancel; keep the database alive", notCleanedUp);
            }
        }
        worker.shutdown();
        if (!worker.awaitTermination(5, TimeUnit.SECONDS))
            throw new IllegalStateException("test worker did not terminate after it owned statement/connection cleanup");
        pool.close();
        postgres.close();
    }

    record Reborrow(int pid, String lockTimeout, String statementTimeout) { }
}
