package coverstory.modules.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Passing controls validate fixture identity/oracles, not learner behavior or completion. */
class CatalogInstructorHarnessTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test void allThreeFocusedAcceptancesHaveIndependentLearnerTodoStubs() throws Exception {
        for (String source : List.of(
                Files.readString(Path.of("src/main/java/coverstory/modules/catalog/api/CAP165Seam.java")),
                Files.readString(Path.of("src/main/java/coverstory/modules/catalog/api/CAP46Seam.java")),
                Files.readString(Path.of("src/main/java/coverstory/modules/catalog/api/CAP50Seam.java")))) {
            assertTrue(source.contains("LEARNER TODO"));
            assertTrue(source.contains("UnsupportedOperationException"));
        }
    }

    @Test void keyIdentityFixtureContainsDelimiterAndInvalidComponentCases() throws Exception {
        JsonNode fixture = read("CAP-165");
        assertEquals("CAP-165-synthetic-v1", fixture.path("fixture_id").asText());
        assertEquals("CAP-165-CS-01", fixture.path("cases").get(0).path("id").asText());
        assertEquals("a:b", fixture.path("cases").get(0).path("pairs").get(0).get(0).asText());
        assertTrue(fixture.path("cases").get(3).path("invalid").get(0).isNull());
    }

    @Test void boundedQueueFixtureDefinesCapacityAndAllFourOperations() throws Exception {
        JsonNode fixture = read("CAP-46");
        assertEquals(2, fixture.path("capacity").asInt());
        assertEquals(4, fixture.path("cases").size());
        assertEquals("CAP-46-CS-04", fixture.path("cases").get(3).path("id").asText());
        assertEquals(4, fixture.path("cases").get(3).path("operations").size());
    }

    @Test void importFixtureSeparatesAcceptAndDuplicateOracle() throws Exception {
        JsonNode fixture = read("CAP-50");
        assertEquals(4, fixture.path("decisions").size());
        assertEquals("ACCEPT", fixture.path("decisions").get(0).path("decision").asText());
        assertEquals("DUPLICATE", fixture.path("decisions").get(1).path("decision").asText());
        assertEquals(2, fixture.path("expected_counts").path("E").asInt());
        assertEquals("CAP-50-CS-04", fixture.path("cases").get(3).path("id").asText());
    }

    private JsonNode read(String id) throws Exception {
        return mapper.readTree(Files.readString(Path.of("fixtures/coverstory", id, "synthetic-v1.json")));
    }
}
