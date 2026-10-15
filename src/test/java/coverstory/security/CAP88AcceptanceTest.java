package coverstory.security;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Learner acceptance contract for a pure role/resource policy, not HTTP authentication. */
@Tag("learnerAcceptance")
class CAP88AcceptanceTest {
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-88/synthetic-v1.json");
    private final CAP88AccessPolicySeam policy = new CAP88AccessPolicySeam();
    private final ObjectMapper json = new ObjectMapper();

    @Test void applicantReadsOnlyTheirOwnDraftAndGetsNoPrivateSerialization() throws Exception {
        JsonNode root = json.readTree(Files.readString(FIXTURE));
        JsonNode own = find(root, "CAP-88-01");
        var decision = policy.decide(request(own, root));
        assertTrue(decision.allowed());
        assertEquals(Set.of("draftSummary", "ownAnswers"), decision.visibleFields());
        assertFalse(decision.visibleFields().toString().contains("SYNTHETIC_PRIVATE"));
        assertFalse(policy.decide(request(find(root, "CAP-88-03"), root)).allowed());
    }

    @Test void catalogEditorCanEditCatalogButCannotReadApplicantDisclosures() throws Exception {
        JsonNode root = json.readTree(Files.readString(FIXTURE));
        assertTrue(policy.decide(request(find(root, "CAP-88-04"), root)).allowed());
        var denied = policy.decide(request(find(root, "CAP-88-05"), root));
        assertFalse(denied.allowed());
        assertTrue(denied.visibleFields().isEmpty());
    }

    @Test void assistantCannotMutatePaymentOrSubmissionState() throws Exception {
        JsonNode root = json.readTree(Files.readString(FIXTURE));
        int mutationCalls = 0;
        for (String id : Set.of("CAP-88-10", "CAP-88-11", "CAP-88-12", "CAP-88-13", "CAP-88-14")) {
            var decision = policy.decide(request(find(root, id), root));
            if (decision.allowed()) mutationCalls++;
            assertFalse(decision.allowed(), id);
        }
        assertEquals(0, mutationCalls);
    }

    @Test void supportReadsAssignedMinimumOnlyAndCannotReadAnUnassignedCase() throws Exception {
        JsonNode root = json.readTree(Files.readString(FIXTURE));
        var assigned = policy.decide(request(find(root, "CAP-88-06"), root));
        assertTrue(assigned.allowed());
        assertEquals(Set.of("caseStatus", "minimumSupportExcerpt"), assigned.visibleFields());
        var unassigned = policy.decide(request(find(root, "CAP-88-07"), root));
        assertFalse(unassigned.allowed());
        assertTrue(unassigned.visibleFields().isEmpty());
    }

    @Test void simulatorAndAssistantReadHaveNarrowPositiveCapabilities() throws Exception {
        JsonNode root = json.readTree(Files.readString(FIXTURE));
        assertTrue(policy.decide(request(find(root, "CAP-88-08"), root)).allowed());
        var evidence = policy.decide(request(find(root, "CAP-88-09"), root));
        assertTrue(evidence.allowed());
        assertEquals(Set.of("allowlistedEvidence"), evidence.visibleFields());
    }

    @Test void unknownRoleAndActionsDenyByDefault() throws Exception {
        JsonNode root = json.readTree(Files.readString(FIXTURE));
        assertFalse(policy.decide(request(find(root, "CAP-88-15"), root)).allowed());
        assertFalse(policy.decide(request(find(root, "CAP-88-16"), root)).allowed());
    }

    private CAP88AccessPolicySeam.AccessRequest request(JsonNode row, JsonNode root) {
        JsonNode resource = root.path("resources").path(row.path("resource").asText());
        return new CAP88AccessPolicySeam.AccessRequest(
                new CAP88AccessPolicySeam.Actor(row.path("actorId").asText(), enumValue(CAP88AccessPolicySeam.Role.class, row.path("role").asText())),
                enumValue(CAP88AccessPolicySeam.Action.class, row.path("action").asText()),
                new CAP88AccessPolicySeam.Resource(enumValue(CAP88AccessPolicySeam.ResourceKind.class, resource.path("kind").asText()),
                        nullable(resource.path("ownerId")), nullable(resource.path("assignedSupportId"))));
    }

    private static String nullable(JsonNode value) { return value.isNull() ? null : value.asText(); }
    private static <E extends Enum<E>> E enumValue(Class<E> type, String value) { return Enum.valueOf(type, value); }
    private static JsonNode find(JsonNode root, String id) {
        for (JsonNode row : root.path("cases")) if (row.path("id").asText().equals(id)) return row;
        throw new IllegalArgumentException("missing fixture case " + id);
    }
}
