package assignments.local;

import assignments.insurance.PolicyReadStore;
import assignments.insurance.PolicySnapshot;

import javax.sql.DataSource;
import java.util.Optional;

/** Owner-scoped read adapter for the synthetic local lifecycle database. */
public final class JdbcPolicyReadStore implements PolicyReadStore {
    private final DataSource dataSource;

    public JdbcPolicyReadStore(DataSource dataSource) { this.dataSource = dataSource; }

    @Override
    public Optional<PolicySnapshot> findByPolicyIdAndOwnerId(String policyId, String ownerId) {
        // LEARNER TODO Day 60: parameterize both identifiers and map the immutable snapshot.
        throw new UnsupportedOperationException("Day 60 learner TODO: implement owner-scoped policy read");
    }

    DataSource dataSourceForTests() { return dataSource; }
}
