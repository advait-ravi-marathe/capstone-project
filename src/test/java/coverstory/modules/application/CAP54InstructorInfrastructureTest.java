package coverstory.modules.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Instructor-only fixture/oracle checks; no learner seam is invoked here. */
class CAP54InstructorInfrastructureTest {
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-54/synthetic-v1.json");

    @Test void fixtureHasOwnerBoundJourneyRevisionsAndSafeOutputVectors() throws Exception {
        JsonNode root = new ObjectMapper().readTree(Files.readString(FIXTURE));
        assertEquals("CAP-54-synthetic-v1", root.path("fixture_id").asText());
        assertEquals("SYNTHETIC_ONLY", root.path("classification").asText());
        assertEquals(2, root.path("journeys").size());
        assertEquals(8, root.path("cases").size());
        for (JsonNode row : root.path("journeys")) {
            UUID.fromString(row.path("journey_id").asText());
            UUID.fromString(row.path("application_id").asText());
            assertTrue(row.path("revision").asLong() > 0);
            assertTrue(row.path("private_payload_marker").asText().startsWith("SYNTHETIC_PRIVATE_SENTINEL_"));
        }
        assertEquals("user-A", root.path("journeys").get(0).path("owner_id").asText());
        assertEquals("PAYMENT_PENDING", root.path("journeys").get(0).path("authoritative_status").asText());
    }

    @Test void accessOraclePassesOwnJourneyAndRejectsUuidOnlyAuthorizationDefect() throws Exception {
        JsonNode root = new ObjectMapper().readTree(Files.readString(FIXTURE));
        Map<String, JsonNode> byId = new HashMap<>();
        root.path("journeys").forEach(row -> byId.put(row.path("journey_id").asText(), row));
        String journeyA = "3c7d76dc-29dd-4f2c-a979-8c4a40c8a001";
        Map<String, Object> owned = referenceResume("user-A", journeyA, byId);
        assertEquals(Map.of("status", 200, "journeyId", journeyA,
                "businessStatus", "PAYMENT_PENDING", "resumeAction", "CONTINUE_CHECKOUT"), owned);

        Map<String, Object> foreign = referenceResume("user-B", journeyA, byId);
        Map<String, Object> absent = referenceResume("user-A", "00000000-0000-4000-8000-000000000099", byId);
        assertEquals(Map.of("status", 404, "error", "NOT_FOUND"), foreign);
        assertEquals(absent, foreign, "foreign and missing must be indistinguishable");
        assertFalse(unsafeUuidOnlyLookup(journeyA, byId).equals(foreign),
                "the witness must detect lookup-by-UUID without owner scope");
        assertFalse(owned.toString().contains("SYNTHETIC_PRIVATE_SENTINEL"));
    }

    @Test void cursorOracleAcceptsOnlyBoundedOwnerMatchedPages() {
        assertTrue(validListRequest("user-A", 50, "cursor:user-A"));
        assertFalse(validListRequest("user-A", 51, "cursor:user-A"));
        assertFalse(validListRequest("user-A", 50, "cursor:user-B"));
        assertFalse(validListRequest("user-A", 0, null));
    }

    private static Map<String, Object> referenceResume(String actor, String journeyId, Map<String, JsonNode> byId) {
        JsonNode row = byId.get(journeyId);
        if (actor == null || row == null || !actor.equals(row.path("owner_id").asText()))
            return Map.of("status", 404, "error", "NOT_FOUND");
        String action = row.path("dispatch_confirmed").asBoolean() ? "VIEW_POLICY"
                : row.path("payment_confirmed").asBoolean() ? "WAIT_FOR_DISPATCH" : "CONTINUE_CHECKOUT";
        return Map.of("status", 200, "journeyId", journeyId,
                "businessStatus", row.path("authoritative_status").asText(), "resumeAction", action);
    }

    private static Map<String, Object> unsafeUuidOnlyLookup(String journeyId, Map<String, JsonNode> byId) {
        JsonNode row = byId.get(journeyId);
        return row == null ? Map.of("status", 404, "error", "NOT_FOUND")
                : Map.of("status", 200, "journeyId", journeyId,
                    "businessStatus", row.path("authoritative_status").asText(),
                    "resumeAction", "CONTINUE_CHECKOUT");
    }

    private static boolean validListRequest(String actor, int limit, String cursor) {
        return actor != null && limit >= 1 && limit <= 50
                && (cursor == null || cursor.equals("cursor:" + actor));
    }
}
