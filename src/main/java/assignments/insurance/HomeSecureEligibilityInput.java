package assignments.insurance;

import java.math.BigDecimal;

/** Synthetic, training-only HomeSecure eligibility facts. */
public record HomeSecureEligibilityInput(String occupancy, BigDecimal rebuildAmountRupees) { }
