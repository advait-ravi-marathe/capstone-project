package coverstory.modules.application.api;

import java.util.UUID;

/** Application-owned accepted offer revision facts exposed to Payments through a public module API. */
public interface AcceptedOfferRevisionFactsPort {
    AcceptedOfferFacts findAcceptedOffer(UUID applicationId);
    record AcceptedOfferFacts(UUID ownerId, UUID applicationId, UUID offerId, String revision,
                              String contentSha256, long totalMinor, String currency, boolean accepted) { }
}
