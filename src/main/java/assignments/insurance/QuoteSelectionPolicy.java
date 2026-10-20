package assignments.insurance;

import java.util.List;
import org.springframework.stereotype.Component;

/** Learner-owned domain policy for de-duplicating selected synthetic quotes. */
@Component
public final class QuoteSelectionPolicy implements QuoteSelectionService {
    @Override
    public List<QuoteSelectionKey> select(List<QuoteSelectionInput> selections) {
        return uniqueSelections(selections);
    }

    public List<QuoteSelectionKey> uniqueSelections(List<QuoteSelectionInput> input) {
        // LEARNER TODO: validate all entries, keep exact composite keys in first-seen order,
        // and do not mutate input. Invalid input must not produce partial output.
        throw new UnsupportedOperationException("Day 1 learner TODO: implement uniqueSelections");
    }
}
