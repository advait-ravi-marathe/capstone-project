package coverstory.modules.shortlist.refresh;

import java.util.List;

/** CAP-645 learner-authored candidate-bound admission runner; not a product endpoint. */
public final class CheckpointRefreshAdmissionRunner {
    public record Event(String kind, String expectedUi, String expectedApi, String expectedDurableHead) { }
    public record Admission(List<Event> observed, String result, String rawEvidencePath) {
        public Admission { observed=List.copyOf(observed); }
    }
    public Admission run(List<Event> schedule) {
        // TODO(CAP-645): execute the same schedule against UI, Spring API and
        // independent Postgres state; retain candidate-bound result/evidence.
        throw new UnsupportedOperationException("TODO(CAP-645): implement branch refresh admission run");
    }
}
