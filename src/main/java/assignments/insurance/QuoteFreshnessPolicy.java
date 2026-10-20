package assignments.insurance;

import java.time.Instant;

/** Learner-owned stale and expiry invariant for a quote selection. */
public final class QuoteFreshnessPolicy {
    public void assertSelectable(QuoteFreshnessState state, Instant now) {
        // LEARNER TODO: enforce journey ownership, expiry-at-now, then current version.
        // Wrong journey is non-disclosing; expired and superseded have distinct types.
        throw new UnsupportedOperationException("Day 11 learner TODO: enforce quote freshness");
    }
}
