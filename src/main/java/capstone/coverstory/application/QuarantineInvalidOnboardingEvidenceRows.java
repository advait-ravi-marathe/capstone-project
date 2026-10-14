package capstone.coverstory.application;

import java.util.List;
import java.util.Set;

/** CAP-100 pure classifier; no storage and no mutation of rating or disclosure state. */
public final class QuarantineInvalidOnboardingEvidenceRows {
    public enum Reason { DUPLICATE, MISSING_ID, INVALID_ATTEMPT }
    public record Batch(String trustedOwnerId, int originalRatingRevision, Set<RowKey> existingIds) {
        public Batch { existingIds = existingIds == null ? Set.of() : Set.copyOf(existingIds); }
    }
    public record RowKey(String ownerId, String rowId) { }
    public record AuditRow(String ownerId, String rowId, Object attempt, int ratingRevision,
                           String suggestedPin, boolean disclosureConfirmed) { }
    public record QuarantinedRow(int index, Reason reason) { }
    public record Counts(int accepted, int duplicate, int missingId, int invalidAttempt, int total) { }
    public record QualityReport(List<RowKey> acceptedIds, List<QuarantinedRow> quarantined,
                                Counts counts, int ratingRevision, boolean disclosureConfirmed) {
        public QualityReport { acceptedIds=List.copyOf(acceptedIds); quarantined=List.copyOf(quarantined); }
    }
    public QualityReport validateEvidenceBatch(Batch batch, List<AuditRow> rows) {
        // LEARNER TODO: validate required IDs, positive integer attempt domain
        // (Boolean must not pass), and duplicates by trusted owner + row ID.
        // Keep an outcome for every input row. Suggestions remain unconfirmed;
        // ratingRevision is the original authoritative revision. Row owner data
        // cannot override the trusted batch partition.
        throw new UnsupportedOperationException("CAP-100 learner TODO: classify evidence batch");
    }
}
