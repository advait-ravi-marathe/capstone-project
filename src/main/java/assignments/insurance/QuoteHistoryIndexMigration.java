package assignments.insurance;

import javax.sql.DataSource;
import java.sql.SQLException;

/** Learner-owned index migration for bounded quote-history reads (Day 24). */
public final class QuoteHistoryIndexMigration {
    private final DataSource dataSource;

    public QuoteHistoryIndexMigration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void apply() throws SQLException {
        // LEARNER TODO: add the suitable index for the Day 16 order/filter query.
        // Preserve query results and explain the index's write/storage cost.
        throw new UnsupportedOperationException("Day 24 learner TODO: add and apply quote-history index");
    }
}
