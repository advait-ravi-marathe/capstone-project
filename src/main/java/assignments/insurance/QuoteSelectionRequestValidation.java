package assignments.insurance;

/** Boundary contract for validating a parsed selection request before service use. */
@FunctionalInterface
public interface QuoteSelectionRequestValidation {
    void validate(QuoteSelectionRequest request);
}
