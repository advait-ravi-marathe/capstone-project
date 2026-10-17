package coverstory.modules.shortlist.refresh;

import coverstory.modules.shortlist.refresh.CheckpointTypes.Conflict;
import coverstory.modules.shortlist.refresh.CheckpointTypes.Slot;
import java.util.List;

/** CAP-644 learner seam: bind explicit local order and conflict decisions by stable slot identity. */
public final class CheckpointOrderIntent {
    public record Decision(String slotId, String chosenVersionId) { }
    public List<Slot> apply(List<Slot> validBase, List<String> localSlotOrder,
                            List<Conflict> conflicts, List<Decision> decisions) {
        // TODO(CAP-644): validate exact permutation and resolve by stable slot,
        // never by array position or upstream map iteration order.
        throw new UnsupportedOperationException("TODO(CAP-644): preserve explicit selection order");
    }
}
