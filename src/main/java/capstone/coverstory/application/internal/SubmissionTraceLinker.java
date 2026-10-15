package capstone.coverstory.application.internal;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Learner-owned correlation seam. Trace links are observability metadata, never authority. */
public final class SubmissionTraceLinker {
    private SubmissionTraceLinker() { }
    public record Span(String spanId, String traceId, UUID ownerId, String operation,
                       Instant startedAt, Instant endedAt, String applicationStatus) { }
    public record SpanLink(String fromSpanId, String toSpanId, String relation) { }
    public record Correlation(List<SpanLink> links, String unresolvedReference, boolean authorized) { }

    public static Correlation correlate(UUID requester, String submitSpanId, String dispatchSpanId,
                                        String investigationSpanId, List<Span> storedSpans) {
        // TODO(CAP-175): resolve only persisted owner-scoped spans; never invent trace authority.
        throw new UnsupportedOperationException("TODO(CAP-175): implement read-only span correlation");
    }
}
