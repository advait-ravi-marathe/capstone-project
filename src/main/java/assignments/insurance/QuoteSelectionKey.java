package assignments.insurance;

/** Exact composite identity for one selected quote revision. */
public record QuoteSelectionKey(String journeyId, String quoteVersionId) { }
