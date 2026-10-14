package coverstory.modules.payments.api;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/** CAP-78 mock-checkout boundary. All persistence remains behind owning-module ports. */
public final class CAP78Seam {
    public enum Code { CREATED, REPLAYED, UNAUTHORIZED, NOT_FOUND, STALE_JOURNEY,
        REVIEW_REQUIRED, IDEMPOTENCY_CONFLICT }

    public record QuoteValidation(UUID validationId, UUID ownerId, UUID journeyId,
                                  UUID quoteId, long quoteRevision, long totalMinor,
                                  String currency, String termsHash, String sourceState,
                                  Instant expiresAt, boolean accepted) { }

    public record JourneySnapshot(UUID journeyId, UUID ownerId, long version,
                                  String currentStep, UUID applicationId,
                                  long applicationRevision, UUID quoteId) { }

    public record IdempotentOperation(byte[] keyHash, byte[] requestHash,
                                      UUID orderId, UUID attemptId, UUID operationId,
                                      String resultCode) {
        public IdempotentOperation {
            keyHash = keyHash.clone(); requestHash = requestHash.clone();
        }
        @Override public byte[] keyHash() { return keyHash.clone(); }
        @Override public byte[] requestHash() { return requestHash.clone(); }
    }

    public record PurchaseIntent(UUID orderId, UUID attemptId, UUID operationId,
                                 UUID ownerId, UUID journeyId, UUID applicationId,
                                 UUID quoteId, long applicationRevision,
                                 long amountMinor, String currency, String termsHash,
                                 String idempotencyKeyHash, String requestHash) { }

    public record JourneyAdvance(UUID journeyId, long expectedVersion, long newVersion,
                                 String fromStep, String toStep, UUID operationId,
                                 String requestId) { }

    public record AuditRecord(UUID ownerId, UUID operationId, UUID journeyId,
                              UUID applicationId, UUID attemptId, String eventCode,
                              String priorState, String newState, long amountMinor,
                              String currency, String requestId, Instant occurredAt) { }

    public record IntentResult(int httpStatus, Code code, UUID orderId,
                               UUID attemptId, UUID operationId, long journeyVersion,
                               boolean replay) { }

    /** Quote review owns validation and approved-source state; checkout receives safe facts only. */
    public interface QuoteReviewPort {
        Optional<QuoteValidation> lockAcceptedValidation(UUID authenticatedOwnerId, UUID validationId);
    }

    /** Application owns its status transition and application revision. */
    public interface ApplicationCheckoutPort {
        boolean transitionToAwaitingPayment(UUID authenticatedOwnerId, UUID applicationId,
                                            long expectedRevision, UUID operationId);
    }

    /** The Application module owns journey projections and append-only step events. */
    public interface JourneyCheckoutPort {
        Optional<JourneySnapshot> lockOwned(UUID authenticatedOwnerId, UUID journeyId);
        boolean advance(JourneyAdvance advance);
        void appendStepEvent(JourneyAdvance advance);
    }

    /** Payments owns idempotency, immutable order/attempt records, and durable outbox work. */
    public interface PaymentCheckoutPort {
        Optional<IdempotentOperation> findOperation(UUID authenticatedOwnerId, byte[] keyHash);
        void createMockOrder(PurchaseIntent intent);
        void createPendingAttempt(PurchaseIntent intent);
        void appendOutboxIntent(UUID operationId, UUID attemptId, String requestId);
    }

    /** Business audit is durable and participates in this same SQL unit of work. */
    public interface TransactionAuditPort { void append(AuditRecord event); }

    /** One PostgreSQL transaction adapter delegates each operation to its owning public port. */
    public interface PurchasePorts extends QuoteReviewPort, ApplicationCheckoutPort,
            JourneyCheckoutPort, PaymentCheckoutPort, TransactionAuditPort {
        String serverRequestId();
    }

    @FunctionalInterface
    public interface UnitOfWork { <T> T inTransaction(Function<PurchasePorts, T> work); }

    /** Best effort only; must be invoked after commit and must not affect the purchase result. */
    @FunctionalInterface
    public interface CheckoutAnalyticsPort { void recordAllowlistedCheckoutStarted(UUID journeyId); }

    private final UnitOfWork unitOfWork;
    private final CheckoutAnalyticsPort analytics;

    public CAP78Seam(UnitOfWork unitOfWork, CheckoutAnalyticsPort analytics) {
        this.unitOfWork = unitOfWork;
        this.analytics = analytics;
    }

    public IntentResult createMockPaymentIntent(UUID owner, UUID journeyId,
            long expectedJourneyVersion, UUID validationId, long acceptedTotalMinor,
            String termsHash, String idempotencyKey) {
        // LEARNER TODO: inside the supplied unit of work, authorize owner before
        // sensitive reads, revalidate the accepted quote/version/source/amount,
        // enforce durable idempotency, and append all domain/audit/journey/outbox
        // writes atomically. No provider call or decryption belongs here.
        throw new UnsupportedOperationException("CAP-78 learner TODO: implement mock purchase transaction");
    }
}
