package assignments.local;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("learnerAcceptance")
class LocalReadinessGateAcceptanceTest {
    @Test
    void day57MissingOrFailedPriorEvidenceReplacesNewWork() {
        LocalReadinessGate gate = new LocalReadinessGate();
        assertEquals(LocalReadinessGate.Readiness.RECOVERY_REQUIRED,
                gate.assess(Map.of("day-56", true, "day-55", false)));
        assertEquals(LocalReadinessGate.Readiness.RECOVERY_REQUIRED,
                gate.assess(Map.of("day-56", true)));
        assertEquals(LocalReadinessGate.Readiness.READY,
                gate.assess(Map.of("day-55", true, "day-56", true)));
    }

    @Test
    void day63SecurityCheckpointKeepsRecoveryInsideDailyBudget() {
        assertEquals(new LocalReadinessGate.SecurityCheckpoint(true, 85, false),
                new LocalReadinessGate().securityCheckpoint(Map.of("day-62-security", false), 85));
    }
}
