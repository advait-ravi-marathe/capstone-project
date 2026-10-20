package assignments.insurance;

import java.time.Instant;

/** Fixed metadata needed to decide whether one requested quote may be selected. */
public record QuoteFreshnessState(
        String requestedJourneyId,
        String quoteJourneyId,
        String requestedQuoteVersionId,
        String currentQuoteVersionId,
        Instant expiresAt) { }
