package coverstory.modules.payments.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/** CAP-102 transport reconciliation boundary; observations are not payment effects. */
public final class CAP102Seam {
    public record Owner(UUID id) { }
    public record TupleCursor(Instant timestamp, String id) { }
    public record Observation(Owner owner, Instant timestamp, String id, String payloadHash) { }
    public record Rows(List<Observation> values) { public Rows { values = List.copyOf(values); } }
    public record CommittedBatch(List<Observation> emitted, TupleCursor cursor) {
        public CommittedBatch { emitted = List.copyOf(emitted); }
    }

    /** Payments-owned durable transaction. Implementations commit observations and cursor together. */
    @FunctionalInterface public interface ReconciliationPort {
        CommittedBatch inTransaction(Owner owner, Function<ReconciliationTransaction, CommittedBatch> work);
    }
    public interface ReconciliationTransaction {
        TupleCursor lockCursor(Owner owner);
        void appendObservation(Observation observation);
        void advanceCursor(Owner owner, TupleCursor expected, TupleCursor next);
    }

    private final ReconciliationPort port;
    public CAP102Seam(ReconciliationPort port) { this.port = port; }

    public CommittedBatch ingestAfter(Owner owner, TupleCursor cursor, Rows rows) {
        // LEARNER TODO: validate owner and complete ordering keys before persistence;
        // validate owner and complete ordering keys before persistence; select rows
        // exclusively after (timestamp,id), sort lexicographically by id for ties,
        // then write observations and advance the locked cursor in one unit of work.
        // A failed write must roll back both. Do not create or alter payment effects.
        throw new UnsupportedOperationException("CAP-102 learner TODO: implement atomic tuple reconciliation");
    }
}
