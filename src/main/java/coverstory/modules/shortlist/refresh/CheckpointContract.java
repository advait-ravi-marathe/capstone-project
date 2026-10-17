package coverstory.modules.shortlist.refresh;

import coverstory.modules.shortlist.refresh.CheckpointTypes.Checkpoint;
import java.util.List;

/** CAP-632 learner seam: validate immutable owner-scoped checkpoint invariants. */
public final class CheckpointContract {
    public record Validation(boolean valid, Checkpoint checkpoint, List<String> violations) {
        public Validation { violations = List.copyOf(violations); }
    }

    public Validation validate(Checkpoint candidate) {
        // TODO(CAP-632): enforce 1–3 unique product/source-version references,
        // same scenario, stable unique slots, digests and required identities.
        // Return a typed invalid result; never return a partial checkpoint.
        throw new UnsupportedOperationException("TODO(CAP-632): validate checkpoint invariants");
    }
}
