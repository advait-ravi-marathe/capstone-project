package coverstory.modules.shortlist.refresh;

import coverstory.modules.shortlist.refresh.CheckpointTypes.Checkpoint;
import coverstory.modules.shortlist.refresh.CheckpointTypes.CatalogSnapshot;
import coverstory.modules.shortlist.refresh.CheckpointTypes.RefreshResult;
import java.util.Optional;
import java.util.UUID;

/** CAP-636 learner seam. All product/source reads go through the Catalog public port. */
public final class PreviewCheckpointRefresh {
    public interface OwnedCheckpointPort { Optional<Checkpoint> findOwned(UUID trustedOwner, UUID shortlistId); }
    public interface CatalogSnapshotPort { CatalogSnapshot readSnapshot(java.util.List<CheckpointTypes.Slot> requested); }
    public record Request(UUID bodyOwnerOverride, long expectedRevision) { }
    public record Reply(int status, String safeCode, RefreshResult preview) { }
    private final OwnedCheckpointPort shortlists;
    private final CatalogSnapshotPort catalog;
    private final ThreeWayCheckpointReconciler reconciler;
    public PreviewCheckpointRefresh(OwnedCheckpointPort shortlists, CatalogSnapshotPort catalog,
                                    ThreeWayCheckpointReconciler reconciler) {
        this.shortlists=shortlists; this.catalog=catalog; this.reconciler=reconciler;
    }
    public Reply preview(UUID trustedOwner, UUID shortlistId, Request request) {
        // TODO(CAP-636): authorize before private reads; capture one bounded
        // Catalog snapshot; build canonical server fingerprint without mutation.
        throw new UnsupportedOperationException("TODO(CAP-636): preview owned checkpoint refresh");
    }
}
