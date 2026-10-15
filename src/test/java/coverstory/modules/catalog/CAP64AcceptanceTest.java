package coverstory.modules.catalog;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import coverstory.modules.catalog.api.CAP64Seam;
import coverstory.modules.catalog.api.CAP64Seam.CatalogPublication;
import coverstory.modules.catalog.api.CAP64Seam.ProducerAck;
import coverstory.modules.catalog.api.CAP64Seam.PublishResult;
import coverstory.modules.catalog.api.SourceVersionKey;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP64AcceptanceTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-64/synthetic-v1.json");

    @Test void publishesExactSchemaAndTypedKeyOnlyAfterRecordedFakeAck() throws Exception {
        JsonNode row = caseById("CAP-64-CS-01");
        AtomicReference<String> topic = new AtomicReference<>();
        AtomicReference<SourceVersionKey> key = new AtomicReference<>();
        AtomicReference<byte[]> body = new AtomicReference<>();
        CAP64Seam seam = new CAP64Seam((t, k, bytes, callback) -> {
            topic.set(t); key.set(k); body.set(bytes);
            callback.accept(new ProducerAck(1, 7), null);
        }, Duration.ofMillis(50));

        PublishResult result = seam.publish(publication(row.path("input"))).toCompletableFuture().get(250, TimeUnit.MILLISECONDS);

        assertEquals("cs.catalog.publication.v1", topic.get());
        assertEquals("health-source-A", key.get().sourceId());
        assertEquals("v1", key.get().sourceVersion());
        assertEquals(row.path("expected_envelope"), JSON.readTree(body.get()));
        assertEquals("SIMULATED_ACK", result.outcome().name());
        assertEquals(1, result.partition());
        assertEquals(7L, result.offset());
    }

    @Test void callbackTimeoutHasNoSuccessMetadata() throws Exception {
        JsonNode row = caseById("CAP-64-CS-02");
        CAP64Seam seam = new CAP64Seam((topic, key, body, callback) -> { }, Duration.ofMillis(20));
        PublishResult result = seam.publish(validPublication()).toCompletableFuture().get(500, TimeUnit.MILLISECONDS);
        assertEquals(row.path("expected_outcome").asText(), result.outcome().name());
        assertNull(result.partition());
        assertNull(result.offset());
    }

    @Test void invalidUuidOrMissingVersionDoesNotCallProducer() throws Exception {
        JsonNode row = caseById("CAP-64-CS-03");
        AtomicInteger sends = new AtomicInteger();
        CAP64Seam seam = new CAP64Seam((topic, key, body, callback) -> sends.incrementAndGet(), Duration.ofMillis(50));
        for (JsonNode invalid : row.path("invalid_inputs")) {
            CatalogPublication event = new CatalogPublication(invalid.path("catalog_version_id").asText(),
                    invalid.path("source_id").asText(), invalid.path("source_version").asText(),
                    7, "SYNTHETIC", "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            PublishResult result = seam.publish(event).toCompletableFuture().get(250, TimeUnit.MILLISECONDS);
            assertEquals("VALIDATION_ERROR", result.outcome().name());
        }
        assertEquals(0, sends.get());
    }

    @Test void duplicateCallbackCompletesOneResultWithoutGlobalExactlyOnceClaim() throws Exception {
        AtomicInteger completions = new AtomicInteger();
        JsonNode row = caseById("CAP-64-CS-04");
        CAP64Seam seam = new CAP64Seam((topic, key, body, callback) -> {
            callback.accept(new ProducerAck(1, 7), null);
            callback.accept(new ProducerAck(1, 8), null);
        }, Duration.ofMillis(50));
        CompletableFuture<PublishResult> result = seam.publish(validPublication()).toCompletableFuture();
        result.whenComplete((value, error) -> completions.incrementAndGet());
        assertEquals("SIMULATED_ACK", result.get(250, TimeUnit.MILLISECONDS).outcome().name());
        assertEquals(1, completions.get());
        assertFalse(row.path("physical_delivery_exactly_once_claim").asBoolean());
    }

    private static CatalogPublication validPublication() throws Exception {
        return publication(caseById("CAP-64-CS-01").path("input"));
    }

    private static CatalogPublication publication(JsonNode input) {
        return new CatalogPublication(input.path("catalog_version_id").asText(), input.path("source_id").asText(),
                input.path("source_version").asText(), input.path("catalog_revision").asLong(),
                input.path("evidence_status").asText(), input.path("payload_hash").asText());
    }

    private static JsonNode caseById(String id) throws Exception {
        for (JsonNode row : JSON.readTree(Files.readString(FIXTURE)).path("cases"))
            if (id.equals(row.path("id").asText())) return row;
        throw new IllegalArgumentException("Fixture case not found: " + id);
    }
}
