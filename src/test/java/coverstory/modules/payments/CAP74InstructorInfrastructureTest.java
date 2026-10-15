package coverstory.modules.payments;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Test-only contract oracle. It does not instantiate or repair the learner seam. */
class CAP74InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-74/synthetic-v1.json");

    @Test void fixtureIsSyntheticAndCarriesAllSafeAndForbiddenFields() throws Exception {
        JsonNode root = fixture();
        assertEquals("SYNTHETIC_OFFLINE_CONTRACT", root.path("classification").asText());
        assertFalse(root.path("actual_provider_call").asBoolean());
        assertEquals("GET /api/v1/payment-attempts/{attemptId}", root.path("endpoint").asText());
        assertEquals(5, root.path("success_fields").size());
        assertEquals(4, root.path("cases").size());
        assertTrue(root.path("forbidden_response_fields").toString().contains("operationReference"));
    }

    @Test void independentReferenceAcceptsPendingAndUnknownAndRejectsLeakyOrMisleadingVariants() throws Exception {
        JsonNode pending = caseById("CAP-74-CS-01");
        OracleResult pendingResult = referenceGet(pending.path("principal").asText(),
                pending.path("attempt").path("attemptId").asText(), pending.path("attempt"), false);
        assertEquals(200, pendingResult.status);
        assertEquals("PENDING", pendingResult.body.get("state"));
        assertEquals("NONE", pendingResult.body.get("nextAction"));
        assertEquals(5, pendingResult.body.size());
        assertFalse(pendingResult.body.containsKey("operationReference"));
        assertFalse(pendingResult.body.toString().contains("op-private-A"));

        JsonNode unknown = caseById("CAP-74-CS-02");
        OracleResult unknownResult = referenceGet(unknown.path("principal").asText(),
                unknown.path("attempt").path("attemptId").asText(), unknown.path("attempt"), false);
        assertEquals(200, unknownResult.status);
        assertEquals("UNKNOWN", unknownResult.body.get("state"));
        assertEquals("RECONCILE", unknownResult.body.get("nextAction"));

        JsonNode invalid = invalidState();
        OracleResult invalidResult = referenceGet(invalid.path("ownerId").asText(),
                invalid.path("attemptId").asText(), invalid, false);
        assertEquals(500, invalidResult.status);
        assertEquals("PAYMENT_STATE_INVALID", invalidResult.errorCode);
        assertFalse(unknownResult.body.get("nextAction").equals("NONE"), "UNKNOWN cannot invite another charge");
    }

    @Test void ownerScopeMakesForeignAndAbsentObservationsByteEquivalentAndAnonymousIsUnauthorized() throws Exception {
        JsonNode contract = caseById("CAP-74-CS-03");
        JsonNode foreign = contract.path("foreign_attempt");
        OracleResult foreignResult = referenceGet(contract.path("principal").asText(),
                foreign.path("attemptId").asText(), foreign, false);
        OracleResult missingResult = referenceGet(contract.path("principal").asText(),
                contract.path("missing_attempt_id").asText(), null, false);
        assertEquals(foreignResult, missingResult);
        assertEquals(404, foreignResult.status);
        assertEquals("PAYMENT_ATTEMPT_NOT_FOUND", foreignResult.errorCode);
        assertEquals("UNAUTHORIZED", referenceGet(null, "any", foreign, false).errorCode);

        OracleResult leaked = referenceGetUnscoped("owner-A", foreign.path("attemptId").asText(), foreign);
        assertEquals(200, leaked.status, "deliberately defective unscoped lookup is caught by the positive 404 requirement");
        assertNotEquals(foreignResult, leaked);
    }

    private static OracleResult referenceGetUnscoped(String principal, String requestedId, JsonNode attempt) {
        if (principal == null || principal.isBlank()) return new OracleResult(401, "UNAUTHORIZED", Map.of());
        // Deliberate defect witness: lookup by identifier alone, ignoring owner.
        return referenceGet(attempt.path("ownerId").asText(), requestedId, attempt, false);
    }

    private static OracleResult referenceGet(String principal, String requestedId, JsonNode attempt, boolean impossibleState) {
        if (principal == null || principal.isBlank()) return new OracleResult(401, "UNAUTHORIZED", Map.of());
        if (attempt == null || !principal.equals(attempt.path("ownerId").asText())
                || !requestedId.equals(attempt.path("attemptId").asText()))
            return new OracleResult(404, "PAYMENT_ATTEMPT_NOT_FOUND", Map.of());
        String state = impossibleState ? "CAPTURED_AND_ISSUED" : attempt.path("state").asText();
        if (!java.util.Set.of("PENDING", "UNKNOWN", "FAILED", "SUCCEEDED").contains(state))
            return new OracleResult(500, "PAYMENT_STATE_INVALID", Map.of());
        String nextAction = "UNKNOWN".equals(state) ? "RECONCILE" : "NONE";
        return new OracleResult(200, null, Map.of(
                "attemptId", requestedId,
                "state", state,
                "updatedAt", attempt.path("updatedAt").asText(),
                "requestId", attempt.path("requestId").asText(),
                "nextAction", nextAction));
    }

    private static JsonNode invalidState() throws Exception { return caseById("CAP-74-CS-04").path("impossible_state_attempt"); }
    private static JsonNode fixture() throws Exception { return JSON.readTree(Files.readString(FIXTURE)); }
    private static JsonNode caseById(String id) throws Exception {
        for (JsonNode row : fixture().path("cases")) if (id.equals(row.path("id").asText())) return row;
        throw new IllegalArgumentException("Fixture case not found: " + id);
    }

    private record OracleResult(int status, String errorCode, Map<String, String> body) { }
}
