package coverstory.modules.shortlist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CAP40ScaffoldInfrastructureTest {
    @Test
    void syntheticFixtureHasExpectedContractShape() throws Exception {
        JsonNode fixture = new ObjectMapper().readTree(Files.readString(
                Path.of("fixtures/coverstory/CAP-40/synthetic-v1.json")));
        assertEquals("CAP-40", fixture.path("contract").asText());
        assertEquals("SYNTHETIC", fixture.path("classification").asText());
        assertEquals(3, fixture.path("cases").size());
    }
}
