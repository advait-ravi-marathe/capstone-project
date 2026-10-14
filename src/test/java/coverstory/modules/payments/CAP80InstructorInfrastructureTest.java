package coverstory.modules.payments;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import coverstory.modules.payments.api.CAP80Seam;
import coverstory.modules.payments.api.CAP80Seam.OwnerPaymentItem;
import coverstory.modules.payments.api.CAP80Seam.PaymentCheckpoint;
import coverstory.modules.payments.api.CAP80Seam.PaymentEffectPort;
import coverstory.modules.payments.api.CAP80Seam.ReplayResult;
import coverstory.modules.payments.api.CAP80Seam.ReplayTransaction;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Independent transaction oracle using embedded PostgreSQL and fixture-only mock rows. */
class CAP80InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-80/synthetic-v1.json");

    @Test void fixtureAndEmbeddedPostgresScaffoldAreSyntheticAndRunnable() throws Exception {
        JsonNode root = fixture();
        assertEquals("SYNTHETIC_MOCK_PAYMENT_REPLAY", root.path("classification").asText());
        assertFalse(root.path("actual_provider_call").asBoolean());
        assertEquals(100000L, root.path("amount_minor").asLong());
        assertEquals("INR", root.path("currency").asText());
        assertEquals(4, java.util.stream.StreamSupport.stream(root.path("confirmed_feed").spliterator(), false)
                .filter(row -> row.path("owner_id").asText().equals(root.path("owner_a").asText())).count());
        assertEquals(1, java.util.stream.StreamSupport.stream(root.path("confirmed_feed").spliterator(), false)
                .filter(row -> row.path("owner_id").asText().equals(root.path("owner_b").asText())).count());
        assertEquals(5, root.path("cases").size());
        UUID ownerA = id(root, "owner_a"), ownerB = id(root, "owner_b");
        try (CAP80JdbcHarness db = db(root)) {
            assertEquals(2, db.checkpoint(ownerA)); assertEquals(2, db.checkpoint(ownerB));
            assertEquals("AWAITING_PAYMENT", db.state(id(root, "application_a")));
            assertEquals(0, db.mockCaptureCount(ownerA));
        }
    }

    @Test void independentReferenceAcceptsAllFiveContractCasesAgainstPostgres() throws Exception {
        JsonNode root = fixture();
        JsonNode c1 = caseById(root, "CAP-80-CS-01");
        try (CAP80JdbcHarness db = db(root)) {
            ReplayResult result = referenceApply(item(root, c1, id(root, "owner_a")), checkpoint(c1, id(root, "owner_a")), db.port(CAP80JdbcHarness.Fault.NONE));
            assertEquals(CAP80Seam.Status.APPLIED, result.status());
            assertState(db, id(root, "owner_a"), id(root, "application_a"), 3, "ONBOARDING", 1, 1, 1);
            assertEquals("100000|INR", db.mockCaptureMoney(id(root, "owner_a")));
        }

        JsonNode c2 = caseById(root, "CAP-80-CS-02");
        try (CAP80JdbcHarness db = db(root)) {
            OwnerPaymentItem item = item(root, c2, id(root, "owner_a")); PaymentCheckpoint cp = checkpoint(c2, id(root, "owner_a"));
            assertEquals(CAP80Seam.Status.APPLIED, referenceApply(item, cp, db.port(CAP80JdbcHarness.Fault.NONE)).status());
            ReplayResult duplicate = referenceApply(item, cp, db.port(CAP80JdbcHarness.Fault.NONE));
            assertEquals(CAP80Seam.Status.ALREADY_APPLIED, duplicate.status());
            assertState(db, id(root, "owner_a"), id(root, "application_a"), 3, "ONBOARDING", 1, 1, 1);
            assertEquals("100000|INR", db.mockCaptureMoney(id(root, "owner_a")));
        }

        JsonNode c3 = caseById(root, "CAP-80-CS-03");
        try (CAP80JdbcHarness db = db(root)) {
            assertEquals(CAP80Seam.Status.GAP, referenceApply(item(root, c3, id(root, "owner_a")), checkpoint(c3, id(root, "owner_a")), db.port(CAP80JdbcHarness.Fault.NONE)).status());
            assertState(db, id(root, "owner_a"), id(root, "application_a"), 2, "AWAITING_PAYMENT", 0, 0, 0);
        }

        JsonNode c4 = caseById(root, "CAP-80-CS-04");
        try (CAP80JdbcHarness db = db(root)) {
            OwnerPaymentItem item = item(root, c4, id(root, "owner_a")); PaymentCheckpoint cp = checkpoint(c4, id(root, "owner_a"));
            assertThrows(IllegalStateException.class, () -> referenceApply(item, cp, db.port(CAP80JdbcHarness.Fault.BEFORE_CHECKPOINT)));
            assertState(db, id(root, "owner_a"), id(root, "application_a"), 2, "AWAITING_PAYMENT", 0, 0, 0);
            assertEquals(CAP80Seam.Status.APPLIED, referenceApply(item, cp, db.port(CAP80JdbcHarness.Fault.NONE)).status());
            assertState(db, id(root, "owner_a"), id(root, "application_a"), 3, "ONBOARDING", 1, 1, 1);
        }

        JsonNode c5 = caseById(root, "CAP-80-CS-05");
        try (CAP80JdbcHarness db = db(root)) {
            ReplayResult forbidden = referenceApply(item(root, c5, id(root, "owner_a")), checkpoint(c5, id(root, "owner_b")), db.port(CAP80JdbcHarness.Fault.NONE));
            assertEquals(CAP80Seam.Status.FORBIDDEN, forbidden.status());
            assertEquals(0, db.transactionCalls());
            assertEquals(0, db.checkpointReads(id(root, "owner_b")));
            assertEquals(0, db.mockCaptureCount(id(root, "owner_b")));
            assertEquals(0, db.onboardingCount(id(root, "owner_b")));
        }
    }

    @Test void independentStateOracleRejectsPrematureCheckpointAndDuplicateEffectDefects() throws Exception {
        Snapshot expectedRolledBack = new Snapshot(2, "AWAITING_PAYMENT", 0, 0, 0);
        assertTrue(acceptsRollback(expectedRolledBack), "real rollback state is accepted");
        assertFalse(acceptsRollback(new Snapshot(3, "AWAITING_PAYMENT", 0, 0, 0)),
                "advancing the cursor before its corresponding effects is rejected");
        assertFalse(acceptsApplied(new Snapshot(3, "ONBOARDING", 2, 1, 1)),
                "a second capture for one onboarding transition is rejected");
    }

    /** Instructor-only reference; application code intentionally remains a TODO. */
    private static ReplayResult referenceApply(OwnerPaymentItem item, PaymentCheckpoint supplied,
                                               PaymentEffectPort port) {
        if (item == null || supplied == null || port == null || !item.confirmed()
                || item.actorOwnerId() == null || !item.actorOwnerId().equals(item.feedOwnerId())
                || !item.feedOwnerId().equals(supplied.ownerId()))
            return new ReplayResult(CAP80Seam.Status.FORBIDDEN, supplied == null ? 0 : supplied.lastAppliedSequence());
        return port.inUnitOfWork(item.feedOwnerId(), (ReplayTransaction tx) -> {
            PaymentCheckpoint current = tx.lockCheckpoint(item.feedOwnerId());
            if (current == null || !current.ownerId().equals(item.feedOwnerId()))
                return new ReplayResult(CAP80Seam.Status.FORBIDDEN, 0);
            if (item.sequence() <= current.lastAppliedSequence())
                return new ReplayResult(CAP80Seam.Status.ALREADY_APPLIED, current.lastAppliedSequence());
            if (item.sequence() != current.lastAppliedSequence() + 1)
                return new ReplayResult(CAP80Seam.Status.GAP, current.lastAppliedSequence());
            tx.appendMockCapture(item);
            tx.unlockOnboarding(item.feedOwnerId(), item);
            tx.advanceCheckpoint(item.feedOwnerId(), item.sequence());
            return new ReplayResult(CAP80Seam.Status.APPLIED, item.sequence());
        });
    }

    private record Snapshot(int checkpoint, String state, int captures, int onboarding, int timeline) { }
    private static boolean acceptsRollback(Snapshot s) { return s.checkpoint() == 2 && "AWAITING_PAYMENT".equals(s.state()) && s.captures() == 0 && s.onboarding() == 0 && s.timeline() == 0; }
    private static boolean acceptsApplied(Snapshot s) { return s.checkpoint() == 3 && "ONBOARDING".equals(s.state()) && s.captures() == 1 && s.onboarding() == 1 && s.timeline() == 1; }

    private static void assertState(CAP80JdbcHarness db, UUID owner, UUID app, int checkpoint, String state,
                                    int captures, int onboarding, int timeline) throws Exception {
        Snapshot observed = new Snapshot(db.checkpoint(owner), db.state(app), db.mockCaptureCount(owner),
                db.onboardingCount(owner), db.timelineCount(owner));
        Snapshot expected = new Snapshot(checkpoint, state, captures, onboarding, timeline);
        assertEquals(expected, observed);
        if ("ONBOARDING".equals(state)) assertTrue(acceptsApplied(observed));
        if ("AWAITING_PAYMENT".equals(state) && captures == 0) assertTrue(acceptsRollback(observed));
    }

    private static CAP80JdbcHarness db(JsonNode root) throws Exception {
        return new CAP80JdbcHarness(id(root, "owner_a"), id(root, "application_a"), id(root, "owner_b"), UUID.fromString("b3000000-0000-4000-8000-000000000003"));
    }

    private static JsonNode fixture() throws Exception { return JSON.readTree(Files.readString(FIXTURE)); }
    private static JsonNode caseById(JsonNode root, String id) { for (JsonNode row : root.path("cases")) if (id.equals(row.path("id").asText())) return row; throw new IllegalArgumentException(id); }
    private static JsonNode feedItem(JsonNode root, String event) { for (JsonNode row : root.path("confirmed_feed")) if (event.equals(row.path("event_id").asText())) return row; throw new IllegalArgumentException(event); }
    private static UUID id(JsonNode root, String key) { return UUID.fromString(root.path(key).asText()); }
    private static OwnerPaymentItem item(JsonNode root, JsonNode caseRow, UUID actor) {
        JsonNode f = feedItem(root, caseRow.path("feed_event").asText());
        return new OwnerPaymentItem(actor, UUID.fromString(f.path("owner_id").asText()), f.path("event_id").asText(), f.path("sequence").asInt(), UUID.fromString(f.path("application_id").asText()), f.path("quote_id").asText(), f.path("amount_minor").asLong(), f.path("currency").asText(), f.path("confirmed").asBoolean());
    }
    private static PaymentCheckpoint checkpoint(JsonNode row, UUID owner) { return new PaymentCheckpoint(owner, row.path("checkpoint").asInt()); }
}
