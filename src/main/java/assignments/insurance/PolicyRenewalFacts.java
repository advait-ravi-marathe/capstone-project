package assignments.insurance;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Minimal owner-scoped projection needed to create a renewal preview. */
public record PolicyRenewalFacts(String policyId, LocalDate effectiveDate, PolicyStatus status,
                                 long policyVersion, String productVersion, String rateVersion,
                                 BigDecimal currentAnnualPremiumRupees, String currency) { }
