package coverstory.modules.application.api;

import java.util.UUID;

/** Application-owned authoritative read of the currently reviewed offer pair. */
public interface OfferRevisionFactsPort {
    OfferPair findCurrentPair(UUID syntheticCaseId);
    record OfferFacts(UUID ownerId, UUID offerId, String revision, String contentSha256,
                      long amountMinor, String currency, java.util.Set<String> termCodes,
                      java.time.Instant expiresAt) {
        public OfferFacts { termCodes=java.util.Set.copyOf(termCodes); }
    }
    record OfferPair(OfferFacts original, OfferFacts revised) { }
}
