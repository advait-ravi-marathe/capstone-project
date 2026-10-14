package coverstory.modules.payments;

import coverstory.modules.payments.api.CAP78Seam;
import coverstory.modules.payments.api.CAP78Seam.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP78AcceptanceTest {
    private static final UUID A = id("78000000-0000-4000-8000-000000000001");
    private static final UUID B = id("78000000-0000-4000-8000-000000000002");
    private static final UUID J = id("78000000-0000-4000-8000-000000000101");
    private static final UUID APP = id("78000000-0000-4000-8000-000000000111");
    private static final UUID Q = id("78000000-0000-4000-8000-000000000201");
    private static final UUID V = id("78000000-0000-4000-8000-000000000401");
    private static final String TERMS = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    private static final String KEY = "cap78-synthetic-idempotency-key-A";
    private static final Instant NOW = Instant.parse("2026-11-20T10:01:00Z");
    private static final String MARKER = "CAP78-SYNTHETIC-PRIVATE-MARKER-DO-NOT-PERSIST";

    @Test void validOwnerAndReviewedVersionCreateOnePendingIntentAndBestEffortAnalytics() {
        FakePorts ports = new FakePorts();
        AtomicInteger analyticsCalls = new AtomicInteger();
        CAP78Seam seam = new CAP78Seam(ports, journey -> {
            analyticsCalls.incrementAndGet(); throw new IllegalStateException("analytics offline");
        });

        IntentResult result = seam.createMockPaymentIntent(A, J, 4, V, 100_000, TERMS, KEY);

        assertEquals(201, result.httpStatus()); assertEquals(Code.CREATED, result.code());
        assertEquals(1, ports.orders); assertEquals(1, ports.attempts); assertEquals(1, ports.outbox);
        assertEquals(1, ports.audit); assertEquals(1, ports.stepEvents);
        assertEquals(1, analyticsCalls.get());
        assertEquals("AWAITING_PAYMENT", ports.applicationStatus);
        assertEquals("PAYMENT_PENDING", ports.journey.currentStep()); assertEquals(5, ports.journey.version());
        assertEquals(0, ports.providerCalls, "checkout records an intent; it never invokes a provider");
        assertEquals(0, ports.decryptCalls, "quote review returns safe facts and does not decrypt consumer data");
    }

    @Test void retryAfterLostResponseReplaysSameOperationAndCountsRemainOne() {
        FakePorts ports = new FakePorts(); CAP78Seam seam = new CAP78Seam(ports, journey -> { });
        IntentResult first = seam.createMockPaymentIntent(A, J, 4, V, 100_000, TERMS, KEY);
        IntentResult replay = seam.createMockPaymentIntent(A, J, 4, V, 100_000, TERMS, KEY);
        assertEquals(200, replay.httpStatus()); assertEquals(Code.REPLAYED, replay.code());
        assertEquals(first.orderId(), replay.orderId()); assertEquals(first.attemptId(), replay.attemptId());
        assertEquals(first.operationId(), replay.operationId());
        assertEquals(1, ports.orders); assertEquals(1, ports.attempts); assertEquals(1, ports.outbox);
        assertEquals(1, ports.audit); assertEquals(1, ports.stepEvents);
    }

    @Test void everyInjectedWriteFailureRollsBackAllDomainAuditAndNavigationEvidence() {
        for (int failAfter = 1; failAfter <= 7; failAfter++) {
            FakePorts ports = new FakePorts(); ports.failAfter = failAfter;
            CAP78Seam seam = new CAP78Seam(ports, journey -> { });
            try {
                seam.createMockPaymentIntent(A, J, 4, V, 100_000, TERMS, KEY);
                fail("injected write failure must escape at write " + failAfter);
            } catch (UnsupportedOperationException todo) {
                if (todo.getMessage() != null && todo.getMessage().contains("CAP-78 learner TODO")) throw todo;
                throw todo;
            } catch (IllegalStateException injectedWriteFailure) {
                assertEquals("injected write failure", injectedWriteFailure.getMessage());
            }
            assertEquals(0, ports.orders); assertEquals(0, ports.attempts); assertEquals(0, ports.outbox);
            assertEquals(0, ports.audit); assertEquals(0, ports.stepEvents);
            assertEquals("DRAFT", ports.applicationStatus); assertEquals("QUOTE_REVIEW", ports.journey.currentStep());
            assertEquals(4, ports.journey.version());
        }
    }

    @Test void changedAmountOrTermsWithSameKeyConflictsAndWithdrawnOrStaleQuotesReject() {
        FakePorts ports = new FakePorts(); CAP78Seam seam = new CAP78Seam(ports, journey -> { });
        IntentResult first = seam.createMockPaymentIntent(A, J, 4, V, 100_000, TERMS, KEY);
        assertEquals(Code.CREATED, first.code());
        assertEquals(Code.IDEMPOTENCY_CONFLICT,
                seam.createMockPaymentIntent(A, J, 4, V, 100_001, TERMS, KEY).code());
        assertEquals(Code.IDEMPOTENCY_CONFLICT,
                seam.createMockPaymentIntent(A, J, 4, V, 100_000, "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc", KEY).code());

        FakePorts withdrawn = new FakePorts(); withdrawn.validation = withdrawn.validationWithSource("WITHDRAWN");
        assertEquals(Code.REVIEW_REQUIRED,
                new CAP78Seam(withdrawn, journey -> { }).createMockPaymentIntent(A, J, 4, V, 100_000, TERMS, "key-w").code());
        FakePorts stale = new FakePorts();
        assertEquals(Code.STALE_JOURNEY,
                new CAP78Seam(stale, journey -> { }).createMockPaymentIntent(A, J, 3, V, 100_000, TERMS, "key-s").code());
        assertEquals(0, withdrawn.orders); assertEquals(0, stale.orders);
    }

    @Test void foreignOwnerValidationAndTelemetryFailureCannotDecryptOrInvokeProvider() {
        FakePorts ports = new FakePorts();
        CAP78Seam seam = new CAP78Seam(ports, journey -> { throw new IllegalStateException("offline"); });
        IntentResult foreign = seam.createMockPaymentIntent(B, J, 4, V, 100_000, TERMS, KEY);
        assertEquals(404, foreign.httpStatus()); assertEquals(Code.NOT_FOUND, foreign.code());
        assertEquals(0, ports.validationReads); assertEquals(0, ports.decryptCalls); assertEquals(0, ports.providerCalls);
        IntentResult anonymous = seam.createMockPaymentIntent(null, J, 4, V, 100_000, TERMS, KEY);
        assertEquals(401, anonymous.httpStatus()); assertEquals(Code.UNAUTHORIZED, anonymous.code());
        assertEquals(0, ports.decryptCalls); assertEquals(0, ports.providerCalls);
    }

    @Test void noSyntheticPrivateMarkerAppearsInResultOrRecordedBusinessValues() {
        FakePorts ports = new FakePorts(); CAP78Seam seam = new CAP78Seam(ports, journey -> { });
        IntentResult result = seam.createMockPaymentIntent(A, J, 4, V, 100_000, TERMS, KEY);
        assertFalse(result.toString().contains(MARKER));
        assertFalse(ports.storedValues().contains(MARKER));
        assertFalse(ports.storedValues().contains(KEY), "only a one-way idempotency key hash is persisted");
    }

    private static final class FakePorts implements UnitOfWork, PurchasePorts {
        private QuoteValidation validation = new QuoteValidation(V, A, J, Q, 2, 100_000, "INR", TERMS,
                "ACTIVE", NOW.plusSeconds(240), true);
        private JourneySnapshot journey = new JourneySnapshot(J, A, 4, "QUOTE_REVIEW", APP, 4, Q);
        private final Map<String, IdempotentOperation> operations = new HashMap<>();
        private int orders, attempts, outbox, audit, stepEvents, providerCalls, decryptCalls, validationReads;
        private int writes, failAfter;
        private String applicationStatus = "DRAFT";

        @Override public <T> T inTransaction(Function<PurchasePorts, T> work) {
            Snapshot before = snapshot();
            try { return work.apply(this); }
            catch (RuntimeException failure) { restore(before); throw failure; }
        }
        private record Snapshot(int orders, int attempts, int outbox, int audit, int steps, int writes,
                               String appStatus, JourneySnapshot journey, Map<String, IdempotentOperation> operations) { }
        private Snapshot snapshot() { return new Snapshot(orders, attempts, outbox, audit, stepEvents, writes,
                applicationStatus, journey, new HashMap<>(operations)); }
        private void restore(Snapshot s) { orders=s.orders; attempts=s.attempts; outbox=s.outbox; audit=s.audit;
            stepEvents=s.steps; writes=s.writes; applicationStatus=s.appStatus; journey=s.journey;
            operations.clear(); operations.putAll(s.operations); }
        private void write() { writes++; if (failAfter == writes) throw new IllegalStateException("injected write failure"); }
        @Override public String serverRequestId() { return "req-cap78-synth-001"; }
        @Override public Optional<QuoteValidation> lockAcceptedValidation(UUID owner, UUID validationId) {
            validationReads++;
            return owner != null && owner.equals(validation.ownerId()) && validationId.equals(validation.validationId())
                    ? Optional.of(validation) : Optional.empty();
        }
        @Override public Optional<JourneySnapshot> lockOwned(UUID owner, UUID journeyId) {
            return owner != null && owner.equals(journey.ownerId()) && journeyId.equals(journey.journeyId())
                    ? Optional.of(journey) : Optional.empty();
        }
        @Override public boolean transitionToAwaitingPayment(UUID owner, UUID appId, long revision, UUID operationId) {
            if (!A.equals(owner) || !APP.equals(appId) || revision != 4 || !"DRAFT".equals(applicationStatus)) return false;
            applicationStatus="AWAITING_PAYMENT"; write(); return true;
        }
        @Override public boolean advance(JourneyAdvance value) {
            if (value.expectedVersion() != journey.version()) return false;
            journey = new JourneySnapshot(journey.journeyId(), journey.ownerId(), value.newVersion(),
                    value.toStep(), journey.applicationId(), journey.applicationRevision(), journey.quoteId());
            write(); return true;
        }
        @Override public void appendStepEvent(JourneyAdvance value) { stepEvents++; write(); }
        @Override public Optional<IdempotentOperation> findOperation(UUID owner, byte[] keyHash) {
            return Optional.ofNullable(operations.get(owner + ":" + HexFormat.of().formatHex(keyHash)));
        }
        @Override public void createMockOrder(PurchaseIntent intent) {
            orders++;
            byte[] keyHash = HexFormat.of().parseHex(intent.idempotencyKeyHash());
            byte[] requestHash = HexFormat.of().parseHex(intent.requestHash());
            operations.put(intent.ownerId() + ":" + intent.idempotencyKeyHash(),
                    new IdempotentOperation(keyHash, requestHash, intent.orderId(), intent.attemptId(), intent.operationId(), "CREATED"));
            write();
        }
        @Override public void createPendingAttempt(PurchaseIntent intent) { attempts++; write(); }
        @Override public void appendOutboxIntent(UUID operationId, UUID attemptId, String requestId) { outbox++; write(); }
        @Override public void append(AuditRecord event) { audit++; write(); }
        public QuoteValidation validationWithSource(String source) {
            return new QuoteValidation(validation.validationId(), validation.ownerId(), validation.journeyId(),
                    validation.quoteId(), validation.quoteRevision(), validation.totalMinor(), validation.currency(),
                    validation.termsHash(), source, validation.expiresAt(), validation.accepted());
        }
        private String storedValues() { return applicationStatus + journey + operations.keySet(); }
    }

    private static UUID id(String value) { return UUID.fromString(value); }
}
