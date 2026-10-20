package assignments.insurance;

/** Learner-owned Day 2 boundary validation; does not perform selection or persistence. */
public final class QuoteSelectionRequestValidator {
    public void validate(QuoteSelectionRequest request) {
        // LEARNER TODO: reject a missing request, missing selections, null entries,
        // and null/empty/blank journeyId or quoteVersionId with IllegalArgumentException.
        // An empty selections list is valid. Do not normalize identifiers.
        throw new UnsupportedOperationException("Day 2 learner TODO: implement request validation");
    }
}
