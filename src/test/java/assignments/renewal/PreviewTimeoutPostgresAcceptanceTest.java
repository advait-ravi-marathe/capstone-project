package assignments.renewal;

import assignments.insurance.PolicyRenewalReadStore;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.BadSqlGrammarException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class PreviewTimeoutPostgresAcceptanceTest {
    private static final PreviewReadBudgetPolicy BUDGET = new PreviewReadBudgetPolicy(500, 2000);

    @Test
    void realRelationLockTimeoutIsObservedAndCleanupFinishesBeforeRelease() throws Exception {
        try (PostgresTimeoutFixture fixture = PostgresTimeoutFixture.open();
             Connection holder = fixture.admin().getConnection();
             ExecutorService executor = Executors.newSingleThreadExecutor()) {
            PostgresTimeoutFixture.Baseline baseline = fixture.baseline();
            holder.setAutoCommit(false);
            try (var statement = holder.createStatement()) {
                statement.execute("LOCK TABLE training_policy_lifecycle IN ACCESS EXCLUSIVE MODE");
            }
            int holderPid = pid(holder);
            assertTrue(hasGrantedRelationLock(fixture, holderPid));

            PolicyRenewalReadStore query = (policy, owner) -> {
                fixture.jdbc().queryForObject("SELECT policy_id FROM training_policy_lifecycle " +
                        "WHERE policy_id=? AND owner_id=?", String.class, policy, owner);
                return Optional.empty();
            };
            TimedPolicyRenewalReadStore timed = fixture.timed(query, BUDGET.lockTimeoutMillis(), BUDGET.statementTimeoutMillis());
            Future<Optional<?>> attempt = executor.submit(() -> timed.findByPolicyIdAndOwnerId("policy-renewal-a", "owner-a"));
            PostgresTimeoutFixture.Activity waiting = fixture.awaitWait(baseline.pid(), "Lock", null, Duration.ofSeconds(10));
            assertEquals(baseline.backendStart(), waiting.backendStart());
            assertEquals("active", waiting.state());
            assertNotNull(waiting.waitEvent());
            assertTrue(waiting.transactionOpen());
            assertNotEquals(holderPid, waiting.pid());

            RuntimeException failure = futureFailure(attempt);
            assertEquals("55P03", sqlState(failure));
            assertTrue(hasGrantedRelationLock(fixture, holderPid), "test holder must remain locked until after rejection");
            PostgresTimeoutFixture.Activity cleaned = fixture.awaitIdle(baseline.pid(), Duration.ofSeconds(10));
            assertFalse(cleaned.transactionOpen(), "rollback must complete before the call returns to the outer admission layer");
            PostgresTimeoutFixture.Baseline reused = fixture.baseline();
            assertEquals(baseline.pid(), reused.pid(), "single-slot diagnostic pool must deliberately reuse the observed backend");
            assertEquals(baseline.backendStart(), reused.backendStart());
            assertEquals(baseline.lockTimeout(), reused.lockTimeout());
            assertEquals(baseline.statementTimeout(), reused.statementTimeout());
        } finally {
            // The test connection is closed by try-with-resources, which releases ACCESS EXCLUSIVE.
        }
    }

    @Test
    void statementTimeoutUsesSameTransactionBoundaryAndFastControlPasses() throws Exception {
        try (PostgresTimeoutFixture fixture = PostgresTimeoutFixture.open();
             ExecutorService executor = Executors.newSingleThreadExecutor()) {
            PostgresTimeoutFixture.Baseline baseline = fixture.baseline();
            PolicyRenewalReadStore sleep = (policy, owner) -> {
                fixture.jdbc().queryForObject("SELECT pg_sleep(4)", Object.class);
                return Optional.empty();
            };
            Future<Optional<?>> attempt = executor.submit(() -> fixture.timed(sleep, 500, 2000)
                    .findByPolicyIdAndOwnerId("policy-renewal-a", "owner-a"));
            PostgresTimeoutFixture.Activity waiting = fixture.awaitWait(baseline.pid(), "Timeout", "PgSleep", Duration.ofSeconds(10));
            assertEquals(baseline.backendStart(), waiting.backendStart());
            assertEquals("active", waiting.state());
            assertTrue(waiting.transactionOpen());
            RuntimeException failure = futureFailure(attempt);
            assertEquals("57014", sqlState(failure), "query_canceled does not uniquely identify statement_timeout");
            fixture.awaitIdle(baseline.pid(), Duration.ofSeconds(10));

            PolicyRenewalReadStore fast = (policy, owner) -> {
                assertEquals(1, fixture.jdbc().queryForObject("SELECT 1", Integer.class));
                return Optional.empty();
            };
            assertEquals(Optional.empty(), fixture.timed(fast, 500, 2000)
                    .findByPolicyIdAndOwnerId("policy-renewal-a", "owner-a"));
            PostgresTimeoutFixture.Baseline reused = fixture.baseline();
            assertEquals(baseline.pid(), reused.pid());
            assertEquals(baseline.backendStart(), reused.backendStart());
            assertEquals(baseline.lockTimeout(), reused.lockTimeout());
            assertEquals(baseline.statementTimeout(), reused.statementTimeout());
        }
    }

    @Test
    void repeatedRecoveryRestoresSessionBaselineBeforePermitReuse() throws Exception {
        try (PostgresTimeoutFixture fixture = PostgresTimeoutFixture.open()) {
            PostgresTimeoutFixture.Baseline baseline = fixture.baseline();
            PolicyRenewalReadStore success = (policy, owner) -> Optional.empty();
            assertEquals(Optional.empty(), fixture.timed(success, 500, 2000)
                    .findByPolicyIdAndOwnerId("missing", "owner-a"));
            assertSameBackendAndBaseline(fixture, baseline);

            PolicyRenewalReadStore mappingFailure = (policy, owner) -> { throw new IllegalArgumentException("mapping fixture"); };
            assertThrows(IllegalArgumentException.class, () -> fixture.timed(mappingFailure, 500, 2000)
                    .findByPolicyIdAndOwnerId("policy-renewal-a", "owner-a"));
            assertSameBackendAndBaseline(fixture, baseline);

            PolicyRenewalReadStore statementTimeout = (policy, owner) -> {
                fixture.jdbc().queryForObject("SELECT pg_sleep(4)", Object.class);
                return Optional.empty();
            };
            RuntimeException timeout = assertThrows(RuntimeException.class, () -> fixture.timed(statementTimeout, 500, 2000)
                    .findByPolicyIdAndOwnerId("policy-renewal-a", "owner-a"));
            assertEquals("57014", sqlState(timeout));
            assertSameBackendAndBaseline(fixture, baseline);
        }
    }

    @Test
    void oneSlotPoolReusesSameBackendAndBaselineAfterEveryOutcome() throws Exception {
        // Exercises commit, mapper rollback and statement-timeout rollback on a pool with one slot.
        repeatedRecoveryRestoresSessionBaselineBeforePermitReuse();
    }

    private static void assertSameBackendAndBaseline(PostgresTimeoutFixture fixture,
                                                      PostgresTimeoutFixture.Baseline baseline) {
        PostgresTimeoutFixture.Baseline current = fixture.baseline();
        assertEquals(baseline.pid(), current.pid());
        assertEquals(baseline.backendStart(), current.backendStart());
        assertEquals(baseline.lockTimeout(), current.lockTimeout());
        assertEquals(baseline.statementTimeout(), current.statementTimeout());
    }

    private static int pid(Connection connection) throws SQLException {
        try (var statement = connection.createStatement(); var rows = statement.executeQuery("SELECT pg_backend_pid()")) {
            assertTrue(rows.next()); return rows.getInt(1);
        }
    }

    private static boolean hasGrantedRelationLock(PostgresTimeoutFixture fixture, int pid) {
        Integer count = new org.springframework.jdbc.core.JdbcTemplate(fixture.admin()).queryForObject(
                "SELECT count(*) FROM pg_locks WHERE pid=? AND locktype='relation' " +
                        "AND relation='training_policy_lifecycle'::regclass AND mode='AccessExclusiveLock' AND granted",
                Integer.class, pid);
        return count != null && count == 1;
    }

    private static RuntimeException futureFailure(Future<?> future) throws Exception {
        try {
            future.get(10, TimeUnit.SECONDS);
            fail("expected bounded PostgreSQL read failure");
            throw new AssertionError("unreachable");
        } catch (ExecutionException failure) {
            if (failure.getCause() instanceof RuntimeException runtime) return runtime;
            throw new AssertionError("unexpected checked failure", failure.getCause());
        }
    }

    private static String sqlState(Throwable failure) {
        for (Throwable current = failure; current != null; current = current.getCause()) {
            if (current instanceof SQLException sql && sql.getSQLState() != null) return sql.getSQLState();
        }
        return null;
    }
}
