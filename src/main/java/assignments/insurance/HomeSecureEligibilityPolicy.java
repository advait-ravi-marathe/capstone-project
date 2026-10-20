package assignments.insurance;

import java.math.BigDecimal;

/** Learner-owned pure training policy. These rules are not insurance advice. */
public final class HomeSecureEligibilityPolicy implements HomeSecureEligibilityCheck {
    private final String requiredOccupancy;
    private final BigDecimal minimumRebuildAmount;
    private final BigDecimal maximumRebuildAmount;

    public HomeSecureEligibilityPolicy(
            String requiredOccupancy,
            BigDecimal minimumRebuildAmount,
            BigDecimal maximumRebuildAmount) {
        this.requiredOccupancy = requiredOccupancy;
        this.minimumRebuildAmount = minimumRebuildAmount;
        this.maximumRebuildAmount = maximumRebuildAmount;
    }

    @Override
    public EligibilityDecision decide(HomeSecureEligibilityInput input) {
        // LEARNER TODO: apply the fixture's required-field, occupancy, and inclusive
        // amount-bound rules in the documented reason precedence.
        throw new UnsupportedOperationException("Day 5 learner TODO: implement HomeSecure eligibility");
    }
}
