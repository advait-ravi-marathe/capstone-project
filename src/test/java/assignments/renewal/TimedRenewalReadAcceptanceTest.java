package assignments.renewal;

import assignments.insurance.PolicyRenewalFacts;
import assignments.insurance.PolicyRenewalReadStore;
import assignments.insurance.PolicyStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class TimedRenewalReadAcceptanceTest {
    @Test
    void budgetPolicyRejectsNonPositiveUnrepresentableAndUnorderedBudgets() {
        for (PreviewReadBudgetPolicy invalid : new PreviewReadBudgetPolicy[]{
                new PreviewReadBudgetPolicy(0, 2), new PreviewReadBudgetPolicy(-1, 2),
                new PreviewReadBudgetPolicy(1, 0), new PreviewReadBudgetPolicy(1, -2),
                new PreviewReadBudgetPolicy(2, 2), new PreviewReadBudgetPolicy(3, 2),
                new PreviewReadBudgetPolicy(Integer.MAX_VALUE + 1L, Long.MAX_VALUE),
                new PreviewReadBudgetPolicy(1, Integer.MAX_VALUE + 1L)}) {
            assertThrows(IllegalArgumentException.class, invalid::validate, "invalid budget=" + invalid);
        }
        assertDoesNotThrow(() -> new PreviewReadBudgetPolicy(500, 2000).validate());
    }

    @Test
    void rejectsAmbientTransactionBeforeCallingDelegate() throws Exception {
        try (PostgresTimeoutFixture fixture = PostgresTimeoutFixture.open()) {
            AtomicInteger calls = new AtomicInteger();
            TimedPolicyRenewalReadStore timed = fixture.timed((policy, owner) -> {
                calls.incrementAndGet(); return Optional.empty();
            }, 500, 2000);
            fixture.tx().execute(status -> {
                assertTrue(timed.hasAmbientTransactionForTests());
                assertThrows(IllegalStateException.class,
                        () -> timed.findByPolicyIdAndOwnerId("policy-renewal-a", "owner-a"));
                return null;
            });
            assertEquals(0, calls.get(), "ambient transaction must be rejected before delegate access");
        }
    }

    @Test
    void setsTransactionLocalBudgetsOnSameBackend() throws Exception {
        try (PostgresTimeoutFixture fixture = PostgresTimeoutFixture.open()) {
            PostgresTimeoutFixture.Baseline before = fixture.baseline();
            AtomicReference<String[]> inDelegate = new AtomicReference<>();
            PolicyRenewalReadStore delegate = (policy, owner) -> {
                assertEquals("policy-renewal-a", policy);
                assertEquals("owner-a", owner);
                inDelegate.set(fixture.jdbc().queryForObject("SELECT pg_backend_pid()::text, " +
                        "current_setting('lock_timeout'), current_setting('statement_timeout'), txid_current()::text",
                        (rs, row) -> new String[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)}));
                return Optional.empty();
            };
            Optional<PolicyRenewalFacts> result = fixture.timed(delegate, 500, 2000)
                    .findByPolicyIdAndOwnerId("policy-renewal-a", "owner-a");
            assertTrue(result.isEmpty());
            assertNotNull(inDelegate.get());
            assertEquals(before.pid(), Integer.parseInt(inDelegate.get()[0]));
            assertEquals("500ms", inDelegate.get()[1]);
            assertEquals("2s", inDelegate.get()[2]);
            assertFalse(inDelegate.get()[3].isBlank(), "delegate executes inside the owned transaction");
            PostgresTimeoutFixture.Baseline after = fixture.baseline();
            assertEquals(before.pid(), after.pid(), "one-slot pool deliberately reuses the same backend");
            assertEquals(before.backendStart(), after.backendStart());
            assertEquals(before.lockTimeout(), after.lockTimeout());
            assertEquals(before.statementTimeout(), after.statementTimeout());
        }
    }

    @Test
    void delegatesExactlyOnceAndPreservesPresentAndEmptyFacts() throws Exception {
        try (PostgresTimeoutFixture fixture = PostgresTimeoutFixture.open()) {
            AtomicInteger calls = new AtomicInteger();
            AtomicReference<String> arguments = new AtomicReference<>();
            PolicyRenewalFacts facts = new PolicyRenewalFacts("policy-renewal-a", LocalDate.parse("2026-01-15"),
                    PolicyStatus.ISSUED, 3, "home-secure-v1", "home-secure-rate-v1",
                    new BigDecimal("125.00"), "INR");
            TimedPolicyRenewalReadStore present = fixture.timed((policy, owner) -> {
                arguments.set(policy + "/" + owner); calls.incrementAndGet(); return Optional.of(facts);
            }, 500, 2000);
            assertEquals(Optional.of(facts), present.findByPolicyIdAndOwnerId("policy-renewal-a", "owner-a"));
            assertEquals(1, calls.get());
            assertEquals("policy-renewal-a/owner-a", arguments.get());

            calls.set(0);
            TimedPolicyRenewalReadStore empty = fixture.timed((policy, owner) -> {
                calls.incrementAndGet(); return Optional.empty();
            }, 500, 2000);
            assertEquals(Optional.empty(), empty.findByPolicyIdAndOwnerId("missing", "owner-a"));
            assertEquals(1, calls.get());
        }
    }

    @Test
    void readOnlyAndMappingFailureRollbackBeforeReturn() throws Exception {
        try (PostgresTimeoutFixture fixture = PostgresTimeoutFixture.open()) {
            PostgresTimeoutFixture.Baseline before = fixture.baseline();
            assertThrows(RuntimeException.class, () -> fixture.timed((policy, owner) -> {
                fixture.jdbc().update("INSERT INTO training_policy_lifecycle VALUES ('forbidden','owner-a')");
                return Optional.empty();
            }, 500, 2000).findByPolicyIdAndOwnerId("policy-renewal-a", "owner-a"));
            assertEquals(0, fixture.jdbc().queryForObject("SELECT count(*) FROM training_policy_lifecycle WHERE policy_id='forbidden'", Integer.class));

            assertThrows(IllegalArgumentException.class, () -> fixture.timed((policy, owner) -> {
                throw new IllegalArgumentException("mapping rejected");
            }, 500, 2000).findByPolicyIdAndOwnerId("policy-renewal-a", "owner-a"));
            PostgresTimeoutFixture.Baseline recovered = fixture.baseline();
            assertEquals(before.lockTimeout(), recovered.lockTimeout());
            assertEquals(before.statementTimeout(), recovered.statementTimeout());
        }
    }

    @Test
    void translatesOnlyKnownStatesWithoutDroppingCauses() {
        SQLException lock = new SQLException("relation is locked", "55P03");
        SQLException queryCanceled = new SQLException("canceling statement due to statement timeout", "57014");
        for (SQLException sql : new SQLException[]{lock, queryCanceled}) {
            RuntimeException translated = PreviewReadFailureTranslator.translate(
                    new CannotGetJdbcConnectionException("outer framework wrapper", sql));
            assertInstanceOf(PreviewReadInterruptedException.class, translated);
            assertTrue(hasCause(translated, sql), "preserve original SQLException for logs/diagnosis");
        }
        RuntimeException unknown = new IllegalStateException("not a SQL-state budget failure",
                new SQLException("unique violation", "23505"));
        assertSame(unknown, PreviewReadFailureTranslator.translate(unknown));
    }

    @Test
    void injectedLifetimeDefectIsDetectedWithoutTreatingItAsEmptySuccess() throws Exception {
        try (PostgresTimeoutFixture fixture = PostgresTimeoutFixture.open()) {
            // Instructor witness: intentionally set session-level state on the one-slot backend.
            try (var connection = fixture.appPool().getConnection(); var statement = connection.createStatement()) {
                statement.execute("SELECT set_config('lock_timeout','17ms',false)");
            }
            PostgresTimeoutFixture.Baseline leaked = fixture.baseline();
            assertEquals("17ms", leaked.lockTimeout(), "the injected defect must be detected by the fixture");

            // The learner decorator must not swallow mapping/cleanup failures as empty.
            TimedPolicyRenewalReadStore faultyMapping = fixture.timed((p, o) -> {
                throw new IllegalStateException("injected mapper defect");
            }, 500, 2000);
            assertThrows(IllegalStateException.class,
                    () -> faultyMapping.findByPolicyIdAndOwnerId("policy-renewal-a", "owner-a"));
        }
    }

    private static boolean hasCause(Throwable outer, Throwable expected) {
        for (Throwable cause = outer; cause != null; cause = cause.getCause()) if (cause == expected) return true;
        return false;
    }
}
