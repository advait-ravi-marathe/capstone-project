package assignments.insurance;

import java.util.Optional;

/** Lookup must scope by both policy ID and the authenticated owner ID. */
@FunctionalInterface
public interface PolicyRenewalReadStore {
    Optional<PolicyRenewalFacts> findByPolicyIdAndOwnerId(String policyId, String ownerId);
}
