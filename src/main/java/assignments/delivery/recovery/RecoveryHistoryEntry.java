package assignments.delivery.recovery;

import java.time.Instant;
import java.util.Objects;

public record RecoveryHistoryEntry(int controlRevision, RecoveryAction action, RecoveryReason reason, Instant decidedAt) {
    public RecoveryHistoryEntry {
        if (controlRevision < 1) throw new IllegalArgumentException("history revision must be positive");
        Objects.requireNonNull(action);
        Objects.requireNonNull(reason);
        Objects.requireNonNull(decidedAt);
    }
}
