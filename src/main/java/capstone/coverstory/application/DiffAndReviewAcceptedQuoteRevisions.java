package capstone.coverstory.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Learner-owned CAP-109 seam. Values are references and digests; protected payload bytes never enter this view. */
public final class DiffAndReviewAcceptedQuoteRevisions {
    private DiffAndReviewAcceptedQuoteRevisions() { }

    public record Owner(UUID id) { }
    public record AuditObservation(long sequence, UUID eventId, String state, String providerReference,
                                   long postedAmountMinor, String currency) { }
    public record ReviewSnapshot(UUID applicationId, long revision, String sourceVersion,
                                 String protectedPayloadReference, Map<String, String> fieldDigests,
                                 long quotedTotalMinor, String currency, String termsDigest,
                                 List<AuditObservation> auditObservations, long authoritativeJourneyRevision,
                                 boolean requiredAuditPresent, boolean journeyStepPresent) { }
    public record FieldChange(String beforeDigest, String afterDigest) { }
    public record RevisionAuditDelta(Map<String, FieldChange> changed, List<String> added, List<String> removed,
                                     long amountDeltaMinor, boolean explicitReviewRequired,
                                     List<String> findings, long authoritativeJourneyRevision) { }

    public static RevisionAuditDelta diffAndCheckAudit(Owner owner, ReviewSnapshot original,
                                                       ReviewSnapshot current) {
        // TODO(CAP-109): compare source-linked revision metadata and validate append-only audit,
        // exact amount/terms review, and authoritative journey transition completeness.
        throw new UnsupportedOperationException("TODO(CAP-109): implement revision and audit reconciliation");
    }
}
