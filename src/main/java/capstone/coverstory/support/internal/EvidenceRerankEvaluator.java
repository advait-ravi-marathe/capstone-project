package capstone.coverstory.support.internal;

import java.util.List;

/** Learner seam for evaluating a precomputed ordering of already-authorized evidence. */
public class EvidenceRerankEvaluator {
    public enum Status { ACCEPTED, TIMEOUT_FALLBACK, INVALID_RERANK }

    public record Candidate(String id, boolean relevant) {}
    public record Evaluation(Status status, List<String> ids, int elapsedMillis,
                             boolean timeoutRecorded, int rerankCalls) {}

    /** TODO(CAP-122): enforce the deadline and candidate-subset invariant before returning a ranking. */
    public Evaluation evaluate(List<Candidate> authorized, List<String> proposed,
                               int simulatedElapsedMillis, int deadlineMillis) {
        throw new UnsupportedOperationException("TODO(CAP-122): bounded authorized rerank evaluation");
    }
}
