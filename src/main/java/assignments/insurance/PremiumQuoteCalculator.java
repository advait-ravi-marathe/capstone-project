package assignments.insurance;

import java.math.BigDecimal;

/** Pricing boundary injected into quote creation so day-specific tests stay isolated. */
@FunctionalInterface
public interface PremiumQuoteCalculator {
    BigDecimal calculate(BigDecimal rebuildAmountRupees, String coverageCode);
}
