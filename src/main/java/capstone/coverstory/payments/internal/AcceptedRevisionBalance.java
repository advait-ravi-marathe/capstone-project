package capstone.coverstory.payments.internal;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** Learner seam for exact accepted-revision reconciliation; no provider side effects live here. */
public final class AcceptedRevisionBalance {
    public Reconciliation reconcile(AcceptedOffer acceptedOffer, List<ConfirmedEffect> effects,
                                    List<PendingAllocation> pendingAllocations) {
        throw new UnsupportedOperationException("CAP-116 learner TODO: reconcile accepted revision balance");
    }

    public record AcceptedOffer(UUID ownerId, UUID applicationId, UUID offerId, String revision,
                                String contentSha256, long totalMinor, String currency, boolean accepted) {
        public AcceptedOffer {
            Objects.requireNonNull(ownerId); Objects.requireNonNull(applicationId); Objects.requireNonNull(offerId);
            Objects.requireNonNull(revision); Objects.requireNonNull(contentSha256); Objects.requireNonNull(currency);
        }
    }

    public record ConfirmedEffect(UUID effectId, UUID ownerId, UUID applicationId, String providerReference,
                                  EffectKind kind, long amountMinor, String currency, UUID offerId,
                                  String revision, String contentSha256, boolean attachedToAuthoritativeEffect) {
        public ConfirmedEffect {
            Objects.requireNonNull(ownerId); Objects.requireNonNull(applicationId);
            Objects.requireNonNull(providerReference); Objects.requireNonNull(kind); Objects.requireNonNull(currency);
            Objects.requireNonNull(offerId); Objects.requireNonNull(revision); Objects.requireNonNull(contentSha256);
        }
    }

    public record PendingAllocation(UUID allocationId, UUID ownerId, UUID applicationId, String idempotencyKey,
                                    UUID offerId, String revision, String contentSha256, AllocationKind kind,
                                    AllocationStatus status, long amountMinor, String currency) {
        public PendingAllocation {
            Objects.requireNonNull(allocationId); Objects.requireNonNull(ownerId); Objects.requireNonNull(applicationId);
            Objects.requireNonNull(idempotencyKey); Objects.requireNonNull(offerId); Objects.requireNonNull(revision);
            Objects.requireNonNull(contentSha256); Objects.requireNonNull(kind); Objects.requireNonNull(status); Objects.requireNonNull(currency);
        }
    }

    public record Reconciliation(UUID acceptedOfferId, String acceptedRevision, String acceptedContentSha256,
                                 long confirmedNetMinor, long pendingAdditionalMinor, long pendingRefundMinor,
                                 long adjustmentMinor, String currency, Set<String> discrepancyCodes,
                                 NextAction nextAction) {
        public Reconciliation { discrepancyCodes = Set.copyOf(discrepancyCodes); }
    }

    public enum EffectKind { CAPTURE, REFUND }
    public enum AllocationKind { ADDITIONAL_PAYMENT, REFUND }
    public enum AllocationStatus { UNSTARTED, IN_FLIGHT, UNKNOWN, CONFIRMED, CANCELLED }
    public enum NextAction { NO_ADJUSTMENT, CREATE_ADDITIONAL_PAYMENT, CREATE_REFUND, WAIT_OR_RECONCILE, REVIEW_DISCREPANCY }
}
