package coverstory.modules.catalog;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import coverstory.modules.catalog.api.CAP165Seam;
import coverstory.modules.catalog.api.SourceVersionKey;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP165AcceptanceTest {
    private final CAP165Seam seam = new CAP165Seam();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test void componentsCannotCollideThroughDelimiterConcatenation() throws Exception {
        JsonNode pairs = fixture().path("cases").get(0).path("pairs");
        assertNotEquals(key(pairs.get(0)), key(pairs.get(1)));
    }

    @Test void equalComponentsHaveStableValueEqualityAndHash() throws Exception {
        JsonNode pair = fixture().path("cases").get(1).path("pair");
        var a = key(pair); var b = key(pair);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(1, new HashSet<>(Set.of(a, b)).size());
    }

    @Test void sourceVersionsRemainDistinct() throws Exception {
        JsonNode pairs = fixture().path("cases").get(2).path("pairs");
        assertNotEquals(key(pairs.get(0)), key(pairs.get(1)));
    }

    @Test void rejectsMissingBlankAndPaddedComponentsWithoutTrimming() throws Exception {
        JsonNode invalid = fixture().path("cases").get(3).path("invalid");
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> seam.create(null, "v1")),
                () -> assertThrows(IllegalArgumentException.class, () -> seam.create("source", invalid.get(1).asText())),
                () -> assertThrows(IllegalArgumentException.class, () -> seam.create("source", invalid.get(2).asText())));
    }

    private SourceVersionKey key(JsonNode pair) { return seam.create(pair.get(0).asText(), pair.get(1).asText()); }
    private JsonNode fixture() throws Exception { return mapper.readTree(Files.readString(Path.of("fixtures/coverstory/CAP-165/synthetic-v1.json"))); }
}
