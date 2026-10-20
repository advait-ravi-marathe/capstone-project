package assignments.insurance;

import java.math.BigDecimal;
import java.time.Instant;

/** Public immutable quote projection for a journey summary response. */
public record JourneyQuoteSummary(
        String quoteVersionId,
        String productCode,
        String productVersion,
        String rateVersion,
        BigDecimal rebuildAmountRupees,
        String coverageCode,
        String currency,
        BigDecimal annualPremiumRupees,
        Instant createdAt,
        Instant expiresAt) { }
