package coverstory.modules.shortlist;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
    void preservesEncounterOrderWhenLexicalSortWouldReverseIt() {
        List<SelectionInput> input = List.of(
                new SelectionInput("device-A", "health-Z-v1"),
                new SelectionInput("device-A", "health-A-v1"));
        List<SelectionKey> expected = List.of(
                new SelectionKey("device-A", "health-Z-v1"),
                new SelectionKey("device-A", "health-A-v1"));
        assertEquals(expected, seam.uniqueSelections(input),
                "sorting by catalog version is a contract violation");
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
        expectExceptionOrTodo(IllegalArgumentException.class, () -> seam.uniqueSelections(List.of(
                new SelectionInput("device-A", "health-A-v1"),
                new SelectionInput("device-A", " "))));
    }

    @Test
    void rejectsBlankDeviceScope() {
        expectExceptionOrTodo(IllegalArgumentException.class,
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
        expectExceptionOrTodo(RuntimeException.class, action);
    }

    private static void expectExceptionOrTodo(Class<? extends RuntimeException> expected, Runnable action) {
        try { action.run(); fail("expected " + expected.getSimpleName()); }
        catch (UnsupportedOperationException todo) { throw todo; }
        catch (RuntimeException actual) { assertTrue(expected.isInstance(actual), "unexpected exception: " + actual); }
    }
}
