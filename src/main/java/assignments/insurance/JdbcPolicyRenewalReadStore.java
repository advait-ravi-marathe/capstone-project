package assignments.insurance;

import javax.sql.DataSource;
import java.util.Optional;

/** JDBC projection over the synthetic policy lifecycle table; it performs no writes. */
public final class JdbcPolicyRenewalReadStore implements PolicyRenewalReadStore {
    private final DataSource dataSource;

    public JdbcPolicyRenewalReadStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<PolicyRenewalFacts> findByPolicyIdAndOwnerId(String policyId, String ownerId) {
        // LEARNER TODO Day 48: use a parameterized owner-scoped projection and return no row for foreign owners.
        throw todo();
    }

    private static UnsupportedOperationException todo() {
        return new UnsupportedOperationException("Day 48 learner TODO: implement owner-scoped renewal read");
    }
}
