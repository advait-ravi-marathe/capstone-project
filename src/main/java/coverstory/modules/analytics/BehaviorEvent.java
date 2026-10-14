package coverstory.modules.analytics;

import java.time.Instant;
import java.util.UUID;

/** Bounded event schema: no arbitrary map, URL, token, or consumer-entered text. */
public record BehaviorEvent(UUID eventId, String eventName, String screenKey,
                            String sessionScope, Instant occurredAt) { }
