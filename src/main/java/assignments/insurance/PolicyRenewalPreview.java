package assignments.insurance;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Reference-only view of the current snapshot and the next term boundary. */
public record PolicyRenewalPreview(String policyId, long sourcePolicyVersion,
                                   String productVersion, String rateVersion,
                                   BigDecimal currentAnnualPremiumRupees, String currency,
                                   LocalDate nextTermEffectiveDate, String notice) { }
