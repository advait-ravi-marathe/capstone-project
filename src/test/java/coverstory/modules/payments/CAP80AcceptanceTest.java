package coverstory.modules.payments;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import coverstory.modules.payments.api.CAP80Seam;
import coverstory.modules.payments.api.CAP80Seam.OwnerPaymentItem;
import coverstory.modules.payments.api.CAP80Seam.PaymentCheckpoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP80AcceptanceTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-80/synthetic-v1.json");

    @Test void nextConfirmedSequenceAppliesCaptureOnboardingTimelineAndCheckpoint() throws Exception {
        JsonNode root = fixture(); JsonNode caseRow = caseById(root, "CAP-80-CS-01");
        UUID ownerA = id(root, "owner_a"), ownerB = id(root, "owner_b"), appA = id(root, "application_a");
        try (CAP80JdbcHarness db = new CAP80JdbcHarness(ownerA, appA, ownerB, UUID.fromString("b3000000-0000-4000-8000-000000000003"))) {
            var result = new CAP80Seam().applyConfirmation(item(root, caseRow, ownerA), checkpoint(ownerA, caseRow), db.port(CAP80JdbcHarness.Fault.NONE));
            assertEquals(CAP80Seam.Status.APPLIED, result.status());
            assertEquals(3, db.checkpoint(ownerA));
            assertEquals(1, db.mockCaptureCount(ownerA));
            assertEquals(1, db.onboardingCount(ownerA));
            assertEquals(1, db.timelineCount(ownerA));
            assertEquals("100000|INR", db.mockCaptureMoney(ownerA));
            assertEquals("ONBOARDING", db.state(appA));
        }
    }

    @Test void repeatingSequenceDoesNotCreateSecondMockEffectOrOnboardingTransition() throws Exception {
        JsonNode root = fixture(); JsonNode caseRow = caseById(root, "CAP-80-CS-02");
        UUID ownerA = id(root, "owner_a"), ownerB = id(root, "owner_b"), appA = id(root, "application_a");
        try (CAP80JdbcHarness db = new CAP80JdbcHarness(ownerA, appA, ownerB, UUID.fromString("b3000000-0000-4000-8000-000000000003"))) {
            var seam = new CAP80Seam(); var port = db.port(CAP80JdbcHarness.Fault.NONE);
            var first = seam.applyConfirmation(item(root, caseRow, ownerA), checkpoint(ownerA, caseRow), port);
            var repeated = seam.applyConfirmation(item(root, caseRow, ownerA), checkpoint(ownerA, caseRow), port);
            assertEquals(CAP80Seam.Status.APPLIED, first.status());
            assertEquals(CAP80Seam.Status.ALREADY_APPLIED, repeated.status());
            assertEquals(1, db.mockCaptureCount(ownerA));
            assertEquals(1, db.onboardingCount(ownerA));
            assertEquals(1, db.timelineCount(ownerA));
            assertEquals("100000|INR", db.mockCaptureMoney(ownerA));
        }
    }

    @Test void sequenceGapMakesNoWrites() throws Exception {
        JsonNode root = fixture(); JsonNode caseRow = caseById(root, "CAP-80-CS-03");
        UUID ownerA = id(root, "owner_a"), ownerB = id(root, "owner_b"), appA = id(root, "application_a");
        try (CAP80JdbcHarness db = new CAP80JdbcHarness(ownerA, appA, ownerB, UUID.fromString("b3000000-0000-4000-8000-000000000003"))) {
            var result = new CAP80Seam().applyConfirmation(item(root, caseRow, ownerA), checkpoint(ownerA, caseRow), db.port(CAP80JdbcHarness.Fault.NONE));
            assertEquals(CAP80Seam.Status.GAP, result.status());
            assertEquals(2, db.checkpoint(ownerA)); assertEquals(0, db.mockCaptureCount(ownerA));
            assertEquals(0, db.onboardingCount(ownerA)); assertEquals(0, db.timelineCount(ownerA));
        }
    }

    @Test void failureAfterEffectAndApplicationWriteRollsBackAndRetryAppliesOnce() throws Exception {
        JsonNode root = fixture(); JsonNode caseRow = caseById(root, "CAP-80-CS-04");
        UUID ownerA = id(root, "owner_a"), ownerB = id(root, "owner_b"), appA = id(root, "application_a");
        try (CAP80JdbcHarness db = new CAP80JdbcHarness(ownerA, appA, ownerB, UUID.fromString("b3000000-0000-4000-8000-000000000003"))) {
            var seam = new CAP80Seam();
            RuntimeException failure = assertThrows(RuntimeException.class, () -> seam.applyConfirmation(item(root, caseRow, ownerA),
                    checkpoint(ownerA, caseRow), db.port(CAP80JdbcHarness.Fault.BEFORE_CHECKPOINT)));
            if (failure instanceof UnsupportedOperationException && failure.getMessage() != null
                    && failure.getMessage().contains("CAP-80 learner TODO")) throw failure;
            assertInstanceOf(IllegalStateException.class, failure);
            assertEquals(2, db.checkpoint(ownerA)); assertEquals(0, db.mockCaptureCount(ownerA));
            assertEquals(0, db.onboardingCount(ownerA)); assertEquals(0, db.timelineCount(ownerA));
            assertEquals("AWAITING_PAYMENT", db.state(appA));
            var retry = seam.applyConfirmation(item(root, caseRow, ownerA), checkpoint(ownerA, caseRow), db.port(CAP80JdbcHarness.Fault.NONE));
            assertEquals(CAP80Seam.Status.APPLIED, retry.status());
            assertEquals(3, db.checkpoint(ownerA)); assertEquals(1, db.mockCaptureCount(ownerA));
            assertEquals(1, db.onboardingCount(ownerA)); assertEquals(1, db.timelineCount(ownerA));
        }
    }

    @Test void foreignFeedIsForbiddenBeforeTransactionOrCheckpointRead() throws Exception {
        JsonNode root = fixture(); JsonNode caseRow = caseById(root, "CAP-80-CS-05");
        UUID ownerA = id(root, "owner_a"), ownerB = id(root, "owner_b"), appA = id(root, "application_a");
        try (CAP80JdbcHarness db = new CAP80JdbcHarness(ownerA, appA, ownerB, UUID.fromString("b3000000-0000-4000-8000-000000000003"))) {
            var result = new CAP80Seam().applyConfirmation(item(root, caseRow, ownerA), checkpoint(ownerB, caseRow), db.port(CAP80JdbcHarness.Fault.NONE));
            assertEquals(CAP80Seam.Status.FORBIDDEN, result.status());
            assertEquals(0, db.transactionCalls());
            assertEquals(0, db.checkpointReads(ownerB));
            assertEquals(0, db.mockCaptureCount(ownerB)); assertEquals(0, db.onboardingCount(ownerB));
        }
    }

    private static JsonNode fixture() throws Exception { return JSON.readTree(Files.readString(FIXTURE)); }

    private static JsonNode caseById(JsonNode root, String id) {
        for (JsonNode row : root.path("cases")) if (id.equals(row.path("id").asText())) return row;
        throw new IllegalArgumentException("fixture case not found: " + id);
    }

    private static JsonNode feedItem(JsonNode root, String eventId) {
        for (JsonNode row : root.path("confirmed_feed")) if (eventId.equals(row.path("event_id").asText())) return row;
        throw new IllegalArgumentException("fixture feed item not found: " + eventId);
    }

    private static OwnerPaymentItem item(JsonNode root, JsonNode caseRow, UUID actor) {
        JsonNode feed = feedItem(root, caseRow.path("feed_event").asText());
        return new OwnerPaymentItem(actor, UUID.fromString(feed.path("owner_id").asText()), feed.path("event_id").asText(),
                feed.path("sequence").asInt(), UUID.fromString(feed.path("application_id").asText()),
                feed.path("quote_id").asText(), feed.path("amount_minor").asLong(), feed.path("currency").asText(),
                feed.path("confirmed").asBoolean());
    }

    private static PaymentCheckpoint checkpoint(UUID owner, JsonNode row) { return new PaymentCheckpoint(owner, row.path("checkpoint").asInt()); }
    private static UUID id(JsonNode root, String key) { return UUID.fromString(root.path(key).asText()); }
}
