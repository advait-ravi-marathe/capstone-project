package assignments.delivery.recovery;

import java.util.List;
import java.util.Objects;

public record RecoveryInspection(RecoveryObservation observation, RecoveryDecision decision,
                                 List<RecoveryHistoryEntry> supplementaryHistory) {
    public RecoveryInspection {
        Objects.requireNonNull(observation);
        Objects.requireNonNull(decision);
        supplementaryHistory = List.copyOf(supplementaryHistory);
        if (supplementaryHistory.size() > 20) throw new IllegalArgumentException("history is bounded to 20 entries");
    }
}
