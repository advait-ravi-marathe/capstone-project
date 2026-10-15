package coverstory.modules.application.api;

import java.util.Set;
import java.util.UUID;

/** Application-owned authority for checking candidate journey identifiers in bulk. */
public interface ApplicationJourneyOwnershipPort {
    Set<UUID> ownedJourneyIds(String authenticatedOwnerId, Set<UUID> candidateIds);
}
