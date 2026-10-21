package assignments.renewal;

/** Transaction-local limits for one synchronous renewal preview read. */
public record PreviewReadBudgetPolicy(long lockTimeoutMillis, long statementTimeoutMillis) {
    public void validate() {
        // LEARNER TODO Day 86: prove both positive budgets convert exactly to PostgreSQL integer
        // milliseconds and lockTimeoutMillis is strictly less than statementTimeoutMillis.
        throw new UnsupportedOperationException("Day 86 learner TODO: validate finite PostgreSQL read budgets");
    }
}
