package coverstory.modules.shortlist.api;

import java.util.List;
import org.springframework.stereotype.Component;

/** CAP-40 learner-owned seam; implement exact-pair deduplication in encounter order. */
@Component
public final class CAP40Seam implements SelectionOrderer {
    @Override
    public List<SelectionKey> uniqueSelections(List<SelectionInput> input) {
        // LEARNER TODO: validate the full input, deduplicate by both key fields,
        // preserve first-seen order, and never mutate the caller's list.
        throw new UnsupportedOperationException("CAP-40 learner TODO: implement uniqueSelections");
    }
}
