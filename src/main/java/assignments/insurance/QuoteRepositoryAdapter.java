package assignments.insurance;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Learner-owned insert/read boundary for immutable quote snapshots (Day 9).
 * The supplied journey ID is part of the composite key and every read scope.
 */
public final class QuoteRepositoryAdapter {
    private final DataSource dataSource;

    public QuoteRepositoryAdapter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(String journeyId, QuoteSnapshot snapshot) throws SQLException {
        // LEARNER TODO: persist all immutable fields using a parameterized INSERT.
        // Preserve NUMERIC(14,2), UTC instants and the composite journey/version key.
        throw new UnsupportedOperationException("Day 9 learner TODO: implement quote insert");
    }

    public Optional<QuoteSnapshot> find(String journeyId, String quoteVersionId) throws SQLException {
        // LEARNER TODO: query by both journeyId and quoteVersionId; map timestamp
        // values to Instant and return Optional.empty() when no scoped row exists.
        throw new UnsupportedOperationException("Day 9 learner TODO: implement scoped quote read");
    }
}
