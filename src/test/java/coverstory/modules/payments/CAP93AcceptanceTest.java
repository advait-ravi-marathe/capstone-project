package coverstory.modules.payments;

import coverstory.modules.payments.api.CAP93Seam;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Learner contract. The source has no actual integrated probe output, so the report stays blocked. */
@Tag("learnerAcceptance")
class CAP93AcceptanceTest {
    private CAP93Seam.ProbeReport report() {
        var jobs = List.of(
                new CAP93Seam.JobObservation("read-001", "COMPLETED", 0, 0),
                new CAP93Seam.JobObservation("read-002", "COMPLETED", 0, 0),
                new CAP93Seam.JobObservation("read-003", "COMPLETED", 0, 0));
        return new CAP93Seam().runOutageProbe(new CAP93Seam.ProbeInput(Duration.ofSeconds(30), jobs, true, false));
    }

    @Test void threeJobsWaitDuringOutageAndCompleteUniquelyAfterRecovery() {
        var r = report();
        assertEquals(3, r.pendingAtOutage()); assertEquals(0, r.completedAtOutage());
        assertEquals(0, r.pendingAfterRecovery()); assertEquals(3, r.uniqueCompleted());
        assertTrue(r.simulatedClock());
    }

    @Test void repeatedDeliveryHasNoDuplicateEffectOrNewPaymentAttempt() {
        var r = report(); assertTrue(r.duplicateDeliveryNoNewEffect());
    }

    @Test void scoreAddsAllFourDiagnosticCategoriesAndNeverClaimsAwsScore() {
        var r = report(); assertEquals(8, r.diagnosticNumerator()); assertEquals(12, r.diagnosticDenominator());
        assertNull(r.awsExamScore());
    }

    @Test void absentBenchmarkAndJobDescriptionLeaveFitUnresolved() {
        var r = report(); assertEquals("UNRESOLVED", r.capacityRoleFit());
    }

    @Test void missingActualProbeOutputRemainsBlockedAndNotRun() {
        var r = report(); assertEquals("BLOCKED_NOT_RUN", r.status()); assertFalse(r.actualProbeOutputPresent());
    }
}
