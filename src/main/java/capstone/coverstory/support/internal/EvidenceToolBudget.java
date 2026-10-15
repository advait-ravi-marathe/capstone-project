package capstone.coverstory.support.internal;

import java.util.List;

/** Learner seam for bounding read-only cancellation-help evidence investigation. */
public final class EvidenceToolBudget {
    public enum Stop { STEP_LIMIT, REPEATED_CALL, DEADLINE, RECONCILIATION_REQUIRED, EXHAUSTED }
    public enum Dispatch { CONFIRMED, NOT_DISPATCHED, UNKNOWN }
    public record Proposal(String tool, String ownerScope, String journeyId, String query) { }
    public record ReadResult(String evidence, int elapsedMillis, Dispatch dispatch) { }
    public record TraceEntry(Proposal proposal, ReadResult result) { }
    public record Investigation(List<TraceEntry> trace, Stop stop, String answer) {
        public Investigation { trace = List.copyOf(trace); }
    }

    /** TODO(CAP-126): stop before duplicate reads, at three calls/deadline, and on unresolved dispatch. */
    public Investigation investigate(List<Proposal> proposals, ReadOnlyEvidencePort port, int deadlineMillis) {
        throw new UnsupportedOperationException("TODO(CAP-126): bounded read-only cancellation-help investigation");
    }

    /** Only synthetic, owning-module read adapters belong behind this port. */
    public interface ReadOnlyEvidencePort {
        ReadResult read(Proposal proposal);
    }
}
