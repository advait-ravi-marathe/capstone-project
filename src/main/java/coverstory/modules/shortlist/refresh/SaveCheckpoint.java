package coverstory.modules.shortlist.refresh;

import coverstory.modules.shortlist.refresh.CheckpointTypes.Checkpoint;
import java.util.Optional;
import java.util.UUID;

/** CAP-635 learner seam: expected-revision save and scoped durable operation replay. */
public final class SaveCheckpoint {
    public record Operation(UUID ownerId, UUID shortlistId, String operationKind,
                            String idempotencyKey, String canonicalRequestSha256) { }
    public record SaveResult(CheckpointTypes.Outcome outcome, long revision, String safeCode) { }
    public record StoredOperation(String canonicalRequestSha256, SaveResult result) { }
    public interface Port {
        Optional<StoredOperation> findOperation(Operation operation);
        SaveResult save(Operation operation, long expectedRevision, Checkpoint checkpoint);
    }
    private final Port port;
    public SaveCheckpoint(Port port) { this.port = port; }
    public SaveResult execute(UUID trustedOwner, Checkpoint candidate, long expectedRevision, Operation operation) {
        // TODO(CAP-635): bind owner/resource/kind and canonical request content,
        // preserve drafts on stale conflict, and replay only identical operations.
        throw new UnsupportedOperationException("TODO(CAP-635): fence concurrent checkpoint saves");
    }
}
