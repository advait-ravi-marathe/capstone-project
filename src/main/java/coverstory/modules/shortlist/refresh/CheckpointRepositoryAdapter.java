package coverstory.modules.shortlist.refresh;

import coverstory.modules.shortlist.refresh.CheckpointTypes.Checkpoint;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/** CAP-634 learner seam. Implement with Shortlist-owned tables and caller transaction. */
public final class CheckpointRepositoryAdapter {
    public enum AppendStatus { APPENDED, STALE_HEAD, IDENTITY_CONFLICT, NOT_OWNED, ROLLED_BACK }
    public record AppendResult(AppendStatus status, long currentRevision) { }
    public interface Transaction {
        Optional<Checkpoint> ownedHead(UUID ownerId, UUID shortlistId);
        boolean appendImmutableRevision(Checkpoint checkpoint, long expectedParentRevision);
        boolean advanceOwnedHead(UUID ownerId, UUID shortlistId, long expectedRevision, long nextRevision);
        void appendRequiredAuditAndJourney(UUID ownerId, Checkpoint checkpoint);
    }
    @FunctionalInterface public interface Transactions { <T> T inTransaction(Function<Transaction,T> work); }

    private final Transactions transactions;
    public CheckpointRepositoryAdapter(Transactions transactions) { this.transactions = transactions; }

    public AppendResult append(UUID trustedOwner, Checkpoint checkpoint, long expectedHead,
                               boolean injectFailureBeforeRequiredEffects) {
        // TODO(CAP-634): verify ownership and expected head; append immutable
        // history, CAS the head, and append required effects in one transaction.
        throw new UnsupportedOperationException("TODO(CAP-634): atomically append checkpoint revision");
    }
}
