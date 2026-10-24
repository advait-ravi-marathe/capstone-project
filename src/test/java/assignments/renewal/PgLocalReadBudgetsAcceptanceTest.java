package assignments.renewal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.jupiter.api.Assertions.*;

/** Day 88 direct seam test; it does not invoke Day 89's timed-store orchestration. */
@Tag("learnerAcceptance")
class PgLocalReadBudgetsAcceptanceTest {
    @Test
    void appliesSettingsInsideOwnedTransactionAndRejectsCallsOutsideIt() throws Exception {
        try (PostgresTimeoutFixture fixture = PostgresTimeoutFixture.open()) {
            var original = fixture.baseline();
            PreviewReadBudgetPolicy budgets = new PreviewReadBudgetPolicy(500, 2000);

            assertThrows(IllegalStateException.class,
                    () -> PgLocalReadBudgets.apply(fixture.jdbc(), budgets),
                    "settings must not be applied in an implicit autocommit transaction");
            assertEquals(original, fixture.baseline(), "rejected call must not change pooled backend state");

            fixture.tx().execute(transaction -> {
                assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
                String[] before = fixture.jdbc().queryForObject(
                        "SELECT pg_backend_pid(), current_setting('lock_timeout'), " +
                                "current_setting('statement_timeout'), txid_current()::text",
                        (row, index) -> new String[]{row.getString(1), row.getString(2), row.getString(3), row.getString(4)});

                PgLocalReadBudgets.apply(fixture.jdbc(), budgets);

                String[] after = fixture.jdbc().queryForObject(
                        "SELECT pg_backend_pid(), current_setting('lock_timeout'), " +
                                "current_setting('statement_timeout'), txid_current()::text",
                        (row, index) -> new String[]{row.getString(1), row.getString(2), row.getString(3), row.getString(4)});
                assertEquals(before[0], after[0], "settings are applied on the transaction-owned backend");
                assertEquals(before[3], after[3], "both observations are in the same transaction");
                assertEquals("500ms", after[1]);
                assertEquals("2s", after[2]);
                return null;
            });

            var afterCommit = fixture.baseline();
            assertEquals(original.pid(), afterCommit.pid());
            assertEquals(original.backendStart(), afterCommit.backendStart());
            assertEquals(original.lockTimeout(), afterCommit.lockTimeout(), "local lock timeout resets after commit");
            assertEquals(original.statementTimeout(), afterCommit.statementTimeout(),
                    "local statement timeout resets after commit");
            var state = fixture.activity(original.pid());
            assertEquals("idle", state.state());
            assertFalse(state.transactionOpen());
        }
    }
}
