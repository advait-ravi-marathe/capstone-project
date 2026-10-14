package coverstory.modules.shortlist.api;

import java.util.List;

/** Shortlist-owned boundary for ordering public catalog references in a compare preview. */
public interface SelectionOrderer {
    List<SelectionKey> uniqueSelections(List<SelectionInput> input);
}
