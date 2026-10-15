package capstone.coverstory.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/** Independent arithmetic witness. The rates are assumptions, never benchmark evidence. */
final class CAP111CapacityOracle {
    record Estimate(double browserRps, double originRps, double transactionRps,
                    boolean capacityProven, Map<String, String> caveats) { }

    static Estimate estimate(long browsers, int browserWindowSeconds, double transactionFraction,
                             int transactionWindowSeconds, double staticHitRatio) {
        if (browsers <= 0 || browserWindowSeconds <= 0 || transactionWindowSeconds <= 0
                || transactionFraction < 0 || transactionFraction > 1 || staticHitRatio < 0 || staticHitRatio > 1)
            throw new IllegalArgumentException("invalid capacity assumptions");
        double browser = round((double) browsers / browserWindowSeconds);
        double origin = round(browser * (1 - staticHitRatio));
        double transactions = round((double) browsers * transactionFraction / transactionWindowSeconds);
        return new Estimate(browser, origin, transactions, false,
                Map.of("measurement", "not_benchmarked", "load_shape", "assumption_only", "scale_claim", "not_proven"));
    }

    private static double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
