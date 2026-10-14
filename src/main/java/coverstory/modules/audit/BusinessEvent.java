package coverstory.modules.audit;

import java.time.Instant;
import java.util.UUID;

/** Allowlisted business transition record; intentionally has no arbitrary properties map. */
public record BusinessEvent(UUID eventId, UUID ownerId, String aggregateType,
                            UUID aggregateId, String eventType, Instant occurredAt) { }
