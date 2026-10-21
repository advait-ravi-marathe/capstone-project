package assignments.renewal;

import org.springframework.jdbc.core.JdbcTemplate;

/** Applies only transaction-local PostgreSQL settings on the transaction-bound connection. */
public final class PgLocalReadBudgets {
    private PgLocalReadBudgets() { }

    public static void apply(JdbcTemplate jdbcTemplate, PreviewReadBudgetPolicy budgets) {
        // LEARNER TODO Day 88: bind both setting names and values through set_config(..., true).
        // Never concatenate learner-controlled SQL; require an already active owned transaction.
        throw new UnsupportedOperationException("Day 88 learner TODO: apply transaction-local PostgreSQL budgets");
    }
}
