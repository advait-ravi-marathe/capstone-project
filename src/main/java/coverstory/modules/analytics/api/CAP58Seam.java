package coverstory.modules.analytics.api;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/** Typed instructor contract. Client labels and timestamps never grant authority. */
public class CAP58Seam {
    public enum ResultStatus { VALID, DISCARDED_BY_POLICY, INVALID }
    public record TrustedAnalyticsContext(String ownerId, boolean collectionAllowed, Set<String> ownedJourneyIds, Instant serverReceivedAt) { }
    /** Internal validated value; pseudonymousSessionId is hashed before persistence and never returned over HTTP. */
    public record SanitizedEvent(String eventId, String eventType, String pageId, String stepCode,
                                 String targetId, Integer durationMs, String pseudonymousSessionId, Instant receivedAt) { }
    public record ValidationResult(ResultStatus status, List<SanitizedEvent> events, String fieldCode) { }

    /** TODO(CAP-58): enforce bounded byte/schema/privacy/ownership rules before constructing sanitized events. */
    public ValidationResult validateAnalyticsBatch(byte[] utf8Body, TrustedAnalyticsContext context) {
        throw new UnsupportedOperationException("TODO(CAP-58): implement analytics batch validation");
    }
}
