package capstone.coverstory.shortlist.api;

import java.util.List;

/** CAP-168 bounded quote-review line classifier; authentication/review remain separate services. */
public final class CAP168Seam {
    public record QuoteReviewRecord(int sourceLine, String quoteId, String scenarioId, String sourceId,
                                    String sourceVersion, String termsHash, long totalPaise,
                                    String currency, boolean sourceIllustration) { }
    public record SafeRejection(int lineNumber, String code) { }
    public record Classification(List<QuoteReviewRecord> validRecords,
                                 List<SafeRejection> rejections,
                                 int blankLinesSkipped, boolean complete) {
        public Classification {
            validRecords = List.copyOf(validRecords);
            rejections = List.copyOf(rejections);
        }
    }

    /** Classify physical NDJSON lines and never include raw source lines in diagnostics. */
    public Classification classifyQuoteReviewLines(List<String> lines) {
        // LEARNER TODO: strict JSON/schema validation; preserve one-based line
        // numbers across blanks; require exact source/scenario/version/terms and
        // a complete integer INR total; reject illustrations and unknown fields.
        // Return safe line/code rejections only and do not silently accept a quote.
        throw new UnsupportedOperationException("CAP-168 learner TODO: classify quote-review lines");
    }
}
