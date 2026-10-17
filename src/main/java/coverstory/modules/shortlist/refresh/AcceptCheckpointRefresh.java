package coverstory.modules.shortlist.refresh;

import coverstory.modules.shortlist.refresh.CheckpointTypes.RefreshResult;
import java.util.Map;
import java.util.UUID;

/** CAP-637 learner seam: reauthorize, revalidate and accept only explicit current resolutions. */
public final class AcceptCheckpointRefresh {
    public record Request(long expectedHead, long expectedCatalogGeneration, String expectedFingerprint,
                          Map<String, String> explicitChoices, String operationKey) { }
    public record Reply(int status, String safeCode, long currentRevision) { }
    @FunctionalInterface public interface CurrentPreview { RefreshResult recompute(UUID owner, UUID shortlist, Request request); }
    private final CurrentPreview preview;
    private final SaveCheckpoint saves;
    public AcceptCheckpointRefresh(CurrentPreview preview, SaveCheckpoint saves) { this.preview=preview; this.saves=saves; }
    public Reply accept(UUID trustedOwner, UUID shortlistId, Request request) {
        // TODO(CAP-637): reauthorize and recompute inside the established
        // transaction/lock order; reject stale/forged choices atomically.
        throw new UnsupportedOperationException("TODO(CAP-637): atomically accept refresh");
    }
}
