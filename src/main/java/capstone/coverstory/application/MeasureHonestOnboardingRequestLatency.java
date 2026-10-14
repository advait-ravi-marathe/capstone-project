package capstone.coverstory.application;

import java.util.List;

/**
 * CAP-103 local synthetic latency summary seam. This class summarizes supplied
 * timer observations; it does not execute requests or make capacity/SLO claims.
 */
public final class MeasureHonestOnboardingRequestLatency {
    public enum Status { SUCCESS, ERROR }

    /** Timer values are monotonic elapsed-clock readings expressed in milliseconds. */
    public record MeasuredRequest(long startMillis, long endMillis, Status status) { }

    /** Percentiles describe successful requests only; error duration is separate. */
    public record LatencyReport(
            int totalRequests,
            int successCount,
            int errorCount,
            Long p50Millis,
            Long p95Millis,
            Long maxSuccessMillis,
            Long errorMaxMillis,
            List<String> diagnostics,
            String runLabel,
            boolean capacityClaim,
            boolean availabilityClaim) { }

    /**
     * Summarize one small local synthetic run using nearest-rank percentiles.
     * Invalid negative durations are diagnosed and excluded from statistics.
     */
    public LatencyReport summarizeLatency(List<MeasuredRequest> requests) {
        // LEARNER TODO: keep successful latency percentiles separate from errors,
        // use p_q = sorted[ceil(q*n)-1], report null for an empty success set,
        // and exclude end-before-start measurements with INVALID_DURATION diagnostics.
        // Label this only as a local synthetic run; never infer capacity or 30-day availability.
        throw new UnsupportedOperationException("CAP-103 learner TODO: implement honest local latency summary");
    }
}
