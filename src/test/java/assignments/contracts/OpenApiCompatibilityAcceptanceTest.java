package assignments.contracts;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.io.InputStream;
import static org.junit.jupiter.api.Assertions.*;

/** Learner acceptance path, exercised against fixed consumer documents and independent expected diagnostics. */
@Tag("learnerAcceptance")
class OpenApiCompatibilityAcceptanceTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test void optionalResponseAdditionIsCompatible() throws Exception {
        JsonNode baseline = readContract();
        JsonNode changed = baseline.deepCopy();
        ((tools.jackson.databind.node.ObjectNode) changed.at("/components/schemas/PolicySnapshot/properties"))
                .putObject("displayLabel").put("type", "string");
        assertLearnerResult(baseline, changed, true, java.util.List.of());
    }

    @Test void removedRequiredResponseFieldAndNewRequiredRequestFieldAreBreaking() throws Exception {
        JsonNode baseline = readContract();
        JsonNode changed = baseline.deepCopy();
        removeString((tools.jackson.databind.node.ArrayNode) changed.at("/components/schemas/PolicySnapshot/required"), "ownerId");
        ((tools.jackson.databind.node.ArrayNode) changed.at("/components/schemas/PolicyIssueRequest/required")).add("channel");
        assertLearnerResult(baseline, changed, false, java.util.List.of(
                "GET /api/policies/{policyId}: response 200 removed required field ownerId",
                "POST /api/policies: new required request field channel",
                "POST /api/policies: response 200 removed required field ownerId",
                "POST /api/policies: response 201 removed required field ownerId"));
    }

    @Test void removedStatusAndChangedRequestTypeProduceExactConsumerDiagnostics() throws Exception {
        JsonNode baseline = readContract();
        JsonNode statusChange = baseline.deepCopy();
        ((tools.jackson.databind.node.ObjectNode) statusChange.at("/paths/~1api~1policies/post/responses")).remove("201");
        assertLearnerResult(baseline, statusChange, false,
                java.util.List.of("POST /api/policies: removed response status 201"));

        JsonNode typeChange = baseline.deepCopy();
        ((tools.jackson.databind.node.ObjectNode) typeChange.at("/components/schemas/PolicyIssueRequest/properties/quoteVersionId"))
                .put("type", "integer");
        assertLearnerResult(baseline, typeChange, false,
                java.util.List.of("POST /api/policies: request changed type for field quoteVersionId"));
    }

    @Test void routeRemovalAndAlwaysCompatibleMutantsFailTheSameLearnerAssertion() throws Exception {
        JsonNode baseline = readContract();
        JsonNode routeRemoved = baseline.deepCopy();
        ((tools.jackson.databind.node.ObjectNode) routeRemoved.path("paths")).remove("/api/policies/{policyId}");
        assertLearnerResult(baseline, routeRemoved, false,
                java.util.List.of("removed operation path /api/policies/{policyId}"));
        assertThrows(AssertionError.class, () -> assertReport(
                new OpenApiCompatibilityAnalyzer.CompatibilityReport(true, java.util.List.of()), false,
                java.util.List.of("POST /api/policies: removed response status 201")),
                "an always-compatible mutant must fail the exact learner-path diagnostic assertion");
    }

    @Test void missingResponseSchemaAndNestedReferencedTypeChangesAreBreaking() throws Exception {
        JsonNode baseline = readContract();
        JsonNode noSchema = baseline.deepCopy();
        ((tools.jackson.databind.node.ObjectNode) noSchema.at("/paths/~1api~1policies/post/responses/201"))
                .remove("content");
        assertLearnerResult(baseline, noSchema, false,
                java.util.List.of("POST /api/policies: response 201 removed content schema"));

        JsonNode nestedType = baseline.deepCopy();
        ((tools.jackson.databind.node.ObjectNode) nestedType.at("/components/schemas/PolicyEndorsement/properties/version"))
                .put("type", "string");
        assertLearnerResult(baseline, nestedType, false, java.util.List.of(
                "GET /api/policies/{policyId}: response 200 changed type for field endorsements[].version",
                "POST /api/policies: response 200 changed type for field endorsements[].version",
                "POST /api/policies: response 201 changed type for field endorsements[].version"));
    }

    private JsonNode readContract() throws Exception {
        try (InputStream stream = getClass().getResourceAsStream("/contracts/coverstory-policies-v1.json")) {
            assertNotNull(stream); return mapper.readTree(stream);
        }
    }

    private static void assertLearnerResult(JsonNode oldDocument, JsonNode newDocument,
                                            boolean compatible, java.util.List<String> diagnostics) {
        assertReport(OpenApiCompatibilityAnalyzer.compare(oldDocument, newDocument), compatible, diagnostics);
    }

    private static void assertReport(OpenApiCompatibilityAnalyzer.CompatibilityReport report,
                                     boolean compatible, java.util.List<String> diagnostics) {
        assertEquals(compatible, report.compatible());
        assertEquals(diagnostics, report.breakingChanges());
    }

    private static void removeString(tools.jackson.databind.node.ArrayNode array, String value) {
        for (int index = 0; index < array.size(); index++) {
            if (array.get(index).asText().equals(value)) { array.remove(index); return; }
        }
        fail("expected contract field not found: " + value);
    }
}
