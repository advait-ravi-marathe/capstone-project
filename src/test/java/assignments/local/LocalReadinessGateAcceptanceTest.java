package assignments.local;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertEquals(LocalReadinessGate.Readiness.RECOVERY_REQUIRED, gate.assess(null));
        assertEquals(LocalReadinessGate.Readiness.RECOVERY_REQUIRED, gate.assess(Map.of()));
        assertEquals(LocalReadinessGate.Readiness.RECOVERY_REQUIRED,
                gate.assess(Map.of("irrelevant-smoke", true, "day-56", true)));
        Map<String, Boolean> nullEvidence = new HashMap<>();
        nullEvidence.put("day-55", true);
        nullEvidence.put("day-56", null);
        Map<String, Boolean> before = new HashMap<>(nullEvidence);
        assertEquals(LocalReadinessGate.Readiness.RECOVERY_REQUIRED, gate.assess(nullEvidence));
        assertEquals(before, nullEvidence, "readiness assessment must not mutate learner evidence");
    }

    @Test
    void day63SecurityCheckpointKeepsRecoveryInsideDailyBudget() {
        var gate = new LocalReadinessGate();
        assertEquals(new LocalReadinessGate.SecurityCheckpoint(true, 85, false),
                gate.securityCheckpoint(Map.of("day-62-security", false), 85));
        assertEquals(new LocalReadinessGate.SecurityCheckpoint(true, 60, false),
                gate.securityCheckpoint(Map.of(), 60));
        assertEquals(new LocalReadinessGate.SecurityCheckpoint(false, 0, false),
                gate.securityCheckpoint(Map.of("day-62-security", true), 0));
        assertEquals(new LocalReadinessGate.SecurityCheckpoint(false, 85, false),
                gate.securityCheckpoint(Map.of("day-62-security", true), 85));
        assertThrows(IllegalArgumentException.class, () -> gate.securityCheckpoint(Map.of(), -1));
        assertThrows(IllegalArgumentException.class, () -> gate.securityCheckpoint(Map.of(), 86));
        Map<String, Boolean> nullEvidence = new HashMap<>();
        nullEvidence.put("day-62-security", null);
        assertEquals(new LocalReadinessGate.SecurityCheckpoint(true, 60, false),
                gate.securityCheckpoint(nullEvidence, 60));
    }
}
