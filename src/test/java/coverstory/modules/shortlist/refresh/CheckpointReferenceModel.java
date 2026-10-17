package coverstory.modules.shortlist.refresh;

import java.util.List;

/** CAP-643 learner-authored independent state model; deliberately has no production helper imports. */
public final class CheckpointReferenceModel {
    public record Command(String kind, String branch, String operationKey, String requestHash) { }
    public record State(long headRevision, List<String> immutableRevisionIds, List<String> outcomes) {
        public State { immutableRevisionIds=List.copyOf(immutableRevisionIds); outcomes=List.copyOf(outcomes); }
    }
    public State apply(State state, Command command) {
        // TODO(CAP-643): implement the independent bounded reference rules
        // before comparing the real Shortlist application service.
        throw new UnsupportedOperationException("TODO(CAP-643): write the checkpoint replay oracle");
    }
}
