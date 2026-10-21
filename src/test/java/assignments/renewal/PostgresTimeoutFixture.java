package assignments.renewal;

import assignments.insurance.PolicyRenewalFacts;
import assignments.insurance.PolicyRenewalReadStore;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/** Instructor-owned disposable PostgreSQL fixture. The app pool has one slot so reuse is unambiguous. */
final class PostgresTimeoutFixture implements AutoCloseable {
    private final EmbeddedPostgres postgres;
    private final HikariDataSource appPool;
    private final DataSource admin;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    private PostgresTimeoutFixture(EmbeddedPostgres postgres, HikariDataSource appPool) {
        this.postgres = postgres;
        this.appPool = appPool;
        this.admin = postgres.getPostgresDatabase();
        this.jdbcTemplate = new JdbcTemplate(appPool);
        this.transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(appPool));
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        this.transactionTemplate.setReadOnly(true);
    }

    static PostgresTimeoutFixture open() throws Exception {
        EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl("postgres", "postgres"));
        config.setUsername("postgres");
        config.setPassword("");
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(1000);
        config.setPoolName("timed-read-one-slot");
        HikariDataSource pool = new HikariDataSource(config);
        PostgresTimeoutFixture fixture = new PostgresTimeoutFixture(postgres, pool);
        try (Connection connection = fixture.admin.getConnection(); var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE training_policy_lifecycle (policy_id TEXT PRIMARY KEY, owner_id TEXT NOT NULL)");
            statement.execute("INSERT INTO training_policy_lifecycle VALUES ('policy-renewal-a','owner-a')");
            return fixture;
        } catch (Exception failure) {
            fixture.close();
            throw failure;
        }
    }

    HikariDataSource appPool() { return appPool; }
    DataSource admin() { return admin; }
    JdbcTemplate jdbc() { return jdbcTemplate; }
    TransactionTemplate tx() { return transactionTemplate; }

    TimedPolicyRenewalReadStore timed(PolicyRenewalReadStore delegate, long lockMillis, long statementMillis) {
        return new TimedPolicyRenewalReadStore(transactionTemplate, jdbcTemplate, delegate,
                new PreviewReadBudgetPolicy(lockMillis, statementMillis));
    }

    Baseline baseline() {
        String[] values = jdbcTemplate.queryForObject("SELECT pg_backend_pid(), current_setting('lock_timeout'), " +
                        "current_setting('statement_timeout')", (rs, row) -> new String[]{
                        Integer.toString(rs.getInt(1)), rs.getString(2), rs.getString(3)});
        Activity activity = activity(Integer.parseInt(values[0]));
        return new Baseline(Integer.parseInt(values[0]), activity.backendStart(), values[1], values[2]);
    }

    Activity activity(int pid) {
        return jdbcAdmin().queryForObject("SELECT pid, backend_start::text, state, wait_event_type, wait_event, " +
                        "xact_start IS NOT NULL FROM pg_stat_activity WHERE pid = ?", (rs, row) ->
                        new Activity(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                                rs.getString(5), rs.getBoolean(6)), pid);
    }

    Activity awaitWait(int pid, String type, String event, Duration watchdog) throws Exception {
        long deadline = System.nanoTime() + watchdog.toNanos();
        Activity last = activity(pid);
        while (System.nanoTime() < deadline) {
            last = activity(pid); // Fresh PostgreSQL snapshot on every bounded poll.
            if ("active".equals(last.state()) && type.equals(last.waitEventType())
                    && (event == null || event.equals(last.waitEvent())) && last.transactionOpen()) return last;
            TimeUnit.MILLISECONDS.sleep(25);
        }
        throw new AssertionError("Timed out observing PostgreSQL wait state; last snapshot=" + last);
    }

    Activity awaitIdle(int pid, Duration watchdog) throws Exception {
        long deadline = System.nanoTime() + watchdog.toNanos();
        Activity last = activity(pid);
        while (System.nanoTime() < deadline) {
            last = activity(pid);
            if ("idle".equals(last.state()) && !last.transactionOpen()) return last;
            TimeUnit.MILLISECONDS.sleep(25);
        }
        throw new AssertionError("Timed out waiting for transaction rollback/cleanup; last snapshot=" + last);
    }

    private JdbcTemplate jdbcAdmin() { return new JdbcTemplate(admin); }

    @Override public void close() throws Exception {
        appPool.close();
        postgres.close();
    }

    record Baseline(int pid, String backendStart, String lockTimeout, String statementTimeout) { }
    record Activity(int pid, String backendStart, String state, String waitEventType,
                    String waitEvent, boolean transactionOpen) { }
}
