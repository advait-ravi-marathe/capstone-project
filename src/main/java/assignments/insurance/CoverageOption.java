package assignments.insurance;

import java.math.BigDecimal;

/** Immutable catalog choice from the synthetic HomeSecure fixture. */
public record CoverageOption(String code, String label, BigDecimal annualAdjustmentRupees) { }
