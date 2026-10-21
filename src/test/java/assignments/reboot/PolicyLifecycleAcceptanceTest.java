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
        var ownerB = service.issue(request, "owner-b");
        assertEquals("owner-b", ownerB.ownerId());
        assertNotEquals(created, ownerB);
        assertEquals(created, service.issue(request, "owner-a"), "owner scope remains independent");

        AtomicInteger constructions = new AtomicInteger();
        PolicyIssueSnapshotFactory candidates = (command, owner) -> {
            int n=constructions.incrementAndGet();
            return new PolicySnapshot("candidate-"+n,owner,command.journeyId(),command.quoteVersionId(),"home-secure-v1","fixture-rate-v1",
                    new BigDecimal("125.00"),"INR",command.effectiveDate(),PolicyStatus.ISSUED,1,List.of(),null);
        };
        var observed = new PolicyLifecycleService(CLOCK,candidates);
        var first=observed.issue(request,"owner-a"); assertEquals("candidate-1",first.policyId());
        assertEquals(first,observed.issue(request,"owner-a")); assertEquals(1,constructions.get());
        assertThrows(IdempotencyConflictException.class,()->observed.issue(new PolicyIssueRequest("journey-a","quote-b","req-a",request.effectiveDate()),"owner-a"));
        assertEquals(first,observed.issue(request,"owner-a")); assertEquals(1,constructions.get());
        var otherOwner=observed.issue(request,"owner-b"); assertEquals("candidate-2",otherOwner.policyId()); assertEquals("owner-b",otherOwner.ownerId());
        var otherIntent=observed.issue(new PolicyIssueRequest("journey-a","quote-a","req-b",request.effectiveDate().plusDays(1)),"owner-a");
        assertNotEquals(first,otherIntent); assertEquals(3,constructions.get());
    }

    @Test void day33StoredIssueSnapshotDoesNotRepriceWhenCatalogChanges() {
        assertTrue(service.isImmutableIssueSnapshot(issued(), issued()));
        var changedPrice = new PolicySnapshot("policy-a", "owner-a", "journey-a", "quote-a", "home-secure-v2", "rate-v2",
                new BigDecimal("999.00"), "INR", LocalDate.parse("2026-12-01"), PolicyStatus.ISSUED, 1, List.of(), null);
        assertFalse(service.isImmutableIssueSnapshot(issued(), changedPrice));
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
            var committed = service.persistIssueAtomically(dataSource, issueRequest(), "owner-a");
            assertNotNull(committed);
            assertEquals(committed, service.issue(issueRequest(), "owner-a"), "same service instance must publish replay only after commit");
            assertEquals(1, count(dataSource, "training_policy_lifecycle"));
            assertEquals(1, count(dataSource, "training_policy_idempotency"));
            assertEquals(1, count(dataSource, "training_policy_lifecycle_event"));
        }
    }

    @Test void day35ReceiptConflictRollsBackPolicyAndEvent() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            var dataSource = postgres.getPostgresDatabase();
            initializeLifecycleSchema(dataSource);
            try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
                s.executeUpdate("INSERT INTO training_policy_lifecycle VALUES ('old-policy','owner-a','journey-old','quote-old','home-secure-v1','rate-v1',125.00,'INR',DATE '2026-12-01','ISSUED',1,NULL)");
                s.executeUpdate("INSERT INTO training_policy_lifecycle_event VALUES ('old-policy',1,'ISSUED',DATE '2026-12-01',NULL,NULL)");
                s.executeUpdate("INSERT INTO training_policy_idempotency VALUES ('owner-a','req-a','different-fingerprint','old-policy')");
            }
            RuntimeException conflict = assertThrows(RuntimeException.class,
                    () -> service.persistIssueAtomically(dataSource, issueRequest(), "owner-a"));
            assertInstanceOf(IdempotencyConflictException.class, conflict);
            assertEquals(1, count(dataSource, "training_policy_lifecycle"), "failed receipt write must roll back the new policy row");
            assertEquals(1, count(dataSource, "training_policy_idempotency"), "pre-existing receipt remains the only receipt");
            assertEquals(1, count(dataSource, "training_policy_lifecycle_event"), "failed transaction leaves no new issue event");
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
                () -> assertFalse(service.validateEndorsement(issued(), new PolicyEndorsementRequest(1, LocalDate.parse("2026-11-30"), "UNKNOWN", new BigDecimal("200000.00")))),
                () -> assertFalse(service.validateEndorsement(issued(), new PolicyEndorsementRequest(0, LocalDate.parse("2026-12-02"), "REBUILD_AMOUNT", new BigDecimal("200000.00"))), "stale expected version 0 must be rejected for current version 1"),
                () -> assertFalse(service.validateEndorsement(issued(), new PolicyEndorsementRequest(2, LocalDate.parse("2026-12-02"), "REBUILD_AMOUNT", new BigDecimal("200000.00"))), "future expected version must be rejected"),
                () -> assertFalse(service.validateEndorsement(issued(), new PolicyEndorsementRequest(1, LocalDate.parse("2026-12-02"), "REBUILD_AMOUNT", new BigDecimal("125.00"))), "no-op amount must be rejected"));
    }

    @Test void day38EndorsementAppendsAndPreservesPriorHistoryInEffectiveOrder() {
        var prior = new PolicyEndorsement(2, LocalDate.parse("2026-12-03"), "REBUILD_AMOUNT", new BigDecimal("200000.00"));
        var policy = withHistory(List.of(prior));
        var changed = service.endorse(policy, new PolicyEndorsementRequest(2, LocalDate.parse("2026-12-04"), "REBUILD_AMOUNT", new BigDecimal("300000.00")));
        assertEquals(3, changed.version());
        assertEquals(List.of(prior), changed.endorsements().subList(0, 1));
        assertEquals(2, changed.endorsements().size());
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
                var first = pool.submit(() -> raceEndorse(ready, start, dataSource, "2026-12-02", "200000.00"));
                var second = pool.submit(() -> raceEndorse(ready, start, dataSource, "2026-12-03", "300000.00"));
                assertTrue(ready.await(5, TimeUnit.SECONDS));
                start.countDown();
                Object firstResult = getRaceResult(first);
                Object secondResult = getRaceResult(second);
                assertEquals(1, (firstResult instanceof PolicySnapshot ? 1 : 0) + (secondResult instanceof PolicySnapshot ? 1 : 0), "exactly one writer wins version 1");
                assertEquals(1, (firstResult instanceof VersionConflictException ? 1 : 0) + (secondResult instanceof VersionConflictException ? 1 : 0), "the loser gets a stale-version conflict");
                assertEquals(2, scalarLong(dataSource, "SELECT version FROM training_policy_lifecycle WHERE policy_id='policy-a'"));
                assertEquals(2, count(dataSource, "training_policy_lifecycle_event"), "only issued and winning endorsement events exist");
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
                () -> assertEquals(PolicyStatus.CANCEL_SCHEDULED, service.scheduleCancellation(issued(),
                        new PolicyCancellationRequest(1, "cancel-today", LocalDate.parse("2026-11-19"), "CUSTOMER_REQUEST"), empty).status(), "same day is non-past under injected Clock"),
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

    @Test void day42CancelledPolicyRejectsWritesButRemainsReadable() {
        var cancelled = withStatus(PolicyStatus.CANCELLED);
        assertFalse(service.permitsServicingWrite(cancelled));
        PolicyReadStore store = (policyId, ownerId) -> Optional.of(cancelled);
        assertEquals(Optional.of(cancelled), service.readForOwner(store, "owner-a", "policy-a"));
    }

    @Test void day43ServicingProjectionOmitsOwnerJourneyAndQuoteIds() {
        var view = service.toServicingView(issued());
        assertEquals("policy-a", view.policyId());
        assertEquals("INR", view.currency());
        assertNull(findField(view.getClass(), "ownerId"));
        assertNull(findField(view.getClass(), "journeyId"));
        assertNull(findField(view.getClass(), "quoteVersionId"));
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

    private static PolicyIssueRequest issueRequest() {
        return new PolicyIssueRequest("journey-a", "quote-a", "req-a", LocalDate.parse("2026-12-01"));
    }
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
                                               String effectiveDate, String amount) throws Exception {
        ready.countDown(); if (!start.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("race start timed out");
        return serviceStatic().endorseAtExpectedVersion(dataSource, issued(),
                new PolicyEndorsementRequest(1, LocalDate.parse(effectiveDate), "REBUILD_AMOUNT", new BigDecimal(amount)));
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
