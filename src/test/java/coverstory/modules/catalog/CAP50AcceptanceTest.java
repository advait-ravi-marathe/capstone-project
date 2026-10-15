package coverstory.modules.catalog;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import coverstory.modules.catalog.api.CAP50Seam;
import coverstory.modules.catalog.api.ImportDecision;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP50AcceptanceTest {
    private final CAP50Seam seam = new CAP50Seam();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test void countsAcceptedProcessingRecordsByExactEditor() throws Exception {
        var result = seam.acceptedCounts(mapper.convertValue(fixture().path("decisions"),
                mapper.getTypeFactory().constructCollectionType(List.class, ImportDecision.class)));
        assertEquals(java.util.Map.of("E", 2L, "F", 1L), result);
        assertThrows(UnsupportedOperationException.class, () -> result.put("X", 1L));
    }

    @Test void conflictOnlyProducesEmptyCount() {
        assertEquals(java.util.Map.of(), seam.acceptedCounts(List.of(new ImportDecision("E", "s1", "CONFLICT"))));
    }

    @Test void rejectsBlankEditorBeforeReturningPartialMap() {
        assertThrows(IllegalArgumentException.class, () -> seam.acceptedCounts(List.of(
                new ImportDecision("E", "s1", "ACCEPT"), new ImportDecision(" ", "s2", "ACCEPT"))));
    }

    private JsonNode fixture() throws Exception {
        return mapper.readTree(Files.readString(Path.of("fixtures/coverstory/CAP-50/synthetic-v1.json")));
    }
}
