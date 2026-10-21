package assignments.delivery.recovery;

import java.util.Objects;

public record RecoveryDecision(RecoveryRecommendation recommendation) {
    public RecoveryDecision { Objects.requireNonNull(recommendation); }
}
