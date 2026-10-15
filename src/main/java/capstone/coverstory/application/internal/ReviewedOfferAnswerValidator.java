package capstone.coverstory.application.internal;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Learner seam for validating a read-only explanation against immutable quote facts. */
public final class ReviewedOfferAnswerValidator {
    public enum AnswerStatus { SUPPORTED, INSUFFICIENT_EVIDENCE }
    public enum ValidationStatus { SUPPORTED, REJECTED, INSUFFICIENT_EVIDENCE }
    public record Offer(UUID ownerId, UUID offerId, String revision, String contentSha256,
                        long amountMinor, String currency, Set<String> termCodes, Instant expiresAt) {
        public Offer { termCodes=Set.copyOf(termCodes); }
    }
    public record OfferPair(Offer original, Offer revised) { }
    public record Citation(UUID ownerId, String sourceId, String version) { }
    public record Explanation(AnswerStatus status, UUID revisedOfferId, String revisedContentSha256,
                             Long claimedAmountDeltaMinor, Set<String> changedTermCodes, Instant expiry,
                             List<Citation> citations, Set<String> unknownFields, String reasonCode) {
        public Explanation { changedTermCodes=Set.copyOf(changedTermCodes);citations=List.copyOf(citations);unknownFields=Set.copyOf(unknownFields); }
    }
    public record Result(ValidationStatus status, String reasonCode, List<Citation> authorizedCitations) {
        public Result { authorizedCitations=List.copyOf(authorizedCitations); }
    }

    /** LEARNER TODO: reject malformed/foreign/stale/unsupported answers while preserving deterministic quote facts. */
    public Result validate(OfferPair authoritativePair, Explanation answer, Set<Citation> authorizedCitations) {
        throw new UnsupportedOperationException("CAP-115 learner TODO: validate read-only offer explanation");
    }
}
