package coverstory.modules.shortlist.refresh;

import coverstory.modules.shortlist.refresh.CheckpointTypes.Branch;
import coverstory.modules.shortlist.refresh.CheckpointTypes.CatalogSnapshot;
import coverstory.modules.shortlist.refresh.CheckpointTypes.Checkpoint;
import coverstory.modules.shortlist.refresh.CheckpointTypes.RefreshResult;

/** CAP-633 learner seam: pure B/L/U reconciliation, preserving declared slot order. */
public final class ThreeWayCheckpointReconciler {
    public RefreshResult reconcile(Checkpoint base, Branch local, CatalogSnapshot upstream) {
        // TODO(CAP-633): implement the supplied per-slot equality truth table.
        // Keep UNAVAILABLE distinct from explicit removal and expose conflicts.
        throw new UnsupportedOperationException("TODO(CAP-633): reconcile branch and upstream");
    }
}
