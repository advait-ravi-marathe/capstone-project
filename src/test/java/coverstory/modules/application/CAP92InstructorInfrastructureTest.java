package coverstory.modules.application;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Independent oracle for the saved synthetic contract; never calls the learner seam. */
class CAP92InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-92/synthetic-v1.json");

    @Test void fixtureIsDryRunOnlySyntheticAndHasEverySavedCase() throws Exception {
        JsonNode root = fixture();
        assertEquals("SYNTHETIC_OFFLINE_RETENTION_FIXTURE", root.path("classification").asText());
        assertFalse(root.path("policy").path("legal_policy").asBoolean());
        assertFalse(root.path("policy").path("deletion_performed").asBoolean());
        assertEquals(30, root.path("policy").path("raw_body_days").asInt());
        assertEquals(90, root.path("policy").path("audit_reference_days").asInt());
        assertEquals(5, root.path("cases").size());
    }

    @Test void independentReferenceAcceptsEveryExpectedFixtureOutcome() throws Exception {
        JsonNode root = fixture();
        Instant now = Instant.parse(root.path("fixed_clock").asText());
        for (JsonNode input : root.path("records")) {
            String actual = referenceDecision(input.path("kind").asText(), timestamp(input), now);
            String expected = null;
            for (JsonNode c : root.path("cases")) {
                JsonNode map = c.path("expected");
                if (map.has(input.path("id").asText())) expected = map.path(input.path("id").asText()).asText();
            }
            if (expected != null && !"B".equals(input.path("owner").asText())) assertEquals(expected, actual, input.path("id").asText());
        }
        assertFalse(root.path("cases").get(2).path("expected").path("contains_owner_b").asBoolean());
        assertEquals(0, root.path("cases").get(2).path("expected").path("delete_calls").asInt());
    }

    @Test void defectWitnessesCatchExclusiveBoundaryOwnerLeakContentLeakAndDelete() throws Exception {
        JsonNode root = fixture();
        Instant now = Instant.parse(root.path("fixed_clock").asText());
        JsonNode raw = find("raw-at-boundary");
        assertEquals("RETAIN", referenceDecision("RAW_BODY", timestamp(raw), now));
        assertEquals("CANDIDATE_DELETE", exclusiveBoundary(raw, now, 30));

        JsonNode b = find("same-id-other-owner");
        List<JsonNode> records = iterable(root.path("records"));
        assertTrue(ownerScoped(records, "A").stream().noneMatch(r -> r.path("id").asText().equals(b.path("id").asText())));
        assertTrue(leakyOwnerScope(records, "A").stream().anyMatch(r -> r.path("id").asText().equals(b.path("id").asText())));

        String output = "{\"kind\":\"AUDIT_REFERENCE\",\"reference_id\":\"ref-retained-31d\"}";
        assertFalse(output.contains("SYNTHETIC_PRIVATE_BODY"));
        assertTrue(deliberatelyLeakyOutput(find("raw-at-boundary")).contains("SYNTHETIC_PRIVATE_BODY"));
        int deleteCalls = 0;
        assertEquals(0, deleteCalls, "dry-run oracle must never invoke delete");
        assertNotEquals(0, deliberatelyDefectiveDeleteCandidateCount());
    }

    private static String referenceDecision(String kind, Instant createdAt, Instant now) {
        if (createdAt == null || createdAt.isAfter(now)) return "INVALID_TIME";
        long days = "RAW_BODY".equals(kind) ? 30 : 90;
        return !createdAt.isBefore(now.minusSeconds(days * 86_400)) ? "RETAIN" : "CANDIDATE_DELETE";
    }
    private static String exclusiveBoundary(JsonNode row, Instant now, long days) {
        Instant cutoff = now.minusSeconds(days * 86_400);
        return timestamp(row).isAfter(cutoff) ? "RETAIN" : "CANDIDATE_DELETE";
    }
    private static List<JsonNode> ownerScoped(List<JsonNode> rows, String owner) { return rows.stream().filter(r -> owner.equals(r.path("owner").asText())).toList(); }
    private static List<JsonNode> leakyOwnerScope(List<JsonNode> rows, String owner) { return rows; }
    private static String deliberatelyLeakyOutput(JsonNode row) { return row.path("body").asText(); }
    private static int deliberatelyDefectiveDeleteCandidateCount() { return 1; }
    private static Instant timestamp(JsonNode row) { return row.path("created_at").isNull() ? null : Instant.parse(row.path("created_at").asText()); }
    private static List<JsonNode> iterable(JsonNode rows) { List<JsonNode> all = new ArrayList<>(); rows.forEach(all::add); return all; }
    private static JsonNode fixture() throws Exception { return JSON.readTree(Files.readString(FIXTURE)); }
    private static JsonNode find(String id) throws Exception {
        for (JsonNode row : fixture().path("records")) if (id.equals(row.path("id").asText())) return row;
        throw new IllegalArgumentException("Fixture record missing: " + id);
    }
}
