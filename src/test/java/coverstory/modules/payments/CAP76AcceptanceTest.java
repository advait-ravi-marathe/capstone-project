package coverstory.modules.payments;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import coverstory.modules.payments.api.CAP76Seam;
import coverstory.modules.payments.api.CAP76Seam.Action;
import coverstory.modules.payments.api.CAP76Seam.RetryDecision;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP76AcceptanceTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-76/synthetic-v1.json");
    private final CAP76Seam seam = new CAP76Seam();

    @Test void firstTransientAttemptUsesOneHundredMillisecondBackoff() throws Exception {
        assertDecision(caseById("CAP-76-CS-01").path("input"), caseById("CAP-76-CS-01").path("expected"));
    }

    @Test void secondThrottledAttemptUsesTwoHundredMillisecondBackoff() throws Exception {
        assertDecision(caseById("CAP-76-CS-02").path("input"), caseById("CAP-76-CS-02").path("expected"));
    }

    @Test void exhaustedAttemptAndInsufficientTimeBothStop() throws Exception {
        for (JsonNode variant : caseById("CAP-76-CS-03").path("variants"))
            assertDecision(variant.path("input"), variant.path("expected"));
    }

    @Test void forbiddenStatusIsPermanent() throws Exception {
        JsonNode row = caseById("CAP-76-CS-04");
        assertDecision(row.path("input"), row.path("expected"));
    }

    @Test void knownSuccessIsRetainedEvenAfterDeadline() throws Exception {
        JsonNode row = caseById("CAP-76-CS-05");
        RetryDecision decision = decision(row.path("input"));
        assertEquals(Action.STOP_SUCCESS, decision.action());
        assertTrue(decision.responseRetained());
        assertEquals(200, decision.responseStatus());
    }

    @Test void longRetryAfterDefersStatusReadAndCaptureUncertaintyRequiresReconciliation() throws Exception {
        JsonNode row = caseById("CAP-76-CS-06");
        JsonNode read = row.path("status_read");
        assertDecision(read.path("input"), read.path("expected"));
        JsonNode money = row.path("possible_capture_timeout");
        assertEquals("UNKNOWN_REQUIRES_RECONCILIATION", money.path("payment_state").asText());
        assertFalse(money.path("automatic_charge_retry").asBoolean());
        assertTrue(money.path("resume_with_status_read").asBoolean());
    }

    @Test void retryMustFitStrictlyInsideDeadlineAndNeverEarlierThanRetryAfter() {
        RetryDecision exactDeadline = seam.next(2, 503, Duration.ofMillis(800), Duration.ZERO);
        assertEquals(Action.STOP_EXHAUSTED, exactDeadline.action());
        RetryDecision honorsHeader = seam.next(1, 503, Duration.ZERO, Duration.ofMillis(250));
        assertEquals(Action.RETRY_AFTER, honorsHeader.action());
        assertEquals(Duration.ofMillis(250), honorsHeader.delay());
    }

    private void assertDecision(JsonNode input, JsonNode expected) {
        RetryDecision actual = decision(input);
        assertEquals(expected.path("action").asText(), actual.action().name());
        JsonNode delay = expected.path("delay_ms");
        assertEquals(delay.isNull() ? null : Duration.ofMillis(delay.asLong()), actual.delay());
        assertEquals(expected.path("response_retained").asBoolean(), actual.responseRetained());
        if (expected.has("response_status")) assertEquals(expected.path("response_status").asInt(), actual.responseStatus());
    }

    private RetryDecision decision(JsonNode input) {
        JsonNode header = input.path("retry_after_ms");
        Duration parsedRetryAfter = header.isNull() ? null : Duration.ofMillis(header.asLong());
        return seam.next(input.path("attempt_number").asInt(), input.path("http_status").asInt(),
                Duration.ofMillis(input.path("elapsed_ms").asLong()), parsedRetryAfter);
    }

    private static JsonNode caseById(String id) throws Exception {
        for (JsonNode row : JSON.readTree(Files.readString(FIXTURE)).path("cases"))
            if (id.equals(row.path("id").asText())) return row;
        throw new IllegalArgumentException("Fixture case not found: " + id);
    }
}
