package assignments.contracts;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.io.InputStream;
import static org.junit.jupiter.api.Assertions.*;

/** Learner acceptance path. Expected to remain red until the starter TODO is implemented. */
@Tag("learnerAcceptance")
class OpenApiCompatibilityAcceptanceTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test void optionalResponseAdditionIsCompatible() throws Exception {
        JsonNode baseline = readContract();
        JsonNode changed = baseline.deepCopy();
        ((tools.jackson.databind.node.ObjectNode) changed.at("/components/schemas/PolicySnapshot/properties"))
                .putObject("displayLabel").put("type", "string");
        var report = OpenApiCompatibilityAnalyzer.compare(baseline, changed);
        assertTrue(report.compatible());
        assertEquals(java.util.List.of(), report.breakingChanges());
    }

    @Test void removedRequiredResponseFieldAndNewRequiredRequestFieldAreBreaking() throws Exception {
        JsonNode baseline = readContract();
        JsonNode changed = baseline.deepCopy();
        removeString((tools.jackson.databind.node.ArrayNode) changed.at("/components/schemas/PolicySnapshot/required"), "ownerId");
        ((tools.jackson.databind.node.ArrayNode) changed.at("/components/schemas/PolicyIssueRequest/required")).add("channel");
        assertFalse(OpenApiCompatibilityAnalyzer.compare(baseline, changed).compatible());
        assertEquals(4, OpenApiCompatibilityAnalyzer.compare(baseline, changed).breakingChanges().size());
    }

    private JsonNode readContract() throws Exception {
        try (InputStream stream = getClass().getResourceAsStream("/contracts/coverstory-policies-v1.json")) {
            assertNotNull(stream); return mapper.readTree(stream);
        }
    }
}
