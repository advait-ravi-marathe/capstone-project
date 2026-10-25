package assignments.contracts;

import tools.jackson.databind.JsonNode;
import java.util.List;

/** Learner seam: compare an existing consumer contract to a proposed provider contract. */
public final class OpenApiCompatibilityAnalyzer {
    private OpenApiCompatibilityAnalyzer() { }

    /** Return independent, stable diagnostics; do not infer compatibility from a document's size. */
    public static CompatibilityReport compare(JsonNode consumer, JsonNode provider) {
        // TODO: inspect every consumed operation, request requirement, response status,
        // response-required field, and field type. Optional response additions are compatible.
        return new CompatibilityReport(false, List.of("TODO: compatibility analysis is not implemented"));
    }

    public record CompatibilityReport(boolean compatible, List<String> breakingChanges) {
        public CompatibilityReport { breakingChanges = List.copyOf(breakingChanges); }
    }
}
