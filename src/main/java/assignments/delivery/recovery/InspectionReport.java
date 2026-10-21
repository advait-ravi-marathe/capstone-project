package assignments.delivery.recovery;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Version 1 allowlisted local evidence projection. Supplementary history is not atomic with observation. */
public record InspectionReport(int schemaVersion, RequestedKey key, boolean workPresent,
                               CapturedEnvelope envelope, int controlRevision,
                               Instant acknowledgedAt, Instant quarantinedAt, RecoveryReason quarantineReason,
                               ReceiptRelation receiptRelation, RecoveryRecommendation recommendation,
                               List<HistoryItem> history, boolean historySupplementary) {
    public InspectionReport { history = List.copyOf(history); }
    public record RequestedKey(String policyId, long sourceVersion) {}
    public record CapturedEnvelope(String policyId, long sourceVersion, String eventType,
                                   LocalDate effectiveDate, int schemaVersion) {}
    public record HistoryItem(int controlRevision, RecoveryAction action, RecoveryReason reason, Instant decidedAt) {}
}
