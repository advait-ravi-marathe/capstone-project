package coverstory.modules.payments.api;

import java.util.UUID;

/** Owner-scoped read seam for synthetic callback-batch reconciliation. */
public final class CAP85Seam {
    public record PaymentBatchSummary(long rawCount, long invalidCount, long duplicateCount,
                                      long insertedCount, long unaccountedCount,
                                      long missingInsertedEffectCount) { }

    @FunctionalInterface
    public interface SummaryQuery {
        PaymentBatchSummary select(UUID authenticatedOwnerId, UUID batchId);
    }

    private final SummaryQuery query;

    public CAP85Seam(SummaryQuery query) { this.query = query; }

    public PaymentBatchSummary summarize(UUID authenticatedOwnerId, UUID batchId) {
        // LEARNER TODO: execute one parameterized owner+batch scoped aggregate
        // SELECT. Count every staged row, detect missing classifications, and
        // link INSERTED rows to durable effects by exact owner and lineage.
        // Return one zero-valued row for an empty owned batch. Never repair rows,
        // dispatch a provider call, or infer confirmed money from these counts.
        throw new UnsupportedOperationException("CAP-85 learner TODO: implement owned batch summary query");
    }
}
