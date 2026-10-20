package assignments.insurance;

import javax.sql.DataSource;
import java.sql.SQLException;

/** Learner-owned bounded quote-history query; persistence remains read-only. */
public final class QuoteHistoryQuery {
    private final DataSource dataSource;

    public QuoteHistoryQuery(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public QuoteHistoryPage page(String journeyId, int page, int size) throws SQLException {
        // LEARNER TODO: validate page >= 0 and 1 <= size <= 20, then query only
        // this journey with LIMIT/OFFSET. Order by created_at DESC, quote_version_id ASC.
        // Return the total count and rows on one deterministic snapshot where possible.
        throw new UnsupportedOperationException("Day 16 learner TODO: query quote history");
    }
}
