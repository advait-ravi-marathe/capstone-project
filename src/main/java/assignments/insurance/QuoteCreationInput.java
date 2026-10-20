package assignments.insurance;

import java.math.BigDecimal;

/** Caller facts used to create one synthetic immutable quote snapshot. */
public record QuoteCreationInput(String occupancy, BigDecimal rebuildAmountRupees, String coverageCode) { }
