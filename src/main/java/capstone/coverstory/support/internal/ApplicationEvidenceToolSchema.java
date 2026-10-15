package capstone.coverstory.support.internal;

import java.util.Map;

/** Learner-owned schema seam for the read-only CAP-124 application evidence tool. */
public final class ApplicationEvidenceToolSchema {
    private ApplicationEvidenceToolSchema() { }

    public record Principal(String subjectId, String ownerId) { }
    public record Request(String applicationId, int limit) { }
    public record EvidenceRow(String eventId, String kind, String occurredAt, String summary) { }
    public record Result(String status, String code, java.util.List<EvidenceRow> rows) {
        public Result { rows = java.util.List.copyOf(rows); }
    }
    public interface ApplicationReadApi {
        /** Must scope by the authenticated principal and return read-only evidence. */
        java.util.List<EvidenceRow> evidenceForOwner(String ownerId, String applicationId, int limit);
    }

    /** Validate model-controlled arguments, then invoke only the owning public read API. */
    public static Result invoke(Principal principal, Map<String, ?> arguments, ApplicationReadApi applicationReadApi) {
        // TODO(CAP-124): strictly validate the exact argument shape and 1..50 integer limit,
        // then issue a read-only owner-scoped request; reject before any API call on invalid input.
        throw new UnsupportedOperationException("TODO(CAP-124): implement strict read-only tool dispatch");
    }
}
