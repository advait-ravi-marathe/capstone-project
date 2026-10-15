package coverstory.modules.payments;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import coverstory.modules.payments.api.CAP104Seam;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP104AcceptanceTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-104/synthetic-v1.json");
    private static final Instant START = Instant.parse("2026-10-14T10:00:00Z");
    private static final Instant END = Instant.parse("2026-10-14T10:05:00Z");
    private static CAP104Seam seam() { return new CAP104Seam(); }
    private static CAP104Seam.Window window() { return new CAP104Seam.Window(START, END, Duration.ofMinutes(2)); }
    private static CAP104Seam.Summary initial() { return new CAP104Seam.Summary(2, 1, Set.of("obs-base-1", "obs-base-2")); }
    private static CAP104Seam.Observation observation(String id, String arrival, boolean unknown) {
        return new CAP104Seam.Observation(id, Instant.parse("2026-10-14T10:04:00Z"), Instant.parse(arrival), unknown);
    }

    @Test void lateObservationRevisesSummaryWithoutAnyMoneyOrDispatchEffect() throws Exception {
        JsonNode expected = expected("CAP-104-CS-01");
        var result = seam().reviseSummary(window(), initial(), observation("late-payment-1", "2026-10-14T10:06:00Z", false));
        assertEquals(expected.path("count").asLong(), result.summary().count());
        assertEquals(expected.path("revision").asLong(), result.summary().revision());
        assertEquals("REVISED", result.outcome().name());
        assertNoEffects(result);
    }

    @Test void repeatedLateIdIsCountedAsDuplicateAndDoesNotReviseAgain() throws Exception {
        var first = seam().reviseSummary(window(), initial(), observation("late-payment-1", "2026-10-14T10:06:00Z", false));
        JsonNode expected = expected("CAP-104-CS-02");
        var repeated = seam().reviseSummary(window(), first.summary(), observation("late-payment-1", "2026-10-14T10:06:00Z", false));
        assertEquals("DUPLICATE", repeated.outcome().name());
        assertEquals(expected.path("count").asLong(), repeated.summary().count());
        assertEquals(expected.path("revision").asLong(), repeated.summary().revision());
        assertEquals(1, repeated.duplicateCount());
        assertNoEffects(repeated);
    }

    @Test void beyondGraceIsQuarantinedAndLeavesSummaryUnchanged() throws Exception {
        JsonNode expected = expected("CAP-104-CS-03");
        var result = seam().reviseSummary(window(), initial(), observation("late-payment-2", "2026-10-14T10:08:00Z", false));
        assertEquals("TOO_LATE", result.outcome().name());
        assertEquals(expected.path("count").asLong(), result.summary().count());
        assertEquals(expected.path("revision").asLong(), result.summary().revision());
        assertEquals(1, result.quarantinedCount());
        assertNoEffects(result);
    }

    @Test void graceDeadlineIsInclusiveToTheMicrosecond() throws Exception {
        var eligible = seam().reviseSummary(window(), initial(), observation("at-deadline", "2026-10-14T10:07:00Z", false));
        var late = seam().reviseSummary(window(), initial(), observation("after-deadline", "2026-10-14T10:07:00.000001Z", false));
        assertEquals("REVISED", eligible.outcome().name());
        assertEquals(3, eligible.summary().count());
        assertEquals("TOO_LATE", late.outcome().name());
        assertEquals(2, late.summary().count());
        assertNoEffects(eligible); assertNoEffects(late);
    }

    @Test void unknownPaymentEvidenceOnlyReferencesExistingReconciliation() throws Exception {
        JsonNode expected = expected("CAP-104-CS-06");
        var result = seam().reviseSummary(window(), initial(), observation("unknown-payment-event", "2026-10-14T10:06:00Z", true));
        assertEquals("RECONCILIATION_REQUIRED", result.paymentDisposition().name());
        assertEquals(expected.path("count").asLong(), result.summary().count());
        assertEquals(expected.path("revision").asLong(), result.summary().revision());
        assertNoEffects(result);
    }

    private static void assertNoEffects(CAP104Seam.Result result) {
        assertEquals(0, result.chargeCalls()); assertEquals(0, result.refundCalls()); assertEquals(0, result.dispatchCalls());
    }
    private static JsonNode expected(String id) throws Exception {
        for (JsonNode row : JSON.readTree(Files.readString(FIXTURE)).path("cases"))
            if (id.equals(row.path("id").asText())) return row.path("expected");
        throw new IllegalArgumentException("Fixture case missing: " + id);
    }
}
