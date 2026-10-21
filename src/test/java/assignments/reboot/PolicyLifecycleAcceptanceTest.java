package assignments.reboot;

import assignments.insurance.*;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/** Fixed learner-behavior tests. Red at an explicit Day 31–44 TODO is intentional. */
@Tag("learnerAcceptance")
class PolicyLifecycleAcceptanceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-19T12:00:00Z"), ZoneOffset.UTC);
    private final PolicyLifecycleService service = new PolicyLifecycleService(CLOCK);

    @Test void day31RejectsForeignUnselectedStaleOrIneligibleQuoteWithoutIssue() {
        var valid = issueRequest();
        var selected = new PolicyIssueFacts("owner-a", "owner-a", "journey-a", "quote-a", true, true, LocalDate.parse("2026-12-01"), true);
        assertAll("quote facts control eligibility independently",
                () -> assertTrue(service.validateIssue(valid, selected)),
                () -> assertFalse(service.validateIssue(valid, new PolicyIssueFacts("owner-b", "owner-a", "journey-a", "quote-a", true, true, LocalDate.parse("2026-12-01"), true)), "authenticated principal must own the quote"),
                () -> assertFalse(service.validateIssue(valid, new PolicyIssueFacts("owner-a", "owner-a", "journey-a", "quote-a", false, true, LocalDate.parse("2026-12-01"), true)), "unselected quote must be rejected"),
                () -> assertFalse(service.validateIssue(valid, new PolicyIssueFacts("owner-a", "owner-a", "journey-a", "quote-a", true, true, LocalDate.parse("2026-11-18"), true)), "expired quote must be rejected against injected clock"),
                () -> assertFalse(service.validateIssue(valid, new PolicyIssueFacts("owner-a", "owner-a", "journey-a", "quote-a", true, false, LocalDate.parse("2026-12-01"), true)), "ineligible quote must be rejected"),
                () -> assertFalse(service.validateIssue(valid, new PolicyIssueFacts("owner-a", "owner-a", "journey-other", "quote-a", true, true, LocalDate.parse("2026-12-01"), true)), "quote journey must match request"),
                () -> assertFalse(service.validateIssue(valid, new PolicyIssueFacts("owner-a", "owner-a", "journey-a", "quote-other", true, true, LocalDate.parse("2026-12-01"), true)), "quote version must match request"),
                () -> assertFalse(service.validateIssue(valid, new PolicyIssueFacts("owner-a", null, "journey-a", "quote-a", true, true, LocalDate.parse("2026-12-01"), false)), "missing quote must be rejected"));
    }

    @Test void day32IdenticalIdempotencyReplayReturnsOriginalAndChangedPayloadConflicts() {
        var request = issueRequest();
        var created = service.issue(request, "owner-a");
        assertEquals(issued(), created);
        assertEquals(created, service.issue(request, "owner-a"), "identical retry returns complete original snapshot");
        for (var changed : List.of(
                new PolicyIssueRequest("journey-a", "quote-other", "req-a", request.effectiveDate()),
                new PolicyIssueRequest("journey-other", "quote-a", "req-a", request.effectiveDate()),
                new PolicyIssueRequest("journey-a", "quote-a", "req-a", request.effectiveDate().plusDays(1)))) {
            assertThrows(IdempotencyConflictException.class, () -> service.issue(changed, "owner-a"));
            assertEquals(created, service.issue(request, "owner-a"), "conflict must not replace original receipt");
        }
        assertNotEquals(created, service.issue(new PolicyIssueRequest("journey-a", "quote-a", "req-b", request.effectiveDate().plusDays(2)), "owner-a"));
        var ownerBResult = service.issue(request, "owner-b");
        assertEquals("owner-b", ownerBResult.ownerId());
        assertNotEquals(created, ownerBResult);
        assertEquals(created, service.issue(request, "owner-a"), "owner scope remains independent");

        AtomicInteger constructions = new AtomicInteger();
        var facts = new HashMap<String, CandidateFacts>();
        facts.put("owner-a|journey-a|quote-a", new CandidateFacts("home-secure-v1", "rate-a", new BigDecimal("125.00"), "INR"));
        facts.put("owner-a|journey-a|quote-b", new CandidateFacts("home-secure-v2", "rate-b", new BigDecimal("175.00"), "INR"));
        facts.put("owner-a|journey-other|quote-a", new CandidateFacts("home-secure-v3", "rate-c", new BigDecimal("225.00"), "INR"));
        facts.put("owner-b|journey-a|quote-a", new CandidateFacts("home-secure-owner-b", "rate-owner-b", new BigDecimal("310.00"), "EUR"));
        facts.put("owner-a|journey-b|quote-b", new CandidateFacts("home-secure-alt", "rate-alt", new BigDecimal("410.00"), "GBP"));
        PolicyIssueSnapshotFactory candidates = (command, owner) -> {
            int n=constructions.incrementAndGet();
            var quote=facts.get(owner+"|"+command.journeyId()+"|"+command.quoteVersionId());
            if(quote==null) throw new IllegalArgumentException("missing independent candidate facts");
            return new PolicySnapshot("candidate-"+n,owner,command.journeyId(),command.quoteVersionId(),quote.product(),quote.rate(),
                    quote.premium(),quote.currency(),command.effectiveDate(),PolicyStatus.ISSUED,1,List.of(),null);
        };
        var observed = new PolicyLifecycleService(CLOCK,candidates);
        var expectedFirst=new PolicySnapshot("candidate-1","owner-a","journey-a","quote-a","home-secure-v1","rate-a",
                new BigDecimal("125.00"),"INR",request.effectiveDate(),PolicyStatus.ISSUED,1,List.of(),null);
        var first=observed.issue(request,"owner-a"); assertEquals(expectedFirst,first,"the entire supplied quote candidate is used");
        facts.put("owner-a|journey-a|quote-a",new CandidateFacts("catalog-now-v9","rate-now-v9",new BigDecimal("999.00"),"CAD"));
        assertEquals(first,observed.issue(request,"owner-a")); assertEquals(1,constructions.get());
        var changedQuote=new PolicyIssueRequest("journey-a","quote-b","req-a",request.effectiveDate());
        var changedJourney=new PolicyIssueRequest("journey-other","quote-a","req-a",request.effectiveDate());
        var changedDate=new PolicyIssueRequest("journey-a","quote-a","req-a",request.effectiveDate().plusDays(1));
        for(var changed:List.of(changedQuote,changedJourney,changedDate)) {
            assertThrows(IdempotencyConflictException.class,()->observed.issue(changed,"owner-a"));
            assertEquals(first,observed.issue(request,"owner-a"),"each conflict leaves the full original despite changed catalog facts");
        }
        assertEquals(1,constructions.get(),"replay and conflicts never construct a candidate");
        var ownerB=observed.issue(request,"owner-b");
        assertEquals(new PolicySnapshot("candidate-2","owner-b","journey-a","quote-a","home-secure-owner-b","rate-owner-b",
                new BigDecimal("310.00"),"EUR",request.effectiveDate(),PolicyStatus.ISSUED,1,List.of(),null),ownerB);
        var otherIntentRequest=new PolicyIssueRequest("journey-b","quote-b","req-b",request.effectiveDate().plusDays(2));
        var otherIntent=observed.issue(otherIntentRequest,"owner-a");
        assertEquals(new PolicySnapshot("candidate-3","owner-a","journey-b","quote-b","home-secure-alt","rate-alt",
                new BigDecimal("410.00"),"GBP",otherIntentRequest.effectiveDate(),PolicyStatus.ISSUED,1,List.of(),null),otherIntent);
        assertEquals(3,constructions.get());
        assertEquals(first,observed.issue(request,"owner-a"),"other owner/key cannot replace the original receipt");
    }

    @Test void day33StoredIssueSnapshotDoesNotRepriceWhenCatalogChanges() {
        var original=issued();
        assertTrue(service.isImmutableIssueSnapshot(original,original));
        var oneFieldChanges=List.of(
                new PolicySnapshot("policy-b",original.ownerId(),original.journeyId(),original.quoteVersionId(),original.productVersion(),original.rateVersion(),original.annualPremiumRupees(),original.currency(),original.effectiveDate(),original.status(),original.version(),original.endorsements(),original.cancellationEffectiveDate()),
                new PolicySnapshot(original.policyId(),"owner-b",original.journeyId(),original.quoteVersionId(),original.productVersion(),original.rateVersion(),original.annualPremiumRupees(),original.currency(),original.effectiveDate(),original.status(),original.version(),original.endorsements(),original.cancellationEffectiveDate()),
                new PolicySnapshot(original.policyId(),original.ownerId(),"journey-b",original.quoteVersionId(),original.productVersion(),original.rateVersion(),original.annualPremiumRupees(),original.currency(),original.effectiveDate(),original.status(),original.version(),original.endorsements(),original.cancellationEffectiveDate()),
                new PolicySnapshot(original.policyId(),original.ownerId(),original.journeyId(),"quote-b",original.productVersion(),original.rateVersion(),original.annualPremiumRupees(),original.currency(),original.effectiveDate(),original.status(),original.version(),original.endorsements(),original.cancellationEffectiveDate()),
                new PolicySnapshot(original.policyId(),original.ownerId(),original.journeyId(),original.quoteVersionId(),"home-secure-v2",original.rateVersion(),original.annualPremiumRupees(),original.currency(),original.effectiveDate(),original.status(),original.version(),original.endorsements(),original.cancellationEffectiveDate()),
                new PolicySnapshot(original.policyId(),original.ownerId(),original.journeyId(),original.quoteVersionId(),original.productVersion(),"rate-v2",original.annualPremiumRupees(),original.currency(),original.effectiveDate(),original.status(),original.version(),original.endorsements(),original.cancellationEffectiveDate()),
                new PolicySnapshot(original.policyId(),original.ownerId(),original.journeyId(),original.quoteVersionId(),original.productVersion(),original.rateVersion(),new BigDecimal("999.00"),original.currency(),original.effectiveDate(),original.status(),original.version(),original.endorsements(),original.cancellationEffectiveDate()),
                new PolicySnapshot(original.policyId(),original.ownerId(),original.journeyId(),original.quoteVersionId(),original.productVersion(),original.rateVersion(),original.annualPremiumRupees(),"EUR",original.effectiveDate(),original.status(),original.version(),original.endorsements(),original.cancellationEffectiveDate()),
                new PolicySnapshot(original.policyId(),original.ownerId(),original.journeyId(),original.quoteVersionId(),original.productVersion(),original.rateVersion(),original.annualPremiumRupees(),original.currency(),original.effectiveDate().plusDays(1),original.status(),original.version(),original.endorsements(),original.cancellationEffectiveDate()));
        for(var changed:oneFieldChanges) assertFalse(service.isImmutableIssueSnapshot(original,changed),"each captured issue field is independently immutable: "+changed);
        var legitimatelyServiced=new PolicySnapshot(original.policyId(),original.ownerId(),original.journeyId(),original.quoteVersionId(),original.productVersion(),original.rateVersion(),original.annualPremiumRupees(),original.currency(),original.effectiveDate(),PolicyStatus.ISSUED,2,List.of(new PolicyEndorsement(2,LocalDate.parse("2026-12-02"),"REBUILD_AMOUNT",new BigDecimal("200000.00"))),null);
        assertTrue(service.isImmutableIssueSnapshot(original,legitimatelyServiced),"servicing status/version/history may change without repricing issue facts");
        var scheduled=new PolicySnapshot(original.policyId(),original.ownerId(),original.journeyId(),original.quoteVersionId(),original.productVersion(),original.rateVersion(),original.annualPremiumRupees(),original.currency(),original.effectiveDate(),PolicyStatus.CANCEL_SCHEDULED,3,legitimatelyServiced.endorsements(),LocalDate.parse("2026-12-10"));
        assertTrue(service.isImmutableIssueSnapshot(original,scheduled),"a valid cancellation schedule changes servicing state/date, not captured issue facts");
    }

    @Test void day34MapsCreateReplayAndConflictingReplayToStableHttpCodes() {
        var request = issueRequest();
        assertEquals(201, service.issueHttpStatus(request, Optional.empty()));
        assertEquals(200, service.issueHttpStatus(request, Optional.of(issued())));
        assertEquals(409, service.issueHttpStatus(new PolicyIssueRequest("journey-a", "quote-other", "req-a", request.effectiveDate()), Optional.of(issued())));
        assertEquals(409, service.issueHttpStatus(new PolicyIssueRequest("journey-other", "quote-a", "req-a", request.effectiveDate()), Optional.of(issued())));
        assertEquals(409, service.issueHttpStatus(new PolicyIssueRequest("journey-a", "quote-a", "req-a", request.effectiveDate().plusDays(1)), Optional.of(issued())));
        assertThrows(IllegalArgumentException.class, () -> service.issueHttpStatus(new PolicyIssueRequest("journey-a", "quote-a", " ", request.effectiveDate()), Optional.empty()));
    }

    @Test void day35AtomicPostgresWriteHasOnePolicyAndOneReceipt() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            var dataSource = postgres.getPostgresDatabase();
            initializeLifecycleSchema(dataSource);
            AtomicInteger constructions=new AtomicInteger();
            var transactionService=new PolicyLifecycleService(CLOCK,(request,owner)->new PolicySnapshot("candidate-"+constructions.incrementAndGet(),owner,request.journeyId(),request.quoteVersionId(),
                    "home-secure-v1","training-rate-v1",new BigDecimal("125.00"),"INR",request.effectiveDate(),PolicyStatus.ISSUED,1,List.of(),null));
            var committed = transactionService.persistIssueAtomically(dataSource, issueRequest(), "owner-a");
            assertNotNull(committed);
            assertEquals(1,constructions.get());
            assertEquals(committed, transactionService.issue(issueRequest(), "owner-a"), "same service instance must publish replay only after commit");
            assertEquals(1,constructions.get(),"a committed replay returns the original without rebuilding a candidate");
            assertEquals(1, count(dataSource, "training_policy_lifecycle"));
            assertEquals(1, count(dataSource, "training_policy_idempotency"));
            assertEquals(1, count(dataSource, "training_policy_lifecycle_event"));
            assertPersistedPolicy(dataSource,committed);
            try(Connection c=dataSource.getConnection();var s=c.prepareStatement("SELECT owner_id,client_request_id,policy_id FROM training_policy_idempotency");var rs=s.executeQuery()){
                assertTrue(rs.next());assertEquals("owner-a",rs.getString(1));assertEquals("req-a",rs.getString(2));assertEquals(committed.policyId(),rs.getString(3));assertFalse(rs.next());
            }
            assertEvent(dataSource,committed.policyId(),1,"ISSUED",committed.effectiveDate(),null,null);
        }
    }

    @Test void day35ReceiptConflictRollsBackPolicyAndEvent() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            var dataSource = postgres.getPostgresDatabase();
            initializeLifecycleSchema(dataSource);
            AtomicInteger constructions=new AtomicInteger();
            var transactionService=new PolicyLifecycleService(CLOCK,(request,owner)->new PolicySnapshot("candidate-"+constructions.incrementAndGet(),owner,request.journeyId(),request.quoteVersionId(),
                    "home-secure-v1","training-rate-v1",new BigDecimal("125.00"),"INR",request.effectiveDate(),PolicyStatus.ISSUED,1,List.of(),null));
            try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
                s.executeUpdate("INSERT INTO training_policy_lifecycle VALUES ('old-policy','owner-a','journey-old','quote-old','home-secure-v1','rate-v1',125.00,'INR',DATE '2026-12-01','ISSUED',1,NULL)");
                s.executeUpdate("INSERT INTO training_policy_lifecycle_event VALUES ('old-policy',1,'ISSUED',DATE '2026-12-01',NULL,NULL)");
                s.executeUpdate("INSERT INTO training_policy_idempotency VALUES ('owner-a','req-a','different-fingerprint','old-policy')");
            }
            RuntimeException conflict = assertThrows(RuntimeException.class,
                    () -> transactionService.persistIssueAtomically(dataSource, issueRequest(), "owner-a"));
            assertInstanceOf(IdempotencyConflictException.class, conflict);
            assertEquals(1,constructions.get(),"candidate was constructed for the failed transaction but must not be published");
            assertEquals(1, count(dataSource, "training_policy_lifecycle"), "failed receipt write must roll back the new policy row");
            assertEquals(1, count(dataSource, "training_policy_idempotency"), "pre-existing receipt remains the only receipt");
            assertEquals(1, count(dataSource, "training_policy_lifecycle_event"), "failed transaction leaves no new issue event");
            var old=new PolicySnapshot("old-policy","owner-a","journey-old","quote-old","home-secure-v1","rate-v1",new BigDecimal("125.00"),"INR",
                    LocalDate.parse("2026-12-01"),PolicyStatus.ISSUED,1,List.of(),null);
            assertPersistedPolicy(dataSource,old);
            assertEvent(dataSource,"old-policy",1,"ISSUED",LocalDate.parse("2026-12-01"),null,null);
            try(Connection c=dataSource.getConnection();var s=c.prepareStatement("SELECT owner_id,client_request_id,request_fingerprint,policy_id FROM training_policy_idempotency");var rs=s.executeQuery()){
                assertTrue(rs.next());assertEquals("owner-a",rs.getString(1));assertEquals("req-a",rs.getString(2));assertEquals("different-fingerprint",rs.getString(3));assertEquals("old-policy",rs.getString(4));assertFalse(rs.next());
            }
            var afterRollback=transactionService.issue(issueRequest(),"owner-a");
            assertEquals("candidate-2",afterRollback.policyId(),"after rollback the same intent constructs afresh rather than replaying a phantom");
            assertEquals(2,constructions.get());
            assertEquals(1,count(dataSource,"training_policy_lifecycle"),"the pure subsequent candidate did not alter the rolled-back database rows");
        }
    }

    @Test void day36ForeignAndAbsentPolicyAreBothNotFound() {
        var seeded = issued();
        PolicyReadStore seededReadStore = (policyId, ownerId) ->
                "policy-a".equals(policyId) && "owner-a".equals(ownerId) ? Optional.of(seeded) : Optional.empty();
        assertAll("seeded owner read and concealment cases",
                () -> assertEquals(Optional.of(seeded), service.readForOwner(seededReadStore, "owner-a", "policy-a"), "owner can read a seeded policy"),
                () -> assertTrue(service.readForOwner(seededReadStore, "owner-b", "policy-a").isEmpty(), "foreign owner is not distinguishable from absence"),
                () -> assertTrue(service.readForOwner(seededReadStore, "owner-a", "missing").isEmpty(), "absent policy has the same result"));
    }

    @Test void day37EndorsementRejectsUnknownNoopPastDateAndStaleVersion() {
        assertAll("endorsement command invariants",
                () -> assertTrue(service.validateEndorsement(issued(), new PolicyEndorsementRequest(1, LocalDate.parse("2026-12-02"), "REBUILD_AMOUNT", new BigDecimal("200000.00")))),
                () -> assertFalse(service.validateEndorsement(issued(), new PolicyEndorsementRequest(1, LocalDate.parse("2026-12-02"), "UNKNOWN", new BigDecimal("200000.00"))), "unknown reason is rejected with an otherwise valid date/version/amount"),
                () -> assertFalse(service.validateEndorsement(issued(), new PolicyEndorsementRequest(0, LocalDate.parse("2026-12-02"), "REBUILD_AMOUNT", new BigDecimal("200000.00"))), "stale expected version 0 must be rejected for current version 1"),
                () -> assertFalse(service.validateEndorsement(issued(), new PolicyEndorsementRequest(2, LocalDate.parse("2026-12-02"), "REBUILD_AMOUNT", new BigDecimal("200000.00"))), "future expected version must be rejected"),
                () -> assertFalse(service.validateEndorsement(issued(), new PolicyEndorsementRequest(1, LocalDate.parse("2026-11-18"), "REBUILD_AMOUNT", new BigDecimal("200000.00"))), "past effective date is rejected independently of reason and version"),
                () -> assertFalse(service.validateEndorsement(issued(), new PolicyEndorsementRequest(1, LocalDate.parse("2026-12-02"), "REBUILD_AMOUNT", new BigDecimal("125.00"))), "no-op amount must be rejected"));
    }

    @Test void day38EndorsementAppendsAndPreservesPriorHistoryInEffectiveOrder() {
        var v2History=List.of(new PolicyEndorsement(2,LocalDate.parse("2026-12-02"),"REBUILD_AMOUNT",new BigDecimal("200000.00")));
        var policyV2=withHistory(v2History);
        var requestA=new PolicyEndorsementRequest(2,LocalDate.parse("2026-12-04"),"REBUILD_AMOUNT",new BigDecimal("300000.00"));
        var expectedA=new PolicySnapshot("policy-a","owner-a","journey-a","quote-a","home-secure-v1","training-rate-v1",new BigDecimal("125.00"),"INR",LocalDate.parse("2026-12-01"),PolicyStatus.ISSUED,3,List.of(v2History.get(0),new PolicyEndorsement(3,requestA.effectiveDate(),requestA.changeCode(),requestA.trainingRebuildAmountRupees())),null);
        var changedA=service.endorse(policyV2,requestA);
        assertEquals(expectedA,changedA,"first appended snapshot is fully derived from the valid v2 input and submitted request");
        assertEquals(2,policyV2.version(),"input value is unchanged");
        assertEquals(v2History,policyV2.endorsements(),"input history remains unchanged");

        var policyV2Different=withHistory(List.of(new PolicyEndorsement(2,LocalDate.parse("2026-12-03"),"REBUILD_AMOUNT",new BigDecimal("250000.00"))));
        var originalDifferentHistory=policyV2Different.endorsements();
        var requestB=new PolicyEndorsementRequest(2,LocalDate.parse("2026-12-05"),"REBUILD_AMOUNT",new BigDecimal("400000.00"));
        var expectedB=new PolicySnapshot("policy-a","owner-a","journey-a","quote-a","home-secure-v1","training-rate-v1",new BigDecimal("125.00"),"INR",LocalDate.parse("2026-12-01"),PolicyStatus.ISSUED,3,List.of(originalDifferentHistory.get(0),new PolicyEndorsement(3,requestB.effectiveDate(),requestB.changeCode(),requestB.trainingRebuildAmountRupees())),null);
        assertEquals(expectedB,service.endorse(policyV2Different,requestB),"a distinct valid request must produce its own full expected history");
        assertEquals(2,policyV2Different.version());
        assertEquals(originalDifferentHistory,policyV2Different.endorsements());
    }

    @Test void day39StaleConcurrentEndorsementDoesNotCreatePartialVersion() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            var dataSource = postgres.getPostgresDatabase();
            initializeLifecycleSchema(dataSource);
            seedPolicy(dataSource);
            seedIssueEvent(dataSource);
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);
            try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
                var firstRequest=new PolicyEndorsementRequest(1,LocalDate.parse("2026-12-02"),"REBUILD_AMOUNT",new BigDecimal("200000.00"));
                var secondRequest=new PolicyEndorsementRequest(1,LocalDate.parse("2026-12-03"),"REBUILD_AMOUNT",new BigDecimal("300000.00"));
                var first = pool.submit(() -> raceEndorse(ready, start, dataSource, firstRequest));
                var second = pool.submit(() -> raceEndorse(ready, start, dataSource, secondRequest));
                assertTrue(ready.await(5, TimeUnit.SECONDS));
                start.countDown();
                Object firstResult = getRaceResult(first);
                Object secondResult = getRaceResult(second);
                assertEquals(1, (firstResult instanceof PolicySnapshot ? 1 : 0) + (secondResult instanceof PolicySnapshot ? 1 : 0), "exactly one writer wins version 1");
                assertEquals(1, (firstResult instanceof VersionConflictException ? 1 : 0) + (secondResult instanceof VersionConflictException ? 1 : 0), "the loser gets a stale-version conflict");
                assertEquals(2, scalarLong(dataSource, "SELECT version FROM training_policy_lifecycle WHERE policy_id='policy-a'"));
                assertEquals(2, count(dataSource, "training_policy_lifecycle_event"), "only issued and winning endorsement events exist");
                var winningRequest=firstResult instanceof PolicySnapshot?firstRequest:secondRequest;
                PolicySnapshot expectedWinner=new PolicySnapshot("policy-a","owner-a","journey-a","quote-a","home-secure-v1","training-rate-v1",new BigDecimal("125.00"),"INR",LocalDate.parse("2026-12-01"),PolicyStatus.ISSUED,2,List.of(new PolicyEndorsement(2,winningRequest.effectiveDate(),winningRequest.changeCode(),winningRequest.trainingRebuildAmountRupees())),null);
                PolicySnapshot winner=firstResult instanceof PolicySnapshot p?p:(PolicySnapshot)secondResult;
                var winningChange=winningRequest;
                assertEquals(expectedWinner,winner,"returned winner must match the exact submitted request that succeeded");
                assertPersistedPolicy(dataSource,winner);
                assertEvent(dataSource,"policy-a",2,"ENDORSED",winningChange.effectiveDate(),winningChange.changeCode(),winningChange.trainingRebuildAmountRupees());
            }
        }
    }

    @Test void day40CancellationMustBeFutureAndRequestReplayIsIdempotent() {
        var request = new PolicyCancellationRequest(1, "cancel-a", LocalDate.parse("2026-12-10"), "CUSTOMER_REQUEST");
        var scheduled = new PolicySnapshot("policy-a", "owner-a", "journey-a", "quote-a", "home-secure-v1", "training-rate-v1",
                new BigDecimal("125.00"), "INR", LocalDate.parse("2026-12-01"), PolicyStatus.CANCEL_SCHEDULED, 2, List.of(), LocalDate.parse("2026-12-10"));
        PolicyCancellationReceiptStore empty = (owner, key) -> Optional.empty();
        PolicyCancellationReceiptStore replayReceipt = (owner, key) -> "owner-a".equals(owner) && "cancel-a".equals(key)
                ? Optional.of(new PolicyCancellationReceipt("owner-a", "cancel-a", LocalDate.parse("2026-12-10"), "CUSTOMER_REQUEST", scheduled))
                : Optional.empty();
        assertAll("schedule, same-intent replay, conflicting replay, and past-date rejection",
                () -> assertEquals(PolicyStatus.CANCEL_SCHEDULED, service.scheduleCancellation(issued(), request, empty).status()),
                () -> assertEquals(scheduled, service.scheduleCancellation(scheduled, request, replayReceipt), "same request replay returns original scheduled snapshot"),
                () -> assertThrows(IdempotencyConflictException.class, () -> service.scheduleCancellation(scheduled,
                        new PolicyCancellationRequest(2, "cancel-a", LocalDate.parse("2026-12-11"), "CUSTOMER_REQUEST"), replayReceipt)),
                () -> assertThrows(IdempotencyConflictException.class, () -> service.scheduleCancellation(scheduled,
                        new PolicyCancellationRequest(2, "cancel-a", LocalDate.parse("2026-12-10"), "OTHER_REASON"), replayReceipt)),
                () -> assertEquals(PolicyStatus.CANCEL_SCHEDULED, service.scheduleCancellation(issued(),
                        new PolicyCancellationRequest(1, "cancel-today", LocalDate.parse("2026-11-19"), "CUSTOMER_REQUEST"), empty).status(), "same day is non-past under injected Clock"),
                () -> assertThrows(IllegalArgumentException.class, () -> service.scheduleCancellation(issued(),
                        new PolicyCancellationRequest(0, "cancel-wrong-version", LocalDate.parse("2026-12-10"), "CUSTOMER_REQUEST"), empty), "wrong expected version is independently rejected"),
                () -> assertThrows(IllegalArgumentException.class, () -> service.scheduleCancellation(issued(),
                        new PolicyCancellationRequest(1, "cancel-bad-reason", LocalDate.parse("2026-12-10"), "UNKNOWN"), empty), "unknown reason is independently rejected"),
                () -> assertThrows(IllegalArgumentException.class, () -> service.scheduleCancellation(issued(),
                        new PolicyCancellationRequest(1, "cancel-past", LocalDate.parse("2026-11-18"), "CUSTOMER_REQUEST"), empty)));
    }

    @Test void day41CancellationRunsOnlyAtOrAfterEffectiveDateAndOnlyOnce() {
        var scheduled = new PolicySnapshot("policy-a", "owner-a", "journey-a", "quote-a", "home-secure-v1", "training-rate-v1",
                new BigDecimal("125.00"), "INR", LocalDate.parse("2026-12-01"), PolicyStatus.CANCEL_SCHEDULED, 2, List.of(), LocalDate.parse("2026-12-10"));
            var before = new PolicyLifecycleService(clockOn("2026-12-09"));
            var onDate = new PolicyLifecycleService(clockOn("2026-12-10"));
            var after = new PolicyLifecycleService(clockOn("2026-12-11"));
            var cancelled = new PolicySnapshot("policy-a", "owner-a", "journey-a", "quote-a", "home-secure-v1", "training-rate-v1",
                    new BigDecimal("125.00"), "INR", LocalDate.parse("2026-12-01"), PolicyStatus.CANCELLED, 3, List.of(), LocalDate.parse("2026-12-10"));
            assertAll("fixed-clock due-date boundaries and retry",
                    () -> assertEquals(PolicyStatus.CANCEL_SCHEDULED, before.applyDueCancellation(scheduled).status(), "one day before is unchanged"),
                    () -> assertEquals(PolicyStatus.CANCELLED, onDate.applyDueCancellation(scheduled).status(), "effective date transitions once due"),
                    () -> assertEquals(PolicyStatus.CANCELLED, after.applyDueCancellation(scheduled).status(), "after effective date remains cancelled"),
                    () -> assertEquals(cancelled, after.applyDueCancellation(cancelled), "retry after transition is idempotent"));
    }

    @Test void day41PersistedDueCancellationUpdatesPolicyAndEventAtomically() throws Exception {
        try(EmbeddedPostgres postgres=EmbeddedPostgres.builder().start()){
            var dataSource=postgres.getPostgresDatabase();initializeLifecycleSchema(dataSource);
            try(Connection c=dataSource.getConnection();Statement s=c.createStatement()){
                s.executeUpdate("INSERT INTO training_policy_lifecycle VALUES ('policy-a','owner-a','journey-a','quote-a','home-secure-v1','training-rate-v1',125.00,'INR',DATE '2026-12-01','CANCEL_SCHEDULED',2,DATE '2026-12-10')");
                s.executeUpdate("INSERT INTO training_policy_lifecycle_event VALUES ('policy-a',1,'ISSUED',DATE '2026-12-01',NULL,NULL)");
                s.executeUpdate("INSERT INTO training_policy_lifecycle_event VALUES ('policy-a',2,'CANCEL_SCHEDULED',DATE '2026-12-10','CUSTOMER_REQUEST',NULL)");
            }
            var scheduled=new PolicySnapshot("policy-a","owner-a","journey-a","quote-a","home-secure-v1","training-rate-v1",new BigDecimal("125.00"),"INR",
                    LocalDate.parse("2026-12-01"),PolicyStatus.CANCEL_SCHEDULED,2,List.of(),LocalDate.parse("2026-12-10"));
            var before=new PolicyLifecycleService(clockOn("2026-12-09"));
            assertEquals(scheduled,before.applyDueCancellation(dataSource,scheduled),"persisted overload leaves an early schedule unchanged");
            assertPersistedPolicy(dataSource,scheduled);assertEquals(2,count(dataSource,"training_policy_lifecycle_event"));
            var due=new PolicyLifecycleService(clockOn("2026-12-10"));
            var cancelled=new PolicySnapshot("policy-a","owner-a","journey-a","quote-a","home-secure-v1","training-rate-v1",new BigDecimal("125.00"),"INR",
                    LocalDate.parse("2026-12-01"),PolicyStatus.CANCELLED,3,List.of(),LocalDate.parse("2026-12-10"));
            assertEquals(cancelled,due.applyDueCancellation(dataSource,scheduled));
            assertPersistedPolicy(dataSource,cancelled);assertEquals(3,count(dataSource,"training_policy_lifecycle_event"));
            assertEvent(dataSource,"policy-a",3,"CANCELLED",LocalDate.parse("2026-12-10"),null,null);
            assertEquals(cancelled,due.applyDueCancellation(dataSource,cancelled),"retry does not append another state version");
            assertPersistedPolicy(dataSource,cancelled);assertEquals(3,count(dataSource,"training_policy_lifecycle_event"));
        }
    }

    @Test void day42CancelledPolicyRejectsWritesButRemainsReadable() {
        var cancelled = withStatus(PolicyStatus.CANCELLED);
        assertFalse(service.permitsServicingWrite(cancelled));
        PolicyReadStore store = (policyId, ownerId) -> Optional.of(cancelled);
        assertEquals(Optional.of(cancelled), service.readForOwner(store, "owner-a", "policy-a"));
    }

    @Test void day43ServicingProjectionOmitsOwnerJourneyAndQuoteIds() {
        var first = new PolicySnapshot("policy-a", "owner-a", "journey-a", "quote-a", "home-secure-v1", "training-rate-v1",
                new BigDecimal("125.00"), "INR", LocalDate.parse("2026-12-01"), PolicyStatus.ISSUED, 2,
                List.of(new PolicyEndorsement(2, LocalDate.parse("2026-12-02"), "REBUILD_AMOUNT", new BigDecimal("200000.00"))), null);
        var firstView = service.toServicingView(first);
        assertProjectionEquals(first, firstView);

        var second = new PolicySnapshot("policy-b", "owner-b", "journey-b", "quote-b", "home-secure-v9", "training-rate-v9",
                new BigDecimal("987.65"), "USD", LocalDate.parse("2026-12-03"), PolicyStatus.CANCEL_SCHEDULED, 8,
                List.of(new PolicyEndorsement(6, LocalDate.parse("2026-12-04"), "REBUILD_AMOUNT", new BigDecimal("310000.00")),
                        new PolicyEndorsement(7, LocalDate.parse("2026-12-05"), "REBUILD_AMOUNT", new BigDecimal("410000.00"))),
                LocalDate.parse("2026-12-20"));
        var secondView = service.toServicingView(second);
        assertProjectionEquals(second, secondView);
        assertNull(findField(firstView.getClass(), "ownerId"));
        assertNull(findField(firstView.getClass(), "journeyId"));
        assertNull(findField(firstView.getClass(), "quoteVersionId"));
        assertNull(findField(secondView.getClass(), "ownerId"));
        assertNull(findField(secondView.getClass(), "journeyId"));
        assertNull(findField(secondView.getClass(), "quoteVersionId"));
    }

    @Test void day44LifecycleIntegrationSeamMustVerifyReadback() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            var dataSource = postgres.getPostgresDatabase();
            initializeLifecycleSchema(dataSource);
            seedPolicy(dataSource);
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO training_policy_lifecycle_event VALUES ('policy-a',1,'ISSUED',DATE '2026-12-01',NULL,NULL)");
                statement.executeUpdate("INSERT INTO training_policy_lifecycle_event VALUES ('policy-a',2,'ENDORSED',DATE '2026-12-03','REBUILD_AMOUNT',200000.00)");
                statement.executeUpdate("INSERT INTO training_policy_lifecycle_event VALUES ('policy-a',3,'CANCEL_SCHEDULED',DATE '2026-12-10','CUSTOMER_REQUEST',NULL)");
                statement.executeUpdate("INSERT INTO training_policy_lifecycle_event VALUES ('policy-a',4,'CANCELLED',DATE '2026-12-10',NULL,NULL)");
                statement.executeUpdate("UPDATE training_policy_lifecycle SET status='CANCELLED',version=4,cancellation_effective_date=DATE '2026-12-10' WHERE policy_id='policy-a'");
            }
            assertEquals(4, count(dataSource, "training_policy_lifecycle_event"));
            assertEquals(4, scalarLong(dataSource, "SELECT version FROM training_policy_lifecycle WHERE policy_id='policy-a'"));
            service.verifyLifecycleReadback(dataSource, "policy-a");
        }
    }

    @Test void day44ReadbackRejectsMissingPolicyIncompleteHistoryAndStatusVersionMismatch() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            var ds=postgres.getPostgresDatabase(); initializeLifecycleSchema(ds);
            assertThrows(IllegalStateException.class,()->service.verifyLifecycleReadback(ds,"missing-policy"));
        }
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            var ds=postgres.getPostgresDatabase(); initializeLifecycleSchema(ds); seedPolicy(ds);
            assertThrows(IllegalStateException.class,()->service.verifyLifecycleReadback(ds,"policy-a"),"missing history is rejected");
        }
        for(String corruption:List.of("intermediate","version","status")) {
            try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
                var ds=postgres.getPostgresDatabase(); initializeLifecycleSchema(ds); seedPolicy(ds);
                try(Connection c=ds.getConnection();Statement s=c.createStatement()) {
                    s.executeUpdate("INSERT INTO training_policy_lifecycle_event VALUES ('policy-a',1,'ISSUED',DATE '2026-12-01',NULL,NULL)");
                    if(corruption.equals("intermediate")) {
                        s.executeUpdate("INSERT INTO training_policy_lifecycle_event VALUES ('policy-a',2,'ENDORSED',DATE '2026-12-03','REBUILD_AMOUNT',200000.00)");
                        s.executeUpdate("INSERT INTO training_policy_lifecycle_event VALUES ('policy-a',3,'CANCEL_SCHEDULED',DATE '2026-12-10','CUSTOMER_REQUEST',NULL)");
                        s.executeUpdate("INSERT INTO training_policy_lifecycle_event VALUES ('policy-a',4,'CANCELLED',DATE '2026-12-10',NULL,NULL)");
                        s.executeUpdate("UPDATE training_policy_lifecycle SET status='CANCELLED',version=4,cancellation_effective_date=DATE '2026-12-10' WHERE policy_id='policy-a'");
                        s.executeUpdate("DELETE FROM training_policy_lifecycle_event WHERE policy_id='policy-a' AND version=2");
                    } else if(corruption.equals("version")) s.executeUpdate("UPDATE training_policy_lifecycle SET version=2 WHERE policy_id='policy-a'");
                    else s.executeUpdate("UPDATE training_policy_lifecycle SET status='CANCELLED' WHERE policy_id='policy-a'");
                }
                assertThrows(IllegalStateException.class,()->service.verifyLifecycleReadback(ds,"policy-a"),corruption+" mismatch is rejected");
            }
        }
    }

    private static PolicyIssueRequest issueRequest() {
        return new PolicyIssueRequest("journey-a", "quote-a", "req-a", LocalDate.parse("2026-12-01"));
    }
    private record CandidateFacts(String product, String rate, BigDecimal premium, String currency) {}
    private static PolicySnapshot issued() {
        return new PolicySnapshot("policy-a", "owner-a", "journey-a", "quote-a", "home-secure-v1", "training-rate-v1",
                new BigDecimal("125.00"), "INR", LocalDate.parse("2026-12-01"), PolicyStatus.ISSUED, 1, List.of(), null);
    }
    private static PolicySnapshot withHistory(List<PolicyEndorsement> history) {
        var p=issued(); return new PolicySnapshot(p.policyId(),p.ownerId(),p.journeyId(),p.quoteVersionId(),p.productVersion(),p.rateVersion(),p.annualPremiumRupees(),p.currency(),p.effectiveDate(),p.status(),2,history,null);
    }
    private static PolicySnapshot withStatus(PolicyStatus status) {
        var p=issued(); return new PolicySnapshot(p.policyId(),p.ownerId(),p.journeyId(),p.quoteVersionId(),p.productVersion(),p.rateVersion(),p.annualPremiumRupees(),p.currency(),p.effectiveDate(),status,3,List.of(),null);
    }
    private static void assertProjectionEquals(PolicySnapshot source, PolicyServicingView projected) {
        assertEquals(source.policyId(), projected.policyId());
        assertEquals(source.productVersion(), projected.productVersion());
        assertEquals(source.annualPremiumRupees(), projected.annualPremiumRupees());
        assertEquals(source.currency(), projected.currency());
        assertEquals(source.status(), projected.status());
        assertEquals(source.version(), projected.version());
        assertEquals(source.endorsements(), projected.history());
        assertEquals(source.cancellationEffectiveDate(), projected.cancellationEffectiveDate());
    }
    private static java.lang.reflect.Field findField(Class<?> type, String name) {
        try { return type.getDeclaredField(name); } catch (NoSuchFieldException expected) { return null; }
    }
    private static void initializeLifecycleSchema(javax.sql.DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            for (String sql : Files.readString(Path.of("src/test/resources/fixtures/continuation-31-44/policy-lifecycle.sql")).split(";"))
                if (!sql.isBlank()) statement.execute(sql);
        }
    }
    private static void seedPolicy(javax.sql.DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO training_policy_lifecycle VALUES ('policy-a','owner-a','journey-a','quote-a','home-secure-v1','training-rate-v1',125.00,'INR',DATE '2026-12-01','ISSUED',1,NULL)");
        }
    }

    private static void assertPersistedPolicy(javax.sql.DataSource dataSource,PolicySnapshot expected)throws Exception {
        try(Connection c=dataSource.getConnection();var s=c.prepareStatement("SELECT owner_id,journey_id,quote_version_id,product_version,rate_version,annual_premium_rupees,currency,effective_date,status,version,cancellation_effective_date FROM training_policy_lifecycle WHERE policy_id=?")){
            s.setString(1,expected.policyId());try(var rs=s.executeQuery()){
                assertTrue(rs.next(),"persisted policy row exists");
                assertEquals(expected.ownerId(),rs.getString(1));assertEquals(expected.journeyId(),rs.getString(2));assertEquals(expected.quoteVersionId(),rs.getString(3));
                assertEquals(expected.productVersion(),rs.getString(4));assertEquals(expected.rateVersion(),rs.getString(5));
                assertEquals(0,expected.annualPremiumRupees().compareTo(rs.getBigDecimal(6)));assertEquals(expected.currency(),rs.getString(7).trim());
                assertEquals(expected.effectiveDate(),rs.getDate(8).toLocalDate());assertEquals(expected.status().name(),rs.getString(9));assertEquals(expected.version(),rs.getLong(10));
                java.sql.Date cancellation=rs.getDate(11);assertEquals(expected.cancellationEffectiveDate(),cancellation==null?null:cancellation.toLocalDate());assertFalse(rs.next());
            }
        }
    }
    private static void assertEvent(javax.sql.DataSource dataSource,String policyId,long version,String kind,LocalDate date,String code,BigDecimal rebuildAmount)throws Exception {
        try(Connection c=dataSource.getConnection();var s=c.prepareStatement("SELECT event_type,effective_date,change_code,training_rebuild_amount_rupees FROM training_policy_lifecycle_event WHERE policy_id=? AND version=?")){
            s.setString(1,policyId);s.setLong(2,version);try(var rs=s.executeQuery()){
                assertTrue(rs.next(),"expected persisted event exists");assertEquals(kind,rs.getString(1));assertEquals(date,rs.getDate(2).toLocalDate());assertEquals(code,rs.getString(3));
                BigDecimal amount=rs.getBigDecimal(4);if(rebuildAmount==null)assertNull(amount);else assertEquals(0,rebuildAmount.compareTo(amount));assertFalse(rs.next());
            }
        }
    }

    private static long count(javax.sql.DataSource dataSource, String table) throws Exception {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement(); var rows = s.executeQuery("SELECT count(*) FROM " + table)) {
            rows.next(); return rows.getLong(1);
        }
    }
    private static long scalarLong(javax.sql.DataSource dataSource, String sql) throws Exception {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement(); var rows = s.executeQuery(sql)) {
            assertTrue(rows.next()); return rows.getLong(1);
        }
    }
    private static void seedIssueEvent(javax.sql.DataSource dataSource) throws Exception {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("INSERT INTO training_policy_lifecycle_event VALUES ('policy-a',1,'ISSUED',DATE '2026-12-01',NULL,NULL)");
        }
    }
    private static PolicySnapshot raceEndorse(CountDownLatch ready, CountDownLatch start, javax.sql.DataSource dataSource,
                                               PolicyEndorsementRequest request) throws Exception {
        ready.countDown(); if (!start.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("race start timed out");
        return serviceStatic().endorseAtExpectedVersion(dataSource, issued(), request);
    }
    private static Object getRaceResult(Future<PolicySnapshot> future) throws Exception {
        try { return future.get(10, TimeUnit.SECONDS); }
        catch (ExecutionException e) {
            // Keep an unfinished learner seam visible as its TODO error instead of
            // misreporting it as a concurrency assertion failure.
            if (e.getCause() instanceof UnsupportedOperationException todo) throw todo;
            return e.getCause();
        }
    }
    private static PolicyLifecycleService serviceStatic() { return new PolicyLifecycleService(CLOCK); }
    private static Clock clockOn(String date) { return Clock.fixed(Instant.parse(date + "T12:00:00Z"), ZoneOffset.UTC); }
}
