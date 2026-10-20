package assignments.insurance;

import java.util.List;

/** One bounded page of immutable quote snapshots. */
public record QuoteHistoryPage(int page, int size, long totalElements,
                               List<QuoteSnapshot> quotes) {
    public QuoteHistoryPage {
        quotes = List.copyOf(quotes);
    }
}
