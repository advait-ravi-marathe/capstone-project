package assignments.insurance;

import java.math.BigDecimal;
import java.util.Map;

/** Learner-owned synthetic decimal premium calculator; not an insurance rate. */
public final class PremiumCalculator implements PremiumQuoteCalculator {
    private final BigDecimal rate;
    private final Map<String, BigDecimal> coverageAdjustments;

    public PremiumCalculator(BigDecimal rate, Map<String, BigDecimal> coverageAdjustments) {
        this.rate = rate;
        this.coverageAdjustments = Map.copyOf(coverageAdjustments);
    }

    @Override
    public BigDecimal calculate(BigDecimal rebuildAmountRupees, String coverageCode) {
        // LEARNER TODO: validate the input/coverage, calculate rebuildAmount * rate
        // plus its fixture adjustment, then apply scale 2 with HALF_UP.
        throw new UnsupportedOperationException("Day 6 learner TODO: implement PremiumCalculator");
    }
}
