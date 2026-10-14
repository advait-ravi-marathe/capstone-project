package coverstory.modules.application.api;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** CAP-92 instructor-prepared, synthetic retention dry-run contract. */
public final class CAP92Seam {
    public enum SourceKind { RAW_BODY, AUDIT_REFERENCE }
    public enum Decision { RETAIN, CANDIDATE_DELETE, INVALID_TIME }

    /** Body content is intentionally absent. Audit metadata contains only reference fields. */
    public record SourceRecord(UUID ownerId, String sourceId, SourceKind kind,
                               Instant createdAt, String referenceId) { }
    public record ReportRow(String sourceId, SourceKind kind, Decision decision,
                            boolean bodyPresent, String referenceId) { }
    public record RetentionPlan(UUID ownerId, Instant evaluatedAt, List<ReportRow> rows,
                                int proposedDeletes, int deleteCalls) { }

    private static final long RAW_RETENTION_DAYS = 30;
    private static final long AUDIT_RETENTION_DAYS = 90;

    /**
     * Dry-run only. Fixture durations are synthetic teaching thresholds, not legal policy.
     * Accepted purchase/payment history is outside this report and must not be removed here.
     */
    public RetentionPlan classifyRetention(UUID owner, Clock clock, List<SourceRecord> records) {
        // LEARNER TODO: scope to owner before inspecting rows, classify inclusive 30/90-day
        // fixture boundaries using the supplied fixed Clock, reject null/future times as
        // INVALID_TIME, retain audit references without body content, and only propose deletes.
        // No persistence or delete interface is available to this seam by design.
        throw new UnsupportedOperationException("CAP-92 learner TODO: classify synthetic retention eligibility");
    }
}
