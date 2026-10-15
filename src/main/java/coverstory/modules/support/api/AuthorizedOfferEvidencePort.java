package coverstory.modules.support.api;

import java.util.Set;
import java.util.UUID;

/** Support-owned allowlist of source/version citations valid for one synthetic offer owner. */
public interface AuthorizedOfferEvidencePort {
    Set<EvidenceCitation> authorizedFor(UUID ownerId);
    record EvidenceCitation(UUID ownerId, String sourceId, String version) { }
}
