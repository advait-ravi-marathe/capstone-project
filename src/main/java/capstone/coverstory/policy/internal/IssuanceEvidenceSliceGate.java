package capstone.coverstory.policy.internal;

import java.util.List;

/** Learner-owned, bounded Policy predicate and answerability-slice evaluator. */
public final class IssuanceEvidenceSliceGate {
    private IssuanceEvidenceSliceGate() { }

    public enum Label { ANSWER, ABSTAIN }
    public enum Status { ACTIVE_COVER, NO_COVER_YET }
    public enum Promotion { PROMOTE, HOLD, INVALID_DATASET }
    public record ScoredCase(String id, Label gold, Label predicted) { }
    public record SliceScore(int n, int correct, Double rate, List<String> errorIds) { }
    public record Evaluation(SliceScore answerable, SliceScore mustAbstain,
                             int total, int totalCorrect, Double overallRate, Promotion promotion) { }
    public record PolicyRecord(String insurerIssuanceReference, boolean effectiveCoverEvidence,
                               boolean paymentConfirmed, boolean offerAccepted) { }
    public record Projection(Status status, boolean maySayActiveCover, String reasonCode) { }

    public static Evaluation evaluate(List<ScoredCase> answerable, List<ScoredCase> mustAbstain,
                                      Evaluation baseline) {
        // TODO(CAP-176): preserve separate denominators/error IDs, reject missing labels,
        // and HOLD when must-abstain accuracy regresses, even if overall accuracy ties.
        throw new UnsupportedOperationException("TODO(CAP-176): implement slice evaluation and promotion gate");
    }

    public static Projection project(PolicyRecord record) {
        // TODO(CAP-176): simulated payment or offer acceptance never imply active cover;
        // require both a nonblank issuance reference and effective-cover evidence.
        throw new UnsupportedOperationException("TODO(CAP-176): implement issuance evidence predicate");
    }
}
