package coverstory.modules.payments;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/** Independent fixture oracle and defect witnesses; no provider and no learner method. */
class CAP76InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-76/synthetic-v1.json");

    @Test void fixtureDeclaresSyntheticStatusReadOnlyPolicyAndSavedCases() throws Exception {
        JsonNode root = fixture();
        assertEquals("SYNTHETIC_OFFLINE_POLICY", root.path("classification").asText());
        assertFalse(root.path("actual_provider_call").asBoolean());
        assertEquals("idempotent mock-provider status GET only", root.path("scope").asText());
        assertEquals(1000, root.path("deadline_ms").asInt());
        assertEquals(3, root.path("maximum_attempts").asInt());
        assertEquals(6, root.path("cases").size());
    }

    @Test void independentReferenceAcceptsEverySavedOutcome() throws Exception {
        JsonNode c1 = caseById("CAP-76-CS-01");
        assertExpected(c1.path("input"), c1.path("expected"));
        JsonNode c2 = caseById("CAP-76-CS-02");
        assertExpected(c2.path("input"), c2.path("expected"));
        for (JsonNode variant : caseById("CAP-76-CS-03").path("variants"))
            assertExpected(variant.path("input"), variant.path("expected"));
        JsonNode c4 = caseById("CAP-76-CS-04");
        assertExpected(c4.path("input"), c4.path("expected"));
        JsonNode c5 = caseById("CAP-76-CS-05");
        assertExpected(c5.path("input"), c5.path("expected"));
        JsonNode c6 = caseById("CAP-76-CS-06").path("status_read");
        assertExpected(c6.path("input"), c6.path("expected"));
        JsonNode money = caseById("CAP-76-CS-06").path("possible_capture_timeout");
        assertEquals("UNKNOWN_REQUIRES_RECONCILIATION", money.path("payment_state").asText());
        assertFalse(money.path("automatic_charge_retry").asBoolean());
        assertTrue(money.path("resume_with_status_read").asBoolean());
    }

    @Test void negativeWitnessesCatchInclusiveDeadlineIgnoredHeaderAndSuccessErasure() {
        Decision exactDeadline = referenceNext(2, 503, 800, null);
        assertEquals("STOP_EXHAUSTED", exactDeadline.action, "elapsed + delay must be strictly below 1000ms");

        Decision headerIgnored = referenceNext(1, 503, 0, 250L);
        assertEquals("RETRY_AFTER", headerIgnored.action);
        assertEquals(250L, headerIgnored.delayMs, "Retry-After is a lower bound, not an instruction to retry earlier");

        Decision prematureRetry = deliberatelyDefectiveAttemptOnly(1, 503, 0);
        assertEquals("RETRY_AFTER", prematureRetry.action);
        assertNotEquals(referenceNext(1, 503, 0, 250L), prematureRetry,
                "attempt-only backoff ignores Retry-After and is rejected by this witness");

        Decision lateKnownSuccess = referenceNext(3, 200, 1500, null);
        assertEquals("STOP_SUCCESS", lateKnownSuccess.action);
        assertTrue(lateKnownSuccess.responseRetained, "success precedence retains the known response after deadline");
        Decision erasedSuccess = deliberatelyDefectiveDeadlineFirst(3, 200, 1500);
        assertNotEquals(lateKnownSuccess, erasedSuccess, "deadline-first logic loses a known success");

        Decision longDelay = referenceNext(1, 429, 0, 31_000L);
        assertEquals("DEFER_STATUS_READ", longDelay.action);
        assertEquals(31_000L, longDelay.delayMs);
    }

    private static void assertExpected(JsonNode input, JsonNode expected) {
        Long retryAfter = input.path("retry_after_ms").isNull() ? null : input.path("retry_after_ms").asLong();
        Decision actual = referenceNext(input.path("attempt_number").asInt(), input.path("http_status").asInt(),
                input.path("elapsed_ms").asLong(), retryAfter);
        assertEquals(expected.path("action").asText(), actual.action);
        JsonNode delay = expected.path("delay_ms");
        assertEquals(delay.isNull() ? null : delay.asLong(), actual.delayMs);
        assertEquals(expected.path("response_retained").asBoolean(), actual.responseRetained);
        if (expected.has("response_status")) assertEquals(expected.path("response_status").asInt(), actual.responseStatus);
    }

    /** Independent instructor pseudocode encoded for deterministic reference checks. */
    private static Decision referenceNext(int attempt, int status, long elapsedMs, Long retryAfterMs) {
        if (status >= 200 && status < 300) return new Decision("STOP_SUCCESS", null, true, status);
        if (!(status == 408 || status == 429 || (status >= 500 && status <= 599)))
            return new Decision("STOP_PERMANENT", null, true, status);
        if (attempt < 1 || attempt >= 3) return Decision.stop("STOP_EXHAUSTED");
        long backoff = attempt == 1 ? 100 : 200;
        boolean hasLongHeader = retryAfterMs != null && retryAfterMs > backoff;
        long delay = Math.max(backoff, retryAfterMs == null ? 0 : Math.max(0, retryAfterMs));
        if (elapsedMs + delay >= 1000)
            return hasLongHeader ? new Decision("DEFER_STATUS_READ", delay, true, status) : Decision.stop("STOP_EXHAUSTED");
        return new Decision("RETRY_AFTER", delay, false, status);
    }

    /** Deliberately defective model: ignores Retry-After and uses only exponential delay. */
    private static Decision deliberatelyDefectiveAttemptOnly(int attempt, int status, long elapsed) {
        long delay = attempt == 1 ? 100 : 200;
        if (elapsed + delay >= 1000) return Decision.stop("STOP_EXHAUSTED");
        return new Decision("RETRY_AFTER", delay, false, status);
    }

    /** Deliberately defective model: lets the deadline hide a response already known to be successful. */
    private static Decision deliberatelyDefectiveDeadlineFirst(int attempt, int status, long elapsed) {
        if (elapsed >= 1000) return Decision.stop("STOP_EXHAUSTED");
        return referenceNext(attempt, status, elapsed, null);
    }

    private static JsonNode fixture() throws Exception { return JSON.readTree(Files.readString(FIXTURE)); }
    private static JsonNode caseById(String id) throws Exception {
        for (JsonNode row : fixture().path("cases")) if (id.equals(row.path("id").asText())) return row;
        throw new IllegalArgumentException("Fixture case not found: " + id);
    }

    private record Decision(String action, Long delayMs, boolean responseRetained, Integer responseStatus) {
        static Decision stop(String action) { return new Decision(action, null, false, null); }
    }
}
