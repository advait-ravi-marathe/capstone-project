package assignments.delivery.recovery;

import assignments.delivery.IssuanceSourceKey;

import java.time.Instant;
import java.util.Objects;

public record RecoveryTransitionResult(IssuanceSourceKey key, RecoveryAction action, int previousRevision,
                                       int controlRevision, RecoveryReason reason, Instant decidedAt) {
    public RecoveryTransitionResult {
        Objects.requireNonNull(key); Objects.requireNonNull(action); Objects.requireNonNull(reason); Objects.requireNonNull(decidedAt);
        if (previousRevision < 0 || controlRevision != previousRevision + 1)
            throw new IllegalArgumentException("control revision must advance once");
    }
}
