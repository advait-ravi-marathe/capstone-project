package coverstory.modules.payments;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Independent reference checks for CAP-104. Does not call the learner seam. */
class CAP104InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-104/synthetic-v1.json");

    @Test void fixtureContainsAllFiveSavedAcceptanceCasesAndNoLiveProviderClaim() throws Exception {
        JsonNode root = JSON.readTree(Files.readString(FIXTURE));
        assertEquals("SYNTHETIC_OFFLINE_CONTRACT", root.path("classification").asText());
        assertFalse(root.path("actual_provider_run").asBoolean());
        assertEquals(6, root.path("cases").size()); // five cases, with both sides of the boundary split explicitly
        assertEquals("REVISED", byId(root, "CAP-104-CS-01").path("expected").path("outcome").asText());
        assertEquals("DUPLICATE", byId(root, "CAP-104-CS-02").path("expected").path("outcome").asText());
        assertEquals("TOO_LATE", byId(root, "CAP-104-CS-03").path("expected").path("outcome").asText());
        assertEquals("2026-10-14T10:07:00.000001Z", byId(root, "CAP-104-CS-05").path("arrival_time").asText());
        assertEquals("RECONCILIATION_REQUIRED", byId(root, "CAP-104-CS-06").path("expected").path("payment_disposition").asText());
    }

    @Test void independentReferenceSatisfiesRevisionDuplicateQuarantineAndInclusiveDeadline() throws Exception {
        JsonNode root = JSON.readTree(Files.readString(FIXTURE));
        JsonNode initial = root.path("initial_summary");
        Instant deadline = Instant.parse(root.path("window").path("end_exclusive").asText())
                .plusSeconds(root.path("window").path("grace_seconds").asLong());
        RefSummary base = new RefSummary(initial.path("count").asLong(), initial.path("revision").asLong(), ids(initial));

        RefResult accepted = reference(base, byId(root, "CAP-104-CS-01"), deadline);
        assertEquals("REVISED", accepted.outcome);
        assertEquals(3, accepted.summary.count);
        assertEquals(2, accepted.summary.revision);
        RefResult duplicate = reference(accepted.summary, byId(root, "CAP-104-CS-02"), deadline);
        assertEquals("DUPLICATE", duplicate.outcome);
        assertEquals(1, duplicate.duplicates);
        assertEquals(3, duplicate.summary.count);
        RefResult quarantined = reference(base, byId(root, "CAP-104-CS-03"), deadline);
        assertEquals("TOO_LATE", quarantined.outcome);
        assertEquals(base, quarantined.summary);
        assertEquals("REVISED", reference(base, byId(root, "CAP-104-CS-04"), deadline).outcome);
        assertEquals("TOO_LATE", reference(base, byId(root, "CAP-104-CS-05"), deadline).outcome);
    }

    @Test void oracleRejectsExclusiveBoundaryAndAnyPaymentEffectMutation() throws Exception {
        JsonNode root = JSON.readTree(Files.readString(FIXTURE));
        Instant deadline = Instant.parse(root.path("window").path("end_exclusive").asText()).plusSeconds(120);
        RefSummary base = new RefSummary(2, 1, Set.of("obs-base-1", "obs-base-2"));
        JsonNode boundary = byId(root, "CAP-104-CS-04");
        assertEquals("REVISED", reference(base, boundary, deadline).outcome);
        assertNotEquals("REVISED", referenceWithExclusiveGrace(base, boundary, deadline).outcome,
                "strict-before mutant must fail the inclusive deadline witness");

        RefResult correct = reference(base, byId(root, "CAP-104-CS-01"), deadline);
        assertMatches(byId(root, "CAP-104-CS-01").path("expected"), correct);
        RefResult chargeMutant = new RefResult(correct.summary, correct.outcome, correct.duplicates,
                correct.quarantined, 1, 0, 0, correct.disposition);
        assertThrows(AssertionError.class,
                () -> assertMatches(byId(root, "CAP-104-CS-01").path("expected"), chargeMutant),
                "independent expected-output comparison must reject a charge-effect mutant");
        assertEquals(base, reference(base, byId(root, "CAP-104-CS-03"), deadline).summary,
                "too-late analytics quarantine does not discard or mutate payment authority");
    }

    @Test void unknownMoneyEvidenceRoutesToExistingReconciliationWithoutEffects() throws Exception {
        JsonNode root = JSON.readTree(Files.readString(FIXTURE));
        Instant deadline = Instant.parse(root.path("window").path("end_exclusive").asText()).plusSeconds(120);
        RefResult result = reference(new RefSummary(2, 1, Set.of("obs-base-1", "obs-base-2")),
                byId(root, "CAP-104-CS-06"), deadline);
        assertEquals("RECONCILIATION_REQUIRED", result.disposition);
        assertEquals(0, result.chargeCalls + result.refundCalls + result.dispatchCalls);
        assertEquals(3, result.summary.count);
    }

    private static RefResult reference(RefSummary current, JsonNode row, Instant deadline) {
        return evaluate(current, row, deadline, true);
    }
    private static RefResult referenceWithExclusiveGrace(RefSummary current, JsonNode row, Instant deadline) {
        return evaluate(current, row, deadline, false);
    }
    private static RefResult evaluate(RefSummary current, JsonNode row, Instant deadline, boolean inclusive) {
        String id = row.path("event_id").asText();
        Instant arrival = Instant.parse(row.path("arrival_time").asText());
        String outcome;
        long duplicates = 0, quarantine = 0;
        RefSummary next = current;
        if (current.ids.contains(id)) { outcome = "DUPLICATE"; duplicates = 1; }
        else if (inclusive ? arrival.isAfter(deadline) : !arrival.isBefore(deadline)) { outcome = "TOO_LATE"; quarantine = 1; }
        else {
            Set<String> ids = new HashSet<>(current.ids); ids.add(id);
            next = new RefSummary(current.count + 1, current.revision + 1, Set.copyOf(ids));
            outcome = "REVISED";
        }
        String disposition = row.path("unknown_payment_evidence").asBoolean() ? "RECONCILIATION_REQUIRED" : "NONE";
        return new RefResult(next, outcome, duplicates, quarantine, 0, 0, 0, disposition);
    }
    private static void assertMatches(JsonNode expected, RefResult actual) {
        assertEquals(expected.path("outcome").asText(), actual.outcome);
        assertEquals(expected.path("count").asLong(), actual.summary.count);
        assertEquals(expected.path("revision").asLong(), actual.summary.revision);
        assertEquals(expected.path("duplicate_count").asLong(), actual.duplicates);
        assertEquals(expected.path("quarantined_count").asLong(), actual.quarantined);
        assertEquals(expected.path("charge_calls").asInt(), actual.chargeCalls);
        assertEquals(expected.path("refund_calls").asInt(), actual.refundCalls);
        assertEquals(expected.path("dispatch_calls").asInt(), actual.dispatchCalls);
        if (expected.has("payment_disposition"))
            assertEquals(expected.path("payment_disposition").asText(), actual.disposition);
    }
    private static Set<String> ids(JsonNode summary) {
        Set<String> values = new HashSet<>(); summary.path("observed_ids").forEach(value -> values.add(value.asText())); return Set.copyOf(values);
    }
    private static JsonNode byId(JsonNode root, String id) {
        for (JsonNode row : root.path("cases")) if (id.equals(row.path("id").asText())) return row;
        throw new IllegalArgumentException("Fixture case missing: " + id);
    }
    private record RefSummary(long count, long revision, Set<String> ids) { }
    private record RefResult(RefSummary summary, String outcome, long duplicates, long quarantined,
                             int chargeCalls, int refundCalls, int dispatchCalls, String disposition) { }
}
