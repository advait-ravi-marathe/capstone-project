package assignments.local;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.HashMap;

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
        assertEquals(LocalReadinessGate.Readiness.RECOVERY_REQUIRED,
                gate.assess(Map.of("day-55", true)));
        assertEquals(LocalReadinessGate.Readiness.RECOVERY_REQUIRED, gate.assess(null));
        assertEquals(LocalReadinessGate.Readiness.RECOVERY_REQUIRED, gate.assess(Map.of()));
        assertEquals(LocalReadinessGate.Readiness.RECOVERY_REQUIRED, gate.assess(Map.of("package-smoke", true, "ui-built", true)));
        var nullable = new HashMap<String, Boolean>();
        nullable.put("day-55", true);
        nullable.put("day-56", null);
        var nullableBefore = new HashMap<>(nullable);
        assertEquals(LocalReadinessGate.Readiness.RECOVERY_REQUIRED, gate.assess(nullable));
        assertEquals(nullableBefore, nullable);
        assertEquals(LocalReadinessGate.Readiness.READY,
                gate.assess(Map.of("day-55", true, "day-56", true)));
    }

    @Test
    void day63SecurityCheckpointKeepsRecoveryInsideDailyBudget() {
        assertEquals(new LocalReadinessGate.SecurityCheckpoint(true, 85, false),
                new LocalReadinessGate().securityCheckpoint(Map.of("day-62-security", false), 85));
        assertEquals(new LocalReadinessGate.SecurityCheckpoint(true, 60, false),
                new LocalReadinessGate().securityCheckpoint(Map.of(), 60));
        assertEquals(new LocalReadinessGate.SecurityCheckpoint(true, 60, false),
                new LocalReadinessGate().securityCheckpoint(null, 60));
        assertEquals(new LocalReadinessGate.SecurityCheckpoint(false, 60, false),
                new LocalReadinessGate().securityCheckpoint(Map.of("day-62-security", true), 60));
        assertEquals(new LocalReadinessGate.SecurityCheckpoint(false, 0, false),
                new LocalReadinessGate().securityCheckpoint(Map.of("day-62-security", true), 0));
        assertThrows(IllegalArgumentException.class,
                () -> new LocalReadinessGate().securityCheckpoint(Map.of("day-62-security", false), -1));
        assertThrows(IllegalArgumentException.class,
                () -> new LocalReadinessGate().securityCheckpoint(Map.of("day-62-security", false), 86));
    }
}
