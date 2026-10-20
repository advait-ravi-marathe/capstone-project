package assignments.renewal;

import assignments.insurance.PolicyRenewalFacts;
import assignments.insurance.PolicyRenewalReadStore;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import java.util.Optional;

/** Synchronous bounded decorator around the existing owner-scoped read adapter. */
public final class BoundedPolicyRenewalReadStore implements PolicyRenewalReadStore {
    private final PolicyRenewalReadStore delegate;
    private final PreviewAdmission admission;

    public BoundedPolicyRenewalReadStore(PolicyRenewalReadStore delegate, PreviewAdmission admission) {
        this.delegate = delegate;
        this.admission = admission;
    }

    @Override
    public Optional<PolicyRenewalFacts> findByPolicyIdAndOwnerId(String policyId, String ownerId) {
        // LEARNER TODO Day 75: reject before delegate when no permit is available; otherwise
        // preserve both arguments, release on every outcome, and wrap only
        // CannotGetJdbcConnectionException as PreviewUnavailableException.
        throw new UnsupportedOperationException("Day 75 learner TODO: reject busy, delegate once and release in all outcomes");
    }
}
