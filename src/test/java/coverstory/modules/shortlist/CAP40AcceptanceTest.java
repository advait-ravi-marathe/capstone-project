package coverstory.modules.shortlist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import coverstory.modules.shortlist.api.CAP40Seam;
import coverstory.modules.shortlist.api.SelectionInput;
import coverstory.modules.shortlist.api.SelectionKey;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** CAP-40 learner acceptance contract; failures are expected until uniqueSelections is implemented. */
@Tag("learnerAcceptance")
class CAP40AcceptanceTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final CAP40Seam seam = new CAP40Seam();

    @Test
    void preservesFirstSeenOrderAndDoesNotMutateInput() throws Exception {
        JsonNode sample = caseById("CAP-40-CS-01");
        List<SelectionInput> input = inputs(sample.path("input"));
        List<SelectionInput> before = List.copyOf(input);

        assertEquals(keys(sample.path("expected")), seam.uniqueSelections(input));
        assertEquals(before, input);
    }

    @Test
    void returnsEmptyForEmptyInput() throws Exception {
        JsonNode sample = caseById("CAP-40-CS-02");
        assertEquals(keys(sample.path("expected")), seam.uniqueSelections(inputs(sample.path("input"))));
    }

    @Test
    void deviceScopeIsPartOfTheCompositeKey() throws Exception {
        JsonNode sample = caseById("CAP-40-CS-03");
        assertEquals(keys(sample.path("expected")), seam.uniqueSelections(inputs(sample.path("input"))));
    }

    @Test
    void rejectsBlankCatalogVersionWithoutReturningPartialResults() {
        assertThrows(IllegalArgumentException.class, () -> seam.uniqueSelections(List.of(
                new SelectionInput("device-A", "health-A-v1"),
                new SelectionInput("device-A", " "))));
    }

    @Test
    void rejectsBlankDeviceScope() {
        assertThrows(IllegalArgumentException.class,
                () -> seam.uniqueSelections(List.of(new SelectionInput(" ", "health-A-v1"))));
    }

    @Test
    void rejectsNullList() {
        assertRejects(() -> seam.uniqueSelections(null));
    }

    @Test
    void rejectsNullElement() {
        List<SelectionInput> input = new ArrayList<>();
        input.add(new SelectionInput("device-A", "health-A-v1"));
        input.add(null);
        assertRejects(() -> seam.uniqueSelections(input));
    }

    private static JsonNode caseById(String id) throws Exception {
        JsonNode cases = MAPPER.readTree(Files.readString(
                Path.of("fixtures/coverstory/CAP-40/synthetic-v1.json"))).path("cases");
        for (JsonNode candidate : cases) {
            if (id.equals(candidate.path("id").asText())) return candidate;
        }
        throw new IllegalArgumentException("Fixture case not found: " + id);
    }

    private static List<SelectionInput> inputs(JsonNode node) {
        return MAPPER.convertValue(node, new TypeReference<>() { });
    }

    private static List<SelectionKey> keys(JsonNode node) {
        return MAPPER.convertValue(node, new TypeReference<>() { });
    }

    private static void assertRejects(Runnable action) {
        RuntimeException exception = assertThrows(RuntimeException.class, action::run);
        assertFalse(exception instanceof UnsupportedOperationException,
                "CAP40_TODO_STUB_REACHED: validation behavior is unassessed until the learner implements the seam");
    }
}
