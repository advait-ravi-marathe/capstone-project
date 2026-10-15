package coverstory.modules.payments;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import coverstory.modules.payments.api.CAP74Seam;
import coverstory.modules.payments.api.CAP74Seam.AttemptSnapshot;
import coverstory.modules.payments.api.CAP74Seam.Principal;
import coverstory.modules.payments.api.CAP74Seam.SafeStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP74AcceptanceTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-74/synthetic-v1.json");

    @Test void ownedPendingAttemptReturnsOnlySafePaymentStatus() throws Exception {
        JsonNode row = caseById("CAP-74-CS-01");
        AttemptSnapshot snapshot = attempt(row.path("attempt"));
        AtomicInteger lookups = new AtomicInteger();
        CAP74Seam seam = new CAP74Seam((owner, id) -> {
            lookups.incrementAndGet();
            assertEquals(snapshot.ownerId(), owner);
            assertEquals(snapshot.attemptId(), id);
            return Optional.of(snapshot);
        });

        CAP74Seam.Reply reply = seam.getStatus(new Principal(snapshot.ownerId()), snapshot.attemptId());

        assertEquals(200, reply.status());
        assertNull(reply.errorCode());
        assertEquals("PENDING", reply.body().state());
        assertEquals(CAP74Seam.NextAction.NONE, reply.body().nextAction());
        assertEquals("req-synth-001", reply.body().requestId());
        assertEquals(1, lookups.get());
        JsonNode safe = JSON.valueToTree(reply.body());
        assertEquals(5, safe.size());
        for (JsonNode forbidden : fixture().path("forbidden_response_fields")) assertFalse(safe.has(forbidden.asText()));
        assertFalse(safe.toString().contains("op-private-A"));
    }

    @Test void ownedUnknownRetainsSameOperationForReconciliationWithoutCreatingAttempt() throws Exception {
        JsonNode row = caseById("CAP-74-CS-02");
        AttemptSnapshot snapshot = attempt(row.path("attempt"));
        AtomicInteger reads = new AtomicInteger();
        CAP74Seam seam = new CAP74Seam((owner, id) -> {
            reads.incrementAndGet();
            assertEquals(snapshot.operationReference(), "op-private-unknown-A");
            return Optional.of(snapshot);
        });

        CAP74Seam.Reply reply = seam.getStatus(new Principal(snapshot.ownerId()), snapshot.attemptId());

        assertEquals(200, reply.status());
        assertEquals("UNKNOWN", reply.body().state());
        assertEquals(CAP74Seam.NextAction.RECONCILE, reply.body().nextAction());
        assertEquals("req-synth-002", reply.body().requestId());
        assertEquals(1, reads.get());
    }

    @Test void foreignAndMissingAttemptsHaveIdenticalNotFoundResponses() throws Exception {
        JsonNode row = caseById("CAP-74-CS-03");
        AttemptSnapshot foreign = attempt(row.path("foreign_attempt"));
        UUID ownerA = UUID.fromString(row.path("principal").asText());
        CAP74Seam seam = new CAP74Seam((owner, id) -> owner.equals(foreign.ownerId()) && id.equals(foreign.attemptId())
                ? Optional.of(foreign) : Optional.empty());

        CAP74Seam.Reply foreignReply = seam.getStatus(new Principal(ownerA), foreign.attemptId());
        CAP74Seam.Reply missingReply = seam.getStatus(new Principal(ownerA), UUID.fromString(row.path("missing_attempt_id").asText()));

        assertEquals(foreignReply, missingReply);
        assertEquals(row.path("expected").path("status").asInt(), foreignReply.status());
        assertEquals(row.path("expected").path("errorCode").asText(), foreignReply.errorCode());
        assertNull(foreignReply.body());
    }

    @Test void anonymousAndImpossiblePersistedStateFailClosed() throws Exception {
        JsonNode row = caseById("CAP-74-CS-04");
        AttemptSnapshot invalid = attempt(row.path("impossible_state_attempt"));
        CAP74Seam seam = new CAP74Seam((owner, id) -> Optional.of(invalid));

        CAP74Seam.Reply anonymous = seam.getStatus(null, invalid.attemptId());
        CAP74Seam.Reply invalidState = seam.getStatus(new Principal(invalid.ownerId()), invalid.attemptId());

        assertEquals(401, anonymous.status());
        assertEquals("UNAUTHORIZED", anonymous.errorCode());
        assertNull(anonymous.body());
        assertEquals(500, invalidState.status());
        assertEquals("PAYMENT_STATE_INVALID", invalidState.errorCode());
        assertNull(invalidState.body());
    }

    private static JsonNode fixture() throws Exception { return JSON.readTree(Files.readString(FIXTURE)); }

    private static JsonNode caseById(String id) throws Exception {
        for (JsonNode row : fixture().path("cases")) if (id.equals(row.path("id").asText())) return row;
        throw new IllegalArgumentException("Fixture case not found: " + id);
    }

    private static AttemptSnapshot attempt(JsonNode json) {
        return new AttemptSnapshot(UUID.fromString(json.path("attemptId").asText()),
                UUID.fromString(json.path("ownerId").asText()), json.path("state").asText(),
                Instant.parse(json.path("updatedAt").asText()), json.path("requestId").asText(),
                json.path("operationReference").asText());
    }
}
