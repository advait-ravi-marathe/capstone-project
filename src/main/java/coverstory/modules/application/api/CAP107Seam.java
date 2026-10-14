package coverstory.modules.application.api;

import java.util.List;

/** Learner seam for validating an immutable synthetic quote review manifest. */
public final class CAP107Seam {
    public enum Status { VALID_REVIEW_DESCRIPTOR, CONFLICT, UNSUPPORTED_SCHEMA, QUOTE_UNAVAILABLE, REQUOTE_REQUIRED, INVALID }
    public record ReviewManifest(String manifestId, String digest, int rowCount, int schemaVersion,
                                 String requiredTotalField, String requiredTaxField,
                                 String suppliedScenarioHash, String currentScenarioHash,
                                 String suppliedConfigurationHash, String currentConfigurationHash,
                                 boolean extractionConfirmed, String disclosure,
                                 String initialQuoteId, String revisedQuoteId) { }
    public record ExactBytes(byte[] value, String existingDigestForManifestId) {
        public ExactBytes { value = value == null ? null : value.clone(); }
        @Override public byte[] value() { return value == null ? null : value.clone(); }
    }
    public record SupportedSchema(int version, List<String> knownMoneyFields) {
        public SupportedSchema { knownMoneyFields = List.copyOf(knownMoneyFields); }
    }
    public record ManifestResult(Status status, String reviewDescriptor, boolean draftStored,
                                 boolean submitReady, List<String> blockers,
                                 boolean payableAdjustment, boolean automaticAcceptance) {
        public ManifestResult { blockers = List.copyOf(blockers); }
    }

    /**
     * Validate manifest identity and reviewability only. No acceptance, ingestion,
     * quote mutation or payment operation is performed by this seam.
     */
    public ManifestResult validateReviewManifest(ReviewManifest manifest, ExactBytes exactBytes,
                                                 SupportedSchema supportedSchema) {
        // LEARNER TODO (CAP-107): validate exact-byte digest/row count and immutable ID,
        // supported schema and required money fields, current scenario/configuration
        // identity, and extraction/disclosure blockers. Return a review descriptor only;
        // do not auto-accept or adjust payable amounts.
        throw new UnsupportedOperationException("CAP-107 learner TODO: validateReviewManifest");
    }
}
