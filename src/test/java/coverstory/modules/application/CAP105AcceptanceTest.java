package coverstory.modules.application;

import coverstory.modules.application.api.CAP105Seam;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Learner-facing contract. These failures are expected until the TODO is implemented. */
@Tag("learnerAcceptance")
class CAP105AcceptanceTest {
    private static final List<CAP105Seam.Demonstration> THREE_PASSING = List.of(
            demo("CAP-95", CAP105Seam.EvidenceKind.LINKED_EXECUTION_OUTPUT),
            demo("CAP-102", CAP105Seam.EvidenceKind.LINKED_EXECUTION_OUTPUT),
            demo("CAP-104", CAP105Seam.EvidenceKind.LINKED_EXECUTION_OUTPUT));

    @Test void threeLinkedPassingDemonstrationsVerifyCheckpoint() {
        var result = new CAP105Seam().reviewEvidenceIndex(THREE_PASSING, List.of(), List.of(), true, true);
        assertEquals(3, result.findings().stream().filter(f -> f.status() == CAP105Seam.Status.VERIFIED).count());
        assertTrue(result.complete());
    }

    @Test void screenshotDoesNotVerifyLateSummaryDemonstration() {
        var demos = List.of(THREE_PASSING.get(0), THREE_PASSING.get(1),
                demo("CAP-104", CAP105Seam.EvidenceKind.SCREENSHOT_ONLY));
        var result = new CAP105Seam().reviewEvidenceIndex(demos, List.of(), List.of(), true, true);
        assertEquals(CAP105Seam.Status.UNVERIFIED, result.findings().get(2).status());
        assertFalse(result.complete());
    }

    @Test void allCriticalDefectsRemainVisibleWithOnlyTwoSelected() {
        var defects = List.of(new CAP105Seam.Defect("D1", "missing output", true),
                new CAP105Seam.Defect("D2", "stale commit", true),
                new CAP105Seam.Defect("D3", "scope mismatch", true),
                new CAP105Seam.Defect("D4", "documentation gap", false));
        var result = new CAP105Seam().reviewEvidenceIndex(THREE_PASSING, defects, List.of("D1", "D2"), true, true);
        assertEquals(List.of("D1", "D2", "D3", "D4"), result.allDefects().stream().map(CAP105Seam.Defect::id).toList());
        assertEquals(List.of("D3", "D4"), result.unscheduledDefectIds());
    }

    @Test void untrustedHarnessOrUnavailableServiceCannotInflateResults() {
        var untrusted = new CAP105Seam().reviewEvidenceIndex(THREE_PASSING, List.of(), List.of(), false, true);
        var unavailable = new CAP105Seam().reviewEvidenceIndex(THREE_PASSING, List.of(), List.of(), true, false);
        assertTrue(untrusted.findings().stream().allMatch(f -> f.status() == CAP105Seam.Status.BLOCKED));
        assertTrue(unavailable.findings().stream().allMatch(f -> f.status() == CAP105Seam.Status.NOT_RUN));
        assertEquals(0, untrusted.findings().stream().filter(f -> f.status() == CAP105Seam.Status.VERIFIED).count());
        assertFalse(untrusted.complete());
        assertFalse(unavailable.complete());
    }

    private static CAP105Seam.Demonstration demo(String id, CAP105Seam.EvidenceKind kind) {
        return new CAP105Seam.Demonstration(id, "bounded synthetic demonstration", "./mvnw test -Dtest=" + id,
                "0123456789abcdef0123456789abcdef01234567", kind, "synthetic://" + id + "/output",
                kind == CAP105Seam.EvidenceKind.SCREENSHOT_ONLY ? "screenshot" : "BUILD SUCCESS", true);
    }
}
