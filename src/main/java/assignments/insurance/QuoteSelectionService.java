package assignments.insurance;

import java.util.List;

/** Application boundary for applying a submitted selection list. */
@FunctionalInterface
public interface QuoteSelectionService {
    List<QuoteSelectionKey> select(List<QuoteSelectionInput> selections);
}
