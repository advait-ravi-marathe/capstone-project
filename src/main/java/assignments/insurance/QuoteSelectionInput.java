package assignments.insurance;

/** Synthetic quote selection; journey IDs are identifiers, never authorization. */
public record QuoteSelectionInput(String journeyId, String quoteVersionId) { }
