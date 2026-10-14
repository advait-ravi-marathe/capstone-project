package coverstory.modules.payments.api;

import java.time.Duration;
import java.util.List;

/** Learner-owned report seam for a bounded, local, synthetic outbox probe. */
public final class CAP93Seam {
    public record JobObservation(String jobId, String state, int businessEffects,
                                 int paymentAttempts) { }
    public record ProbeInput(Duration outage, List<JobObservation> jobs,
                             boolean recoveryObserved, boolean actualProbeOutputPresent) { }
    public record ProbeReport(String status, int pendingAtOutage, int completedAtOutage,
                              int pendingAfterRecovery, int uniqueCompleted,
                              boolean duplicateDeliveryNoNewEffect,
                              boolean actualProbeOutputPresent,
                              boolean simulatedClock, String capacityRoleFit,
                              Integer diagnosticNumerator, Integer diagnosticDenominator,
                              String awsExamScore, String reason) { }

    public ProbeReport runOutageProbe(ProbeInput input) {
        // LEARNER TODO: validate the bounded 0–30 s fixture, reconcile unique
        // read-only jobs after recovery, and report diagnostic counts without
        // making capacity, role-fit, AWS-score, or payment-authority claims.
        // Missing actual probe output must remain NOT_RUN/BLOCKED.
        throw new UnsupportedOperationException("CAP-93 learner TODO: implement bounded outage report");
    }
}
