package assignments.insurance;

import java.time.LocalDate;

/** Pure rules for the synthetic, non-binding annual renewal preview. */
public class PolicyRenewalPreviewPolicy {
    public PolicyRenewalTerm termFor(LocalDate effectiveDate) {
        // LEARNER TODO Day 45: use an exclusive anniversary and a 30-day preview window.
        throw todo(45);
    }

    public PolicyRenewalEligibility eligibility(PolicyRenewalFacts facts, LocalDate today) {
        // LEARNER TODO Day 46: enforce lifecycle status and both renewal-window boundaries.
        throw todo(46);
    }

    public PolicyRenewalPreview buildPreview(PolicyRenewalFacts facts) {
        // LEARNER TODO Day 47: copy the frozen current snapshot; do not recalculate or bind a price.
        throw todo(47);
    }

    private static UnsupportedOperationException todo(int day) {
        return new UnsupportedOperationException("Day %02d learner TODO: implement renewal preview policy".formatted(day));
    }
}
