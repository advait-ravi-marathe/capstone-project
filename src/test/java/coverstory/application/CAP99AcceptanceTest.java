package coverstory.application;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Learner contract. Failures at the explicit TODO are expected before assignment. */
@Tag("learnerAcceptance")
class CAP99AcceptanceTest {
    private final CAP99Seam seam = new CAP99Seam();
    private static final String COMMIT = "0123456789abcdef0123456789abcdef01234567";
    private static final String HASH = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
    private List<CAP99Seam.GateEvidence> sixGreen() {
        var rows = new ArrayList<CAP99Seam.GateEvidence>();
        for (var gate : CAP99Seam.Gate.values()) rows.add(new CAP99Seam.GateEvidence(gate,
                CAP99Seam.EvidenceStatus.VERIFIED,
                new CAP99Seam.Receipt(CAP99Seam.EvidenceKind.TEST_OUTPUT, "./mvnw -q verify",
                        "PASS synthetic gate assertion", COMMIT, HASH), ""));
        return rows;
    }
    @Test void sixVerifiedGatesYieldReviewOnly() {
        var result = seam.evaluateReadiness(sixGreen());
        assertEquals(CAP99Seam.Decision.READY_FOR_REVIEW, result.decision());
        assertFalse(result.findings().toString().contains("RELEASE_APPROVED"));
    }
    @Test void missingMigrationNamesCAP98() {
        var rows = sixGreen(); rows.removeIf(e -> e.gate() == CAP99Seam.Gate.MIGRATION_COMPATIBILITY_RESTORE);
        var result = seam.evaluateReadiness(rows);
        assertEquals(CAP99Seam.Decision.BLOCKED, result.decision());
        assertTrue(result.findings().stream().anyMatch(s -> s.contains("CAP-98")));
    }
    @Test void crossOwnerFailureCannotBeAveragedAway() {
        var rows = sixGreen();
        rows.replaceAll(e -> e.gate() == CAP99Seam.Gate.OWNERSHIP
                ? new CAP99Seam.GateEvidence(e.gate(), CAP99Seam.EvidenceStatus.FAIL, e.receipt(), "owner B read owner A") : e);
        var result = seam.evaluateReadiness(rows);
        assertEquals(CAP99Seam.Decision.BLOCKED, result.decision());
        assertTrue(result.findings().stream().anyMatch(s -> s.contains("cross-owner failure")));
    }
    @Test void plannedPathsAndScreenshotAreNotTestEvidence() {
        var rows = sixGreen();
        rows.set(0, new CAP99Seam.GateEvidence(CAP99Seam.Gate.ARTIFACT, CAP99Seam.EvidenceStatus.VERIFIED,
                new CAP99Seam.Receipt(CAP99Seam.EvidenceKind.SCREENSHOT, "", "", "", ""), ""));
        var result = seam.evaluateReadiness(rows);
        assertEquals(CAP99Seam.Decision.UNVERIFIED, result.decision());
        rows.set(0, new CAP99Seam.GateEvidence(CAP99Seam.Gate.ARTIFACT, CAP99Seam.EvidenceStatus.VERIFIED,
                new CAP99Seam.Receipt(CAP99Seam.EvidenceKind.PLANNED_PATH, "", "", "", ""), ""));
        assertEquals(CAP99Seam.Decision.UNVERIFIED, seam.evaluateReadiness(rows).decision());
    }
}
