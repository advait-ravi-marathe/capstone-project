package assignments.insurance;

import java.time.Instant;

/** Instructor-owned adapter demonstrating that freshness rejection precedes a write. */
public final class QuoteSelectionFreshnessGuard {
    private final QuoteFreshnessPolicy policy;

    public QuoteSelectionFreshnessGuard(QuoteFreshnessPolicy policy) {
        this.policy = policy;
    }

    public void select(QuoteFreshnessState state, Instant now, Runnable persistSelection) {
        policy.assertSelectable(state, now);
        persistSelection.run();
    }
}
