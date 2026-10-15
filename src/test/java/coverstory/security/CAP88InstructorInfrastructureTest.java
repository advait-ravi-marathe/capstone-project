package coverstory.security;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Independent fixture oracle; it never calls the learner policy seam. */
class CAP88InstructorInfrastructureTest {
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-88/synthetic-v1.json");
    private final ObjectMapper json = new ObjectMapper();

    @Test void fixtureIsSyntheticCompleteAndInternallyWellFormed() throws Exception {
        JsonNode root = json.readTree(Files.readString(FIXTURE));
        assertEquals("CAP-88-synthetic-v1", root.path("fixture_id").asText());
        assertEquals("SYNTHETIC_ONLY", root.path("classification").asText());
        assertEquals(16, root.path("cases").size());
        Set<String> ids = new HashSet<>();
        for (JsonNode row : root.path("cases")) {
            assertTrue(ids.add(row.path("id").asText()), "case IDs are unique");
            assertTrue(root.path("roles").toString().contains(row.path("role").asText()));
            assertTrue(root.path("resources").has(row.path("resource").asText()));
            assertTrue(row.path("allow").isBoolean());
        }
        assertTrue(root.path("resources").path("draftA").path("privateMarker").asText().startsWith("SYNTHETIC_"));
        assertTrue(root.path("resources").path("case2").path("answerMarker").asText().startsWith("SYNTHETIC_"));
    }

    @Test void independentReferenceAcceptsAllSavedMatrixRows() throws Exception {
        JsonNode root = json.readTree(Files.readString(FIXTURE));
        for (JsonNode row : root.path("cases")) {
            ReferenceDecision actual = reference(row, root);
            assertEquals(row.path("allow").asBoolean(), actual.allowed(), row.path("id").asText());
            assertEquals(strings(row.path("visibleFields")), actual.visibleFields(), row.path("id").asText());
        }
    }

    @Test void oracleRejectsBroadRoleAndUuidOnlyMutants() throws Exception {
        JsonNode root = json.readTree(Files.readString(FIXTURE));
        JsonNode ownApplicant = find(root, "CAP-88-01");
        assertTrue(reference(ownApplicant, root).allowed());
        assertTrue(broadRoleMutant(ownApplicant, root), "the applicant-role rule accepts the positive owner witness");
        JsonNode foreignApplicant = find(root, "CAP-88-03");
        assertTrue(broadRoleMutant(foreignApplicant, root), "the deliberate mutant incorrectly grants the foreign draft");
        assertFalse(reference(foreignApplicant, root).allowed());

        JsonNode assistantCharge = find(root, "CAP-88-12");
        assertTrue(roleOnlyMutant(assistantCharge), "the deliberate role-only mutant grants an assistant mutation");
        assertFalse(reference(assistantCharge, root).allowed());
        JsonNode unassigned = find(root, "CAP-88-07");
        assertTrue(roleOnlyMutant(unassigned), "the deliberate support-role mutant ignores assignment scope");
        assertFalse(reference(unassigned, root).allowed());
    }

    private record ReferenceDecision(boolean allowed, Set<String> visibleFields) { }

    private static ReferenceDecision reference(JsonNode row, JsonNode root) {
        String role = row.path("role").asText();
        String actor = row.path("actorId").asText();
        String action = row.path("action").asText();
        JsonNode resource = root.path("resources").path(row.path("resource").asText());
        boolean allow = switch (role) {
            case "APPLICANT" -> actor.equals(resource.path("ownerId").asText())
                    && ((action.equals("READ_OWN_DRAFT") && resource.path("kind").asText().equals("DRAFT"))
                    || (action.equals("WRITE_OWN_DRAFT") && resource.path("kind").asText().equals("DRAFT")));
            case "CATALOG_EDITOR" -> action.equals("EDIT_CATALOG") && resource.path("kind").asText().equals("CATALOG");
            case "SUPPORT" -> action.equals("READ_SUPPORT_CASE")
                    && actor.equals(resource.path("assignedSupportId").asText())
                    && resource.path("kind").asText().equals("SUPPORT_CASE");
            case "SIMULATOR" -> action.equals("SUBMIT_SIMULATED_EVENT")
                    && resource.path("kind").asText().equals("SIMULATED_EVENT");
            case "READ_ONLY_ASSISTANT" -> action.equals("READ_PERMITTED_EVIDENCE")
                    && resource.path("kind").asText().equals("EVIDENCE");
            default -> false;
        };
        Set<String> fields = !allow ? Set.of() : switch (role) {
            case "APPLICANT" -> action.equals("READ_OWN_DRAFT") ? Set.of("draftSummary", "ownAnswers") : Set.of();
            case "SUPPORT" -> Set.of("caseStatus", "minimumSupportExcerpt");
            case "READ_ONLY_ASSISTANT" -> Set.of("allowlistedEvidence");
            default -> Set.of();
        };
        return new ReferenceDecision(allow, fields);
    }

    private static boolean broadRoleMutant(JsonNode row, JsonNode root) {
        return row.path("role").asText().equals("APPLICANT")
                && root.path("resources").path(row.path("resource").asText()).path("kind").asText().equals("DRAFT");
    }

    private static boolean roleOnlyMutant(JsonNode row) {
        return row.path("role").asText().equals("READ_ONLY_ASSISTANT") || row.path("role").asText().equals("SUPPORT");
    }

    private static JsonNode find(JsonNode root, String id) {
        for (JsonNode row : root.path("cases")) if (row.path("id").asText().equals(id)) return row;
        throw new IllegalArgumentException("missing fixture case " + id);
    }

    private static Set<String> strings(JsonNode array) {
        Set<String> result = new HashSet<>();
        array.forEach(value -> result.add(value.asText()));
        return Set.copyOf(result);
    }
}
