package capstone.coverstory.application;

import java.util.Map;
import java.util.UUID;

/** Learner-owned CAP-111 seam; submission and load-shedding effects are not implemented here. */
public final class DefendExplicitSubmissionAndCapacityBoundaries {
    private DefendExplicitSubmissionAndCapacityBoundaries() { }
    public record SubmitCommand(long expectedVersion, long reviewedRevision, UUID quoteId,
                                String configurationHash, String termsHash, boolean consent) { }
    public record SubmitResult(int status, String code, UUID intentId) { }
    public record CapacityAssumptions(long browsers, int browserWindowSeconds, double transactionFraction,
                                      int transactionWindowSeconds, double staticHitRatio) { }
    public record CapacityEstimate(double browserRps, double originRps, double transactionRps,
                                   boolean capacityProven, Map<String, String> caveats) { }

    public static SubmitResult submit(UUID applicationId, SubmitCommand command, String idempotencyKey) {
        // TODO(CAP-111): perform explicit owner-scoped submission against current review and payment state.
        throw new UnsupportedOperationException("TODO(CAP-111): implement explicit submission boundary");
    }

    public static CapacityEstimate estimateCapacity(CapacityAssumptions assumptions) {
        // TODO(CAP-111): derive assumption-labelled rates; do not describe estimates as benchmarks.
        throw new UnsupportedOperationException("TODO(CAP-111): implement capacity assumptions");
    }
}
