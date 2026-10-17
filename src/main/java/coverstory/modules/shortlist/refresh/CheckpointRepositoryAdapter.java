package coverstory.modules.shortlist.refresh;

import coverstory.modules.shortlist.refresh.CheckpointTypes.Checkpoint;
import java.util.UUID;

/** CAP-634 learner seam. Implement with Shortlist-owned tables and caller transaction. */
public final class CheckpointRepositoryAdapter {
    public enum AppendStatus { APPENDED, STALE_HEAD, IDENTITY_CONFLICT, NOT_OWNED }
    public record AppendResult(AppendStatus status, long currentRevision) { }
    @FunctionalInterface public interface RequiredEffects {
        void appendAuditAndJourney(UUID ownerId, Checkpoint checkpoint);
    }

    private final RequiredEffects requiredEffects;
    public CheckpointRepositoryAdapter(RequiredEffects requiredEffects) { this.requiredEffects = requiredEffects; }

    public AppendResult append(UUID trustedOwner, Checkpoint checkpoint, long expectedHead,
                               boolean injectFailureBeforeRequiredEffects) {
        // TODO(CAP-634): append immutable history, advance the owned head and
        // invoke required audit/journey effects in the same database transaction.
        throw new UnsupportedOperationException("TODO(CAP-634): atomically append checkpoint revision");
    }
}
