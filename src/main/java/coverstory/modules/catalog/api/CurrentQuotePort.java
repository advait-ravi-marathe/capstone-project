package coverstory.modules.catalog.api;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Catalog-owned authoritative current sandbox quote. It contains no insurer or customer data. */
public interface CurrentQuotePort {
    record Snapshot(UUID ownerId, String quoteId, String revision, String scenarioId, String sourceId,
                    String sourceVersion, Long totalPaise, String currency, boolean taxKnown,
                    boolean available, boolean illustration, String termsHash, Instant expiresAt) { }
    Optional<Snapshot> findCurrent(UUID ownerId, String quoteId);
}
