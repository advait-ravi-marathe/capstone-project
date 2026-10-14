package coverstory.modules.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import coverstory.modules.catalog.api.CAP46Seam;
import coverstory.modules.catalog.api.SourceVersionKey;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Tag("learnerAcceptance")
class CAP46AcceptanceTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test void preservesFifoOrderAndDrainsToZero() {
        JsonNode fixture = fixture();
        var queue = new CAP46Seam(fixture.path("capacity").asInt());
        var a = key(fixture.path("keys").get(0)); var b = key(fixture.path("keys").get(1));
        assertTrue(queue.enqueue(a)); assertTrue(queue.enqueue(b));
        assertEquals(a, queue.poll().orElseThrow()); assertEquals(b, queue.poll().orElseThrow());
        assertEquals(0, queue.size());
    }

    @Test void emptyPollIsOptionalEmpty() { assertTrue(new CAP46Seam(1).poll().isEmpty()); }

    @Test void fullQueueRejectsWithoutDroppingExistingEntry() {
        var queue = new CAP46Seam(1); var a = new SourceVersionKey("health-A", "v1");
        assertTrue(queue.enqueue(a)); assertFalse(queue.enqueue(new SourceVersionKey("health-B", "v1")));
        assertEquals(a, queue.poll().orElseThrow());
    }

    @Test void duplicateIsPendingOnlyAndCanBeRequeuedAfterPoll() {
        var queue = new CAP46Seam(1); var a = new SourceVersionKey("health-A", "v1");
        assertTrue(queue.enqueue(a)); assertFalse(queue.enqueue(a));
        assertEquals(a, queue.poll().orElseThrow()); assertTrue(queue.enqueue(a));
    }

    private JsonNode fixture() {
        try { return mapper.readTree(Files.readString(Path.of("fixtures/coverstory/CAP-46/synthetic-v1.json"))); }
        catch (Exception e) { throw new IllegalStateException("CAP-46 synthetic fixture unavailable", e); }
    }
    private static SourceVersionKey key(JsonNode node) { return new SourceVersionKey(node.get(0).asText(), node.get(1).asText()); }
}
