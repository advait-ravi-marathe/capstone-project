package capstone.coverstory.application;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CAP109InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final UUID APP = UUID.fromString("10900000-0000-4000-8000-000000000001");
    private static final String OWNER = "10900000-0000-4000-8000-000000000002";
    private static final Path FIXTURE = Path.of("docs/evidence/coverstory/CAP-109/fixtures.json");

    @Test void fixtureContainsOnlySyntheticDigestsAndPaiseReferences() throws Exception {
        JsonNode root = JSON.readTree(Files.readString(FIXTURE));
        assertEquals("SYNTHETIC_METADATA_ONLY", root.path("classification").asText());
        assertEquals(100000, root.path("versions").get(0).path("total_minor").asLong());
        assertEquals("INR", root.path("versions").get(0).path("currency").asText());
        assertFalse(root.path("raw_payload_included").asBoolean());
        assertFalse(root.path("payment_or_policy_effects").asBoolean());
        assertTrue(root.path("versions").get(0).path("protected_payload_reference").asText().startsWith("synthetic://"));
    }

    @Test void revisionDiffNamesExactChangedAddedAndRemovedMetadataWithoutPayload() {
        var before = version(1, "quote-v1", Map.of("plan", "a1", "rider", "b1"), 100000, "terms-a");
        var after = version(2, "quote-v2", Map.of("plan", "a2", "newOption", "c1"), 110000, "terms-b");
        var delta = CAP109InstructorOracle.compare(before, after, history(List.of(), List.of(), 4, 4, false, false, false, List.of("later", "earlier")));
        assertEquals(Map.of("plan", "a1->a2"), delta.changed());
        assertEquals(List.of("newOption"), delta.added());
        assertEquals(List.of("rider"), delta.removed());
        assertEquals(10000, delta.amountDeltaMinor());
        assertTrue(delta.reviewRequired());
        assertTrue(delta.findings().isEmpty());
    }

    @Test void equalRevisionHasNoDeltaAndTermsChangeStillRequiresReview() {
        var original = version(2, "quote-v2", Map.of("plan", "a2"), 100000, "terms-a");
        var samePriceNewTerms = version(3, "quote-v3", Map.of("plan", "a2"), 100000, "terms-b");
        var equal = CAP109InstructorOracle.compare(original, original, history(List.of(), List.of(), 5, 5, false, false, false, List.of()));
        var changed = CAP109InstructorOracle.compare(original, samePriceNewTerms, history(List.of(), List.of(), 5, 5, false, false, false, List.of()));
        assertTrue(equal.changed().isEmpty() && equal.added().isEmpty() && equal.removed().isEmpty());
        assertFalse(equal.reviewRequired());
        assertEquals(0, changed.amountDeltaMinor());
        assertTrue(changed.reviewRequired(), "terms revision requires explicit review even with unchanged price");
    }

    @Test void duplicateReconciliationCallbackCountsOnePostedEffectAndPreservesHistory() {
        var initiated = event(1, "INITIATED", "provider-event-1", 0);
        var unknown = event(2, "UNKNOWN", "provider-event-1", 0);
        var succeeded = event(3, "RECONCILED_SUCCEEDED", "provider-event-1", 100000);
        var duplicate = event(4, "RECONCILED_SUCCEEDED", "provider-event-1", 100000);
        var delta = CAP109InstructorOracle.compare(version(1, "q1", Map.of("plan", "a1"), 100000, "t1"),
                version(2, "q2", Map.of("plan", "a2"), 110000, "t2"),
                history(List.of(initiated, unknown), List.of(initiated, unknown, succeeded, duplicate), 4, 5, true, true, true, List.of()));
        assertEquals(100000, delta.postedEffectMinor());
        assertTrue(delta.findings().isEmpty());
    }

    @Test void overwrittenOrIncompleteAuditAndWrongRevisionAreRejectedButTelemetryDoesNotAffectState() {
        var original = event(1, "UNKNOWN", "provider-event-1", 0);
        var rewritten = event(1, "FAILED", "provider-event-1", 0);
        var before = version(1, "q1", Map.of("plan", "a1"), 100000, "t1");
        var after = version(2, "q2", Map.of("plan", "a2"), 100000, "t1");
        var broken = CAP109InstructorOracle.compare(before, after,
                history(List.of(original), List.of(rewritten), 4, 9, false, false, true, List.of("late click", "missing view")));
        assertTrue(broken.findings().containsAll(List.of("APPEND_ONLY_VIOLATION", "MISSING_AUDIT", "MISSING_STEP_EVENT", "REVISION_MISMATCH")));
        var withTelemetry = CAP109InstructorOracle.compare(before, after, history(List.of(original), List.of(original), 4, 4, true, true, false, List.of("arbitrary-late-client-time")));
        var withoutTelemetry = CAP109InstructorOracle.compare(before, after, history(List.of(original), List.of(original), 4, 4, true, true, false, List.of()));
        assertEquals(withoutTelemetry, withTelemetry);
    }

    @Test void crossOwnerRevisionIsRejectedWithoutExposingOtherMetadata() {
        var before = version(1, "q1", Map.of("plan", "secret-digest-a"), 100000, "terms-a");
        var foreign = new CAP109InstructorOracle.Version("synthetic-owner-b", APP, 2, "q2",
                "synthetic://envelope/other-owner/revision-2", Map.of("plan", "secret-digest-b"), 110000, "INR", "terms-b");
        var result = CAP109InstructorOracle.compare(before, foreign, history(List.of(), List.of(), 4, 4, false, false, false, List.of()));
        assertEquals(List.of("FORBIDDEN"), result.findings());
        assertTrue(result.changed().isEmpty());
        assertTrue(result.added().isEmpty() && result.removed().isEmpty());
    }

    private static CAP109InstructorOracle.Version version(long revision, String source,
            Map<String, String> digests, long total, String terms) {
        return new CAP109InstructorOracle.Version(OWNER, APP, revision, source,
                "synthetic://envelope/app-1/revision-" + revision, digests, total, "INR", terms);
    }
    private static CAP109InstructorOracle.History history(List<CAP109InstructorOracle.Event> prior,
            List<CAP109InstructorOracle.Event> current, long priorRevision, long currentRevision,
            boolean audit, boolean step, boolean applied, List<String> telemetry) {
        return new CAP109InstructorOracle.History(prior, current, priorRevision, currentRevision, audit, step, applied, telemetry);
    }
    private static CAP109InstructorOracle.Event event(long seq, String state, String ref, long amount) {
        return new CAP109InstructorOracle.Event(seq, UUID.nameUUIDFromBytes((seq + state).getBytes()), state, ref, amount);
    }
}
