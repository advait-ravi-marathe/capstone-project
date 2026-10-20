package assignments.insurance;

import java.math.BigDecimal;
import java.time.Instant;

/** Immutable quote facts captured at creation; no customer or payment state. */
public record QuoteSnapshot(
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
