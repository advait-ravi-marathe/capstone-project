package coverstory.modules.catalog;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import coverstory.modules.catalog.api.CAP64Seam;
import coverstory.modules.catalog.api.CAP64Seam.CatalogPublication;
import coverstory.modules.catalog.api.CAP64Seam.ProducerAck;
import coverstory.modules.catalog.api.SourceVersionKey;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Instructor-only independent witnesses; no live broker or learner implementation is used. */
class CAP64InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-64/synthetic-v1.json");

    @Test void syntheticFixtureHasSchemaKeyAndAckExpectedBySavedCard() throws Exception {
        JsonNode root = JSON.readTree(Files.readString(FIXTURE));
        assertEquals("SYNTHETIC_OFFLINE_CONTRACT", root.path("classification").asText());
        assertFalse(root.path("actual_broker_run").asBoolean());
        assertEquals("cs.catalog.publication.v1", root.path("topic_label").asText());
        JsonNode valid = caseById(root, "CAP-64-CS-01");
        assertEquals(1, valid.path("expected_envelope").path("schemaVersion").asInt());
        assertEquals(1, valid.path("ack").path("partition").asInt());
        assertEquals(7, valid.path("ack").path("offset").asInt());
        assertEquals("health-source-A", valid.path("expected_key").path("source_id").asText());
        assertEquals("v1", valid.path("expected_key").path("source_version").asText());
        assertEquals(4, root.path("cases").size());
    }

    @Test void referenceWitnessAcceptsCorrectCallbackAndRejectsContractDefects() throws Exception {
        JsonNode root = JSON.readTree(Files.readString(FIXTURE));
        JsonNode valid = caseById(root, "CAP-64-CS-01");
        CatalogPublication event = publication(valid.path("input"));
        ReferenceResult accepted = referencePublish(event, new ProducerAck(1, 7), false, 0);
        assertEquals("SIMULATED_ACK", accepted.outcome);
        assertEquals(1, accepted.partition);
        assertEquals(7L, accepted.offset);
        assertEquals("cs.catalog.publication.v1", accepted.topic);
        assertEquals(new SourceVersionKey("health-source-A", "v1"), accepted.key);
        assertEquals(valid.path("expected_envelope"), JSON.readTree(accepted.body));

        assertEquals("PUBLISH_TIMEOUT", referencePublish(event, null, false, 0).outcome,
                "no callback before deadline yields no successful metadata");
        ReferenceResult timeout = referencePublish(event, null, false, 0);
        assertNull(timeout.partition);
        assertNull(timeout.offset);
        assertEquals("INVALID_SCHEMA", referencePublish(event, new ProducerAck(1, 7), false, 2).outcome,
                "schema version defect cannot be acknowledged");
        assertEquals("VALIDATION_ERROR", referencePublish(event, new ProducerAck(1, 7), true, 0).outcome,
                "invalid UUID or missing typed-key version never reaches send");
        SourceVersionKey collisionLeft = new SourceVersionKey("a:b", "c");
        SourceVersionKey collisionRight = new SourceVersionKey("a", "b:c");
        assertEquals("a:b:c", collisionLeft.sourceId() + ":" + collisionLeft.sourceVersion());
        assertEquals("a:b:c", collisionRight.sourceId() + ":" + collisionRight.sourceVersion());
        assertNotEquals(collisionLeft, collisionRight, "typed components preserve identity across delimiter collisions");

        AtomicInteger completionCount = new AtomicInteger();
        AtomicBoolean completed = new AtomicBoolean();
        AtomicReferenceResult first = new AtomicReferenceResult();
        completeOnce(completed, completionCount, first, referencePublish(event, new ProducerAck(1, 7), false, 0));
        // A later duplicate callback is ignored by the result gate; it does not
        // prove that the underlying physical delivery occurred only once.
        completeOnce(completed, completionCount, first, referencePublish(event, new ProducerAck(1, 8), false, 0));
        assertEquals(1, completionCount.get());
        assertEquals(7L, first.value.offset);
        assertFalse(caseById(root, "CAP-64-CS-04").path("physical_delivery_exactly_once_claim").asBoolean());
    }

    private static ReferenceResult referencePublish(CatalogPublication event, ProducerAck ack,
                                                    boolean invalidInput, int schemaVersion) throws Exception {
        if (invalidInput || !validUuid(event.catalogVersionId()) || blank(event.sourceVersion()))
            return ReferenceResult.failure("VALIDATION_ERROR");
        if (schemaVersion != 0 && schemaVersion != 1) return ReferenceResult.failure("INVALID_SCHEMA");
        if (ack == null) return ReferenceResult.failure("PUBLISH_TIMEOUT");
        SourceVersionKey key = new SourceVersionKey(event.sourceId(), event.sourceVersion());
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("schemaVersion", schemaVersion == 0 ? 1 : schemaVersion);
        envelope.put("catalogVersionId", event.catalogVersionId());
        envelope.put("catalogRevision", event.catalogRevision());
        envelope.put("evidenceStatus", event.evidenceStatus());
        envelope.put("payloadHash", event.payloadHash());
        return new ReferenceResult("SIMULATED_ACK", CAP64Seam.TOPIC, key,
                JSON.writeValueAsBytes(envelope), ack.partition(), ack.offset(), null);
    }

    private static void completeOnce(AtomicBoolean gate, AtomicInteger count,
                                     AtomicReferenceResult destination, ReferenceResult candidate) {
        if (gate.compareAndSet(false, true)) {
            destination.value = candidate;
            count.incrementAndGet();
        }
    }

    private static boolean validUuid(String value) {
        try { java.util.UUID.fromString(value); return true; }
        catch (RuntimeException invalid) { return false; }
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }

    private static CatalogPublication publication(JsonNode input) {
        return new CatalogPublication(input.path("catalog_version_id").asText(), input.path("source_id").asText(),
                input.path("source_version").asText(), input.path("catalog_revision").asLong(),
                input.path("evidence_status").asText(), input.path("payload_hash").asText());
    }

    private static JsonNode caseById(JsonNode root, String id) {
        for (JsonNode row : root.path("cases")) if (id.equals(row.path("id").asText())) return row;
        throw new IllegalArgumentException("Fixture case not found: " + id);
    }

    private record ReferenceResult(String outcome, String topic, SourceVersionKey key, byte[] body,
                                   Integer partition, Long offset, String safeCode) {
        static ReferenceResult failure(String code) { return new ReferenceResult(code, null, null, null, null, null, code); }
    }
    private static final class AtomicReferenceResult { private ReferenceResult value; }
}
