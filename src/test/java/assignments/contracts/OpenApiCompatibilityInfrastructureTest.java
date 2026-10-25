package assignments.contracts;

import assignments.local.LocalPolicyIssueController;
import assignments.insurance.PolicyEndorsement;
import assignments.insurance.PolicyIssueRequest;
import assignments.insurance.PolicySnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.*;
import java.util.function.BiFunction;
import static org.junit.jupiter.api.Assertions.*;

/** Independent fixture/source validation; does not award learner completion. */
class OpenApiCompatibilityInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test void contractOperationsMatchTheActualSpringControllerMappings() throws Exception {
        JsonNode contract = contract();
        Set<String> expected = Set.of("POST /api/policies", "GET /api/policies/{policyId}");
        Set<String> actual = new TreeSet<>();
        RequestMapping root = LocalPolicyIssueController.class.getAnnotation(RequestMapping.class);
        String prefix = root.value()[0];
        for (var method : LocalPolicyIssueController.class.getDeclaredMethods()) {
            GetMapping get = method.getAnnotation(GetMapping.class);
            PostMapping post = method.getAnnotation(PostMapping.class);
            if (get != null) actual.add("GET " + prefix + first(get.value()));
            if (post != null) actual.add("POST " + prefix + first(post.value()));
        }
        Set<String> documented = new TreeSet<>();
        for (String path : contract.path("paths").propertyNames()) {
            for (String verb : contract.path("paths").path(path).propertyNames()) {
                documented.add(verb.toUpperCase(Locale.ROOT) + " " + path);
            }
        }
        assertEquals(expected, actual);
        assertEquals(actual, documented);
    }

    @Test void contractHasIndependentRequestAndResponseFieldsAndExpectedStatusCodes() throws Exception {
        JsonNode doc = contract();
        assertEquals("3.2.1", doc.path("openapi").asText());
        JsonNode request = resolve(doc, doc.at("/paths/~1api~1policies/post/requestBody/content/application~1json/schema/$ref").asText());
        assertEquals(Set.of("journeyId", "quoteVersionId", "clientRequestId", "effectiveDate"), strings(request.path("required")));
        JsonNode snapshot = doc.at("/components/schemas/PolicySnapshot");
        assertEquals(Set.of("policyId", "ownerId", "journeyId", "quoteVersionId", "currency", "status", "version", "endorsements"), strings(snapshot.path("required")));
        assertEquals(Set.of("200", "201", "400", "401", "404", "409"), names(doc.at("/paths/~1api~1policies/post/responses")));
        assertEquals(Set.of("200", "401", "404"), names(doc.at("/paths/~1api~1policies~1{policyId}/get/responses")));
        assertEquals(recordFields(PolicyIssueRequest.class), names(doc.at("/components/schemas/PolicyIssueRequest/properties")));
        assertEquals(recordFields(PolicySnapshot.class), names(doc.at("/components/schemas/PolicySnapshot/properties")));
        assertEquals(recordFields(PolicyEndorsement.class), names(doc.at("/components/schemas/PolicyEndorsement/properties")));
        assertEquals("int64", doc.at("/components/schemas/PolicySnapshot/properties/version/format").asText());
        assertEquals(Set.of("ISSUED", "CANCEL_SCHEDULED", "CANCELLED"),
                strings(doc.at("/components/schemas/PolicySnapshot/properties/status/enum")));
    }

    @Test void independentExpectedResultsAndMutantsExerciseTheCompatibilityAssertionPath() throws Exception {
        JsonNode baseline = contract();
        JsonNode additive = baseline.deepCopy();
        ((tools.jackson.databind.node.ObjectNode) additive.at("/components/schemas/PolicySnapshot/properties"))
                .putObject("displayLabel").put("type", "string");
        assertWith(OpenApiCompatibilityReference::compare, baseline, additive, true, List.of());

        JsonNode breaking = baseline.deepCopy();
        var snapshot = (tools.jackson.databind.node.ObjectNode) breaking.at("/components/schemas/PolicySnapshot");
        removeString((tools.jackson.databind.node.ArrayNode) snapshot.path("required"), "ownerId");
        ((tools.jackson.databind.node.ArrayNode) breaking.at("/components/schemas/PolicyIssueRequest/required")).add("channel");
        List<String> expected = List.of(
                "GET /api/policies/{policyId}: response 200 removed required field ownerId",
                "POST /api/policies: new required request field channel",
                "POST /api/policies: response 200 removed required field ownerId",
                "POST /api/policies: response 201 removed required field ownerId");
        assertWith(OpenApiCompatibilityReference::compare, baseline, breaking, false, expected);
        assertThrows(AssertionError.class, () -> assertWith((oldDoc, newDoc) ->
                new OpenApiCompatibilityAnalyzer.CompatibilityReport(true, List.of()), baseline, breaking, false, expected),
                "always-compatible mutant must fail the actual expected-result assertion");
        assertThrows(AssertionError.class, () -> assertWith((oldDoc, newDoc) ->
                new OpenApiCompatibilityAnalyzer.CompatibilityReport(false, List.of("wrong")), baseline, additive, true, List.of()),
                "always-breaking/wrong-diagnostic mutant must fail the additive control");

        JsonNode statusChanged = baseline.deepCopy();
        ((tools.jackson.databind.node.ObjectNode) statusChanged.at("/paths/~1api~1policies/post/responses")).remove("201");
        assertWith(OpenApiCompatibilityReference::compare, baseline, statusChanged, false,
                List.of("POST /api/policies: removed response status 201"));
        JsonNode typeChanged = baseline.deepCopy();
        ((tools.jackson.databind.node.ObjectNode) typeChanged.at("/components/schemas/PolicyIssueRequest/properties/quoteVersionId"))
                .put("type", "integer");
        assertWith(OpenApiCompatibilityReference::compare, baseline, typeChanged, false,
                List.of("POST /api/policies: request changed type for field quoteVersionId"));

        JsonNode removedRoute = baseline.deepCopy();
        ((tools.jackson.databind.node.ObjectNode) removedRoute.path("paths")).remove("/api/policies/{policyId}");
        assertWith(OpenApiCompatibilityReference::compare, baseline, removedRoute, false,
                List.of("removed operation path /api/policies/{policyId}"));
    }

    private static void assertWith(BiFunction<JsonNode, JsonNode, OpenApiCompatibilityAnalyzer.CompatibilityReport> analyzer,
                                   JsonNode oldDoc, JsonNode newDoc, boolean expectedCompatible, List<String> expectedIssues) {
        OpenApiCompatibilityAnalyzer.CompatibilityReport report = analyzer.apply(oldDoc, newDoc);
        assertEquals(expectedCompatible, report.compatible());
        assertEquals(expectedIssues, report.breakingChanges());
    }

    private static void removeString(tools.jackson.databind.node.ArrayNode array, String value) {
        for (int index = 0; index < array.size(); index++) {
            if (array.get(index).asText().equals(value)) { array.remove(index); return; }
        }
        fail("expected fixture field not found: " + value);
    }

    private static JsonNode contract() throws Exception {
        try (InputStream stream = OpenApiCompatibilityInfrastructureTest.class.getResourceAsStream("/contracts/coverstory-policies-v1.json")) {
            assertNotNull(stream);
            return JSON.readTree(stream);
        }
    }

    private static JsonNode resolve(JsonNode root, String pointer) {
        return root.at(pointer.startsWith("#") ? pointer.substring(1) : pointer);
    }
    private static String first(String[] values) { return values.length == 0 ? "" : values[0]; }
    private static Set<String> strings(JsonNode array) {
        TreeSet<String> out = new TreeSet<>(); array.forEach(item -> out.add(item.asText())); return out;
    }
    private static Set<String> recordFields(Class<?> record) {
        TreeSet<String> out = new TreeSet<>();
        for (var component : record.getRecordComponents()) out.add(component.getName());
        return out;
    }
    private static Set<String> names(JsonNode node) {
        return new TreeSet<>(node.propertyNames());
    }
}
