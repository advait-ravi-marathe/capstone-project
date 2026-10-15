package coverstory.modules.shortlist;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;
import coverstory.modules.shortlist.api.SelectionInput;
import coverstory.modules.shortlist.api.SelectionKey;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CAP40ScaffoldInfrastructureTest {
    @Test
    void syntheticFixtureHasExpectedContractShape() throws Exception {
        JsonNode fixture = new ObjectMapper().readTree(Files.readString(
                Path.of("fixtures/coverstory/CAP-40/synthetic-v1.json")));
        assertEquals("CAP-40", fixture.path("contract").asText());
        assertEquals("SYNTHETIC", fixture.path("classification").asText());
        assertEquals(3, fixture.path("cases").size());
    }

    @Test
    void instructorWitnessAcceptsCorrectPairsAndRejectsScopeAndOrderDefects() throws Exception {
        JsonNode cases = new ObjectMapper().readTree(Files.readString(
                Path.of("fixtures/coverstory/CAP-40/synthetic-v1.json"))).path("cases");
        JsonNode firstSeen = byId(cases, "CAP-40-CS-01");
        List<SelectionInput> input = inputs(firstSeen.path("input"));
        List<SelectionKey> expected = keys(firstSeen.path("expected"));
        List<SelectionInput> before = List.copyOf(input);

        assertEquals(expected, referenceWitness(input));
        assertEquals(before, input, "the oracle fixture must not mutate its input");

        JsonNode scopeVector = byId(cases, "CAP-40-CS-03");
        List<SelectionInput> sameVersionAcrossScopes = inputs(scopeVector.path("input"));
        List<SelectionKey> scopeExpected = keys(scopeVector.path("expected"));
        assertEquals(scopeExpected, referenceWitness(sameVersionAcrossScopes));
        assertNotEquals(scopeExpected, versionOnlyDefect(sameVersionAcrossScopes),
                "the positive/negative vector must catch version-only identity");

        List<SelectionInput> reverseLexicalOrder = List.of(
                new SelectionInput("device-A", "health-Z-v1"),
                new SelectionInput("device-A", "health-A-v1"));
        List<SelectionKey> firstSeenExpected = List.of(
                new SelectionKey("device-A", "health-Z-v1"),
                new SelectionKey("device-A", "health-A-v1"));
        assertEquals(firstSeenExpected, referenceWitness(reverseLexicalOrder));
        assertNotEquals(firstSeenExpected, sortedOrderDefect(referenceWitness(reverseLexicalOrder)),
                "the vector must distinguish first-seen order from sorting");
    }

    private static JsonNode byId(JsonNode cases, String id) {
        for (JsonNode row : cases) if (id.equals(row.path("id").asText())) return row;
        throw new IllegalArgumentException("Fixture case not found: " + id);
    }

    private static List<SelectionInput> inputs(JsonNode node) {
        return new ObjectMapper().convertValue(node, new TypeReference<>() { });
    }

    private static List<SelectionKey> keys(JsonNode node) {
        return new ObjectMapper().convertValue(node, new TypeReference<>() { });
    }

    /** Test-only reference witness; it is not part of the application or learner implementation. */
    private static List<SelectionKey> referenceWitness(List<SelectionInput> input) {
        List<SelectionKey> output = new ArrayList<>();
        Set<SelectionKey> seen = new LinkedHashSet<>();
        for (SelectionInput row : input) {
            SelectionKey key = new SelectionKey(row.deviceScope(), row.catalogVersionId());
            if (seen.add(key)) output.add(key);
        }
        return output;
    }

    private static List<SelectionKey> versionOnlyDefect(List<SelectionInput> input) {
        Set<String> versions = new LinkedHashSet<>();
        List<SelectionKey> output = new ArrayList<>();
        for (SelectionInput row : input) {
            if (versions.add(row.catalogVersionId())) output.add(new SelectionKey(row.deviceScope(), row.catalogVersionId()));
        }
        return output;
    }

    private static List<SelectionKey> sortedOrderDefect(List<SelectionKey> input) {
        return input.stream().sorted(java.util.Comparator.comparing(SelectionKey::catalogVersionId)).toList();
    }
}
