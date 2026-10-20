package assignments.insurance;

import javax.sql.DataSource;
import java.sql.SQLException;

/** Learner-owned atomic/idempotent database write for one already authorized pair. */
public final class IdempotentSelectionWriter {
    private final DataSource dataSource;

    public IdempotentSelectionWriter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public QuoteSelectionKey select(String journeyId, String quoteVersionId) throws SQLException {
        // LEARNER TODO: in a real PostgreSQL transaction, create-or-return this exact
        // composite pair so two concurrent identical requests return the same row.
        // Do not call Day 1 dedup or infer authorization from the IDs.
        throw new UnsupportedOperationException("Day 25 learner TODO: implement concurrent idempotent selection");
    }
}
