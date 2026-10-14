package coverstory.application;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CAP99InstructorInfrastructureTest {
    private static final String COMMIT = "0123456789abcdef0123456789abcdef01234567";
    private static final String HASH = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
    private List<CAP99Seam.GateEvidence> green() {
        var rows = new ArrayList<CAP99Seam.GateEvidence>();
        for (var gate : CAP99Seam.Gate.values()) rows.add(new CAP99Seam.GateEvidence(gate,
                CAP99Seam.EvidenceStatus.VERIFIED,
                new CAP99Seam.Receipt(CAP99Seam.EvidenceKind.TEST_OUTPUT,
                        "mvn -q -Dtest=CAP99InstructorInfrastructureTest test", "PASS observed synthetic oracle output", COMMIT, HASH), ""));
        return rows;
    }

    @Test void allActualGatesProduceOnlyLocalReviewReadiness() {
        var decision = CAP99ReferenceEvaluator.evaluate(green());
        assertEquals(CAP99Seam.Decision.READY_FOR_REVIEW, decision.decision());
        assertTrue(decision.findings().getFirst().contains("local review only"));
        assertFalse(decision.findings().toString().toLowerCase().contains("release approved"));
    }

    @Test void missingMigrationNamesCAP98AndBlocks() {
        var rows = green(); rows.removeIf(e -> e.gate() == CAP99Seam.Gate.MIGRATION_COMPATIBILITY_RESTORE);
        var decision = CAP99ReferenceEvaluator.evaluate(rows);
        assertEquals(CAP99Seam.Decision.BLOCKED, decision.decision());
        assertTrue(decision.findings().stream().anyMatch(f -> f.contains("migration") && f.contains("CAP-98")));
    }

    @Test void oneCrossOwnerFailureBlocksFiveGreenGates() {
        var rows = green();
        rows.replaceAll(e -> e.gate() == CAP99Seam.Gate.OWNERSHIP
                ? new CAP99Seam.GateEvidence(e.gate(), CAP99Seam.EvidenceStatus.FAIL, e.receipt(), "owner B read owner A") : e);
        var decision = CAP99ReferenceEvaluator.evaluate(rows);
        assertEquals(CAP99Seam.Decision.BLOCKED, decision.decision());
        assertTrue(decision.findings().stream().anyMatch(f -> f.contains("cross-owner failure")));
    }

    @Test void pathsAndScreenshotsWithoutObservedTestOutputStayUnverified() {
        var rows = green();
        rows.set(0, new CAP99Seam.GateEvidence(CAP99Seam.Gate.ARTIFACT, CAP99Seam.EvidenceStatus.VERIFIED,
                new CAP99Seam.Receipt(CAP99Seam.EvidenceKind.SCREENSHOT, "", "", "", ""), ""));
        assertEquals(CAP99Seam.Decision.UNVERIFIED, CAP99ReferenceEvaluator.evaluate(rows).decision());
        rows.set(0, new CAP99Seam.GateEvidence(CAP99Seam.Gate.ARTIFACT, CAP99Seam.EvidenceStatus.VERIFIED,
                new CAP99Seam.Receipt(CAP99Seam.EvidenceKind.PLANNED_PATH, "", "", "", ""), ""));
        assertEquals(CAP99Seam.Decision.UNVERIFIED, CAP99ReferenceEvaluator.evaluate(rows).decision());
    }

    @Test void unverifiedNeverBecomesReadyAndOwnershipFailureDominatesIt() {
        var rows = green();
        rows.set(2, new CAP99Seam.GateEvidence(CAP99Seam.Gate.MIGRATION_COMPATIBILITY_RESTORE,
                CAP99Seam.EvidenceStatus.UNVERIFIED, null, ""));
        assertEquals(CAP99Seam.Decision.UNVERIFIED, CAP99ReferenceEvaluator.evaluate(rows).decision());
        rows.set(3, new CAP99Seam.GateEvidence(CAP99Seam.Gate.OWNERSHIP, CAP99Seam.EvidenceStatus.FAIL, null, "foreign owner"));
        assertEquals(CAP99Seam.Decision.BLOCKED, CAP99ReferenceEvaluator.evaluate(rows).decision());
    }
}
