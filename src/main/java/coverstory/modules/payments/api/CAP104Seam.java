package coverstory.modules.payments.api;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/** CAP-104 summary-only revision boundary for late synthetic payment observations. */
public final class CAP104Seam {
    public record Window(Instant startInclusive, Instant endExclusive, Duration grace) { }
    public record Summary(long count, long revision, Set<String> observedIds) {
        public Summary { observedIds = Set.copyOf(observedIds); }
    }
    public record Observation(String id, Instant eventTime, Instant arrivalTime,
                              boolean unknownPaymentEvidence) { }
    public enum Outcome { REVISED, DUPLICATE, TOO_LATE }
    public enum PaymentDisposition { NONE, RECONCILIATION_REQUIRED }
    public record Result(Summary summary, Outcome outcome, PaymentDisposition paymentDisposition,
                         long duplicateCount, long quarantinedCount,
                         int chargeCalls, int refundCalls, int dispatchCalls) { }

    /** Learner implementation must update only the analytics observation summary. */
    public Result reviseSummary(Window window, Summary current, Observation observation) {
        // LEARNER TODO: validate the observation, window and grace boundary; deduplicate
        // by stable observation ID; revise count/revision only for a unique event in the
        // closed grace interval. Arrival after the inclusive deadline is quarantined.
        // UNKNOWN payment evidence points to the existing reconciliation workflow.
        // This seam has no ports for charging, refunding or dispatching and must never
        // acquire or add them. Preserve the current summary for duplicates/quarantine.
        throw new UnsupportedOperationException("CAP-104 learner TODO: implement summary-only late revision");
    }
}
