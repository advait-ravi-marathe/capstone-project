package coverstory.modules.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import coverstory.modules.catalog.api.SourceVersionKey;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Resettable instructor oracle for the CAP-46 FIFO contract; never invokes the learner seam. */
class CAP46InstructorInfrastructureTest {
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-46/synthetic-v1.json");

    @Test void fixtureHasSyntheticCapacityAndFourOperationVectors() throws Exception {
        JsonNode fixture = fixture();
        assertEquals("CAP-46-synthetic-v1", fixture.path("fixture_id").asText());
        assertTrue(fixture.path("synthetic").asBoolean());
        assertEquals(2, fixture.path("capacity").asInt());
        assertEquals(4, fixture.path("cases").size());
    }

    @Test void independentQueueOracleMatchesAllSavedCasesAndResetsPerCase() throws Exception {
        JsonNode fixture = fixture();
        for (JsonNode row : fixture.path("cases")) {
            int capacity = row.has("capacity") ? row.path("capacity").asInt() : fixture.path("capacity").asInt();
            assertEquals(strings(row.path("expected")), runOracle(fixture, row, capacity), row.path("id").asText());
        }
        JsonNode first = fixture.path("cases").get(0);
        assertEquals(strings(first.path("expected")), runOracle(fixture, first, fixture.path("capacity").asInt()),
                "a second run starts with an empty queue");
    }

    @Test void knownBadOverflowLifoAndPermanentDuplicateMutantsAreRejected() throws Exception {
        JsonNode fixture = fixture();
        JsonNode full = fixture.path("cases").get(2);
        List<String> expected = strings(full.path("expected"));
        assertNotEquals(expected, runUnboundedOverflowMutant(fixture, full, 1),
                "an unbounded queue must be caught by the capacity-one vector");

        JsonNode fifo = fixture.path("cases").get(0);
        assertNotEquals(strings(fifo.path("expected")), runLifoMutant(fixture, fifo, 2),
                "a stack must be caught by the two-item order vector");

        JsonNode requeue = fixture.path("cases").get(3);
        assertNotEquals(strings(requeue.path("expected")), runPermanentDuplicateMutant(fixture, requeue, 1),
                "duplicate identity must become enqueueable after its pending item is polled");
    }

    private static JsonNode fixture() throws Exception {
        return new ObjectMapper().readTree(Files.readString(FIXTURE));
    }

    private static List<String> strings(JsonNode values) {
        List<String> result = new ArrayList<>();
        values.forEach(value -> result.add(value.isNull() ? "null" : value.isTextual() ? value.asText() : value.asText()));
        return result;
    }

    private static List<String> runOracle(JsonNode fixture, JsonNode testCase, int capacity) {
        ArrayDeque<SourceVersionKey> fifo = new ArrayDeque<>();
        Set<SourceVersionKey> pending = new HashSet<>();
        List<String> outcomes = new ArrayList<>();
        for (JsonNode operation : testCase.path("operations")) {
            String op = operation.asText();
            if (op.startsWith("enqueue-")) {
                int index = Integer.parseInt(op.substring("enqueue-".length()));
                JsonNode pair = fixture.path("keys").get(index);
                SourceVersionKey key = new SourceVersionKey(pair.get(0).asText(), pair.get(1).asText());
                boolean accepted = fifo.size() < capacity && pending.add(key);
                if (accepted) fifo.addLast(key);
                outcomes.add(Boolean.toString(accepted));
            } else if (op.equals("poll")) {
                SourceVersionKey key = fifo.pollFirst();
                if (key != null) pending.remove(key);
                outcomes.add(key == null ? "null" : key.sourceId() + "/" + key.sourceVersion());
            } else throw new IllegalArgumentException("Unknown saved operation: " + op);
        }
        if (testCase.path("expected").size() > testCase.path("operations").size())
            outcomes.add(Integer.toString(fifo.size()));
        return outcomes;
    }

    private static List<String> runUnboundedOverflowMutant(JsonNode fixture, JsonNode testCase, int ignoredCapacity) {
        ArrayDeque<SourceVersionKey> queue = new ArrayDeque<>();
        Set<SourceVersionKey> pending = new HashSet<>();
        List<String> out = new ArrayList<>();
        for (JsonNode item : testCase.path("operations")) {
            String op = item.asText();
            if (op.startsWith("enqueue-")) {
                JsonNode pair = fixture.path("keys").get(Integer.parseInt(op.substring(8)));
                SourceVersionKey key = new SourceVersionKey(pair.get(0).asText(), pair.get(1).asText());
                boolean accepted = pending.add(key);
                if (accepted) queue.addLast(key);
                out.add(Boolean.toString(accepted));
            } else {
                SourceVersionKey key = queue.pollFirst();
                if (key != null) pending.remove(key);
                out.add(key == null ? "null" : key.sourceId() + "/" + key.sourceVersion());
            }
        }
        return out;
    }

    private static List<String> runLifoMutant(JsonNode fixture, JsonNode testCase, int capacity) {
        ArrayList<SourceVersionKey> stack = new ArrayList<>();
        Set<SourceVersionKey> pending = new HashSet<>();
        List<String> out = new ArrayList<>();
        for (JsonNode item : testCase.path("operations")) {
            String op = item.asText();
            if (op.startsWith("enqueue-")) {
                JsonNode pair = fixture.path("keys").get(Integer.parseInt(op.substring(8)));
                SourceVersionKey key = new SourceVersionKey(pair.get(0).asText(), pair.get(1).asText());
                boolean accepted = stack.size() < capacity && pending.add(key);
                if (accepted) stack.add(key);
                out.add(Boolean.toString(accepted));
            } else {
                SourceVersionKey key = stack.isEmpty() ? null : stack.remove(stack.size() - 1);
                if (key != null) pending.remove(key);
                out.add(key == null ? "null" : key.sourceId() + "/" + key.sourceVersion());
            }
        }
        if (testCase.path("expected").size() > testCase.path("operations").size()) out.add(Integer.toString(stack.size()));
        return out;
    }

    private static List<String> runPermanentDuplicateMutant(JsonNode fixture, JsonNode testCase, int capacity) {
        ArrayDeque<SourceVersionKey> queue = new ArrayDeque<>();
        Set<SourceVersionKey> everSeen = new HashSet<>();
        List<String> out = new ArrayList<>();
        for (JsonNode item : testCase.path("operations")) {
            String op = item.asText();
            if (op.startsWith("enqueue-")) {
                JsonNode pair = fixture.path("keys").get(Integer.parseInt(op.substring(8)));
                SourceVersionKey key = new SourceVersionKey(pair.get(0).asText(), pair.get(1).asText());
                boolean accepted = queue.size() < capacity && everSeen.add(key);
                if (accepted) queue.addLast(key);
                out.add(Boolean.toString(accepted));
            } else {
                SourceVersionKey key = queue.pollFirst();
                out.add(key == null ? "null" : key.sourceId() + "/" + key.sourceVersion());
            }
        }
        return out;
    }
}
