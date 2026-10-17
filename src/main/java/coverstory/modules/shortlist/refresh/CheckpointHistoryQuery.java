package coverstory.modules.shortlist.refresh;

import coverstory.modules.shortlist.refresh.CheckpointTypes.Checkpoint;
import java.util.List;
import java.util.UUID;

/** CAP-642 learner seam: bounded 20-revision owner-scoped history + public Catalog batch. */
public final class CheckpointHistoryQuery {
    public record Page(List<Checkpoint> revisions, String nextCursor) {
        public Page { revisions=List.copyOf(revisions); }
    }
    public interface CatalogBatchPort { java.util.Map<String, CheckpointTypes.SourceLookup> lookupCurrent(List<CheckpointTypes.Slot> slots); }
    public Page page(UUID trustedOwner, UUID shortlistId, long beforeRevision, int limit) {
        // TODO(CAP-642): use deterministic indexed owner/head ordering, cap the
        // page at 20, and batch current public source refs (at most 3).
        throw new UnsupportedOperationException("TODO(CAP-642): query bounded checkpoint history");
    }
}
