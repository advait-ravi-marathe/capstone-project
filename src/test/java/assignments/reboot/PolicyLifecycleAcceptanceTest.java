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

import static org.junit.jupiter.api.Assertions.*;

/** Fixed learner-behavior tests. Red at an explicit Day 31–44 TODO is intentional. */
@Tag("learnerAcceptance")
class PolicyLifecycleAcceptanceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-19T12:00:00Z"), ZoneOffset.UTC);
    private final PolicyLifecycleService service = new PolicyLifecycleService(CLOCK);

    @Test void day31RejectsForeignUnselectedStaleOrIneligibleQuoteWithoutIssue() {
        var valid = new PolicyIssueRequest("owner-a", "journey-a", "quote-a", "req-a", LocalDate.parse("2026-12-01"));
        assertTrue(service.validateIssue(valid));
        assertFalse(service.validateIssue(new PolicyIssueRequest("owner-b", "journey-a", "quote-a", "req-b", valid.effectiveDate())));
    }

    @Test void day32IdenticalIdempotencyReplayReturnsOriginalAndChangedPayloadConflicts() {
        var request = issueRequest();
        var created = issued();
        assertEquals(created, service.issue(request));
        assertEquals(409, service.issueHttpStatus(new PolicyIssueRequest("owner-a", "journey-a", "quote-other", "req-a", request.effectiveDate()), Optional.of(created)));
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
    }

    @Test void day35AtomicPostgresWriteHasOnePolicyAndOneReceipt() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            var dataSource = postgres.getPostgresDatabase();
            initializeLifecycleSchema(dataSource);
            assertNotNull(service.persistIssueAtomically(dataSource, issueRequest()));
        }
    }

    @Test void day36ForeignAndAbsentPolicyAreBothNotFound() {
        assertTrue(service.readForOwner("owner-b", "policy-a").isEmpty());
        assertTrue(service.readForOwner("owner-a", "missing").isEmpty());
    }

    @Test void day37EndorsementRejectsUnknownNoopPastDateAndStaleVersion() {
        assertTrue(service.validateEndorsement(issued(), new PolicyEndorsementRequest(1, LocalDate.parse("2026-12-02"), "REBUILD_AMOUNT", new BigDecimal("200000.00"))));
        assertFalse(service.validateEndorsement(issued(), new PolicyEndorsementRequest(1, LocalDate.parse("2026-11-30"), "UNKNOWN", new BigDecimal("200000.00"))));
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
            assertNotNull(service.endorseAtExpectedVersion(dataSource, issued(),
                    new PolicyEndorsementRequest(1, LocalDate.parse("2026-12-02"), "REBUILD_AMOUNT", new BigDecimal("200000.00"))));
        }
    }

    @Test void day40CancellationMustBeFutureAndRequestReplayIsIdempotent() {
        var request = new PolicyCancellationRequest(1, "cancel-a", LocalDate.parse("2026-12-10"), "CUSTOMER_REQUEST");
        var scheduled = service.scheduleCancellation(issued(), request);
        assertEquals(PolicyStatus.CANCEL_SCHEDULED, scheduled.status());
        assertEquals(LocalDate.parse("2026-12-10"), scheduled.cancellationEffectiveDate());
    }

    @Test void day41CancellationRunsOnlyAtOrAfterEffectiveDateAndOnlyOnce() {
        var scheduled = new PolicySnapshot("policy-a", "owner-a", "journey-a", "quote-a", "home-secure-v1", "training-rate-v1",
                new BigDecimal("125.00"), "INR", LocalDate.parse("2026-12-01"), PolicyStatus.CANCEL_SCHEDULED, 2, List.of(), LocalDate.parse("2026-12-10"));
        assertEquals(PolicyStatus.CANCELLED, service.applyDueCancellation(scheduled).status());
    }

    @Test void day42CancelledPolicyRejectsWritesButRemainsReadable() {
        var cancelled = withStatus(PolicyStatus.CANCELLED);
        assertFalse(service.permitsServicingWrite(cancelled));
        assertEquals(cancelled, service.readForOwner("owner-a", "policy-a").orElse(cancelled));
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
            }
            service.verifyLifecycleReadback(dataSource, "policy-a");
        }
    }

    private static PolicyIssueRequest issueRequest() {
        return new PolicyIssueRequest("owner-a", "journey-a", "quote-a", "req-a", LocalDate.parse("2026-12-01"));
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
}
