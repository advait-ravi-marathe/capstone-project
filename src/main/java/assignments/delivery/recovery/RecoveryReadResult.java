package assignments.delivery.recovery;

import java.util.List;
import java.util.Objects;

public record RecoveryReadResult(RecoveryObservation observation, List<RecoveryHistoryEntry> supplementaryHistory) {
    public RecoveryReadResult {
        Objects.requireNonNull(observation);
        supplementaryHistory = List.copyOf(supplementaryHistory);
        if (supplementaryHistory.size() > 20) throw new IllegalArgumentException("history is bounded to 20 entries");
    }
}
