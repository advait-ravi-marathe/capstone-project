package assignments.delivery.recovery;

import assignments.delivery.IssuanceEnvelope;
import assignments.delivery.IssuanceSourceKey;

import java.time.Instant;
import java.util.Objects;

/** One exact-key observation; history, when requested, is read separately as supplementary context. */
public record RecoveryObservation(
        IssuanceSourceKey key,
        boolean workPresent,
        IssuanceEnvelope envelope,
        int controlRevision,
        Instant acknowledgedAt,
        Instant quarantinedAt,
        RecoveryReason quarantineReason,
        ReceiptRelation receiptRelation) {
    public RecoveryObservation {
        Objects.requireNonNull(key);
        Objects.requireNonNull(receiptRelation);
        if (workPresent) {
            Objects.requireNonNull(envelope);
            if (!key.policyId().equals(envelope.policyId()) || key.sourceVersion() != envelope.sourceVersion())
                throw new IllegalArgumentException("observation envelope identity mismatch");
            if (controlRevision < 0) throw new IllegalArgumentException("control revision must be nonnegative");
            if ((quarantinedAt == null) != (quarantineReason == null))
                throw new IllegalArgumentException("quarantine time and reason must be present together");
        } else if (envelope != null || acknowledgedAt != null || quarantinedAt != null || quarantineReason != null
                || controlRevision != 0 || receiptRelation != ReceiptRelation.ABSENT) {
            throw new IllegalArgumentException("not-found observation cannot contain row evidence");
        }
    }

    public boolean quarantined() { return quarantinedAt != null; }
    public boolean acknowledged() { return acknowledgedAt != null; }
    public static RecoveryObservation notFound(IssuanceSourceKey key) {
        return new RecoveryObservation(key, false, null, 0, null, null, null, ReceiptRelation.ABSENT);
    }
}
