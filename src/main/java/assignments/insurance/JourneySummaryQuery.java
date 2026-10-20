package assignments.insurance;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Optional;

/** Learner-owned Day 15 owner-scoped read projection backed by PostgreSQL. */
public final class JourneySummaryQuery {
    private final DataSource dataSource;

    public JourneySummaryQuery(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<JourneySummary> find(String journeyId, String ownerId) throws SQLException {
        // LEARNER TODO: read one owner-scoped journey summary using the fixed schema.
        // Unknown and other-owned journeys must have the same absence. Return only
        // the response projection, with selections ordered by selection_order.
        throw new UnsupportedOperationException("Day 15 learner TODO: query journey summary");
    }
}
