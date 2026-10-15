package capstone.coverstory.application.internal;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Learner-owned callback reducer. Provider bodies and private answers are never telemetry fields. */
public final class InsurerReviewEventReducer {
    private InsurerReviewEventReducer() { }
    public record Snapshot(UUID ownerId,UUID applicationId,String status,long version,Set<String> eventIds,List<String> timeline) { }
    public record Event(UUID ownerId,UUID applicationId,String eventId,long version,String nextStatus) { }
    public record Diagnostic(String routeCode,String outcome,long durationMillis,UUID requestId) { }
    public record Result(Snapshot snapshot,String outcome,Diagnostic diagnostic) { }
    public static Result apply(Snapshot current,Event event,UUID requestId,long durationMillis,Throwable providerFailure) {
        // TODO(CAP-112): deduplicate by owner/event, fence older versions, and return only bounded metadata.
        throw new UnsupportedOperationException("TODO(CAP-112): implement simulated insurer review reducer");
    }
}
