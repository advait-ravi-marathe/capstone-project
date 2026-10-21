package assignments.insurance;

import java.util.Optional;

/** Learner-facing data seam; lookup must be constrained by both policy and authenticated owner. */
@FunctionalInterface
public interface PolicyReadStore {
    Optional<PolicySnapshot> findByPolicyIdAndOwnerId(String policyId, String ownerId);
}
