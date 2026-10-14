package coverstory.modules.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import coverstory.modules.catalog.api.CAP55Seam;
import coverstory.modules.catalog.api.CAP55Seam.CatalogPublicationEnvelope;
import coverstory.modules.catalog.api.CAP55Seam.RecordedObservation;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Offline fixture/oracle self-check. It neither installs nor starts Kafka. */
class CAP55InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-55/synthetic-v1.json");

    @Test void fixtureIsSyntheticAndContainsTheExactRecordedOffsets() throws Exception {
        JsonNode root = JSON.readTree(Files.readString(FIXTURE));
        assertEquals("SYNTHETIC_OFFLINE_TRACE", root.path("classification").asText());
        assertFalse(root.path("actual_broker_run").asBoolean());
        assertEquals(4, root.path("cases").size());
        JsonNode observations = caseById(root, "CAP-55-CS-01").path("observations");
        assertEquals(List.of(10, 11, 12), values(observations, "offset"));
        assertEquals(List.of(0, 0, 0), values(observations, "partition"));
    }

    @Test void positiveWitnessPassesAndNegativeWitnessesCatchOrderLocalityAndEvidenceClaims() throws Exception {
        JsonNode root = JSON.readTree(Files.readString(FIXTURE));
        List<JsonNode> validRows = List.of(caseById(root, "CAP-55-CS-01"), caseById(root, "CAP-55-CS-02"));
        List<RecordedObservation> trace = new ArrayList<>();
        List<CatalogPublicationEnvelope> pubs = new ArrayList<>();
        for (JsonNode row : validRows) {
            trace.addAll(observations(row)); pubs.addAll(publications(row));
        }
        assertTrue(referenceValid(pubs, trace), "recorded same-key order plus shared partition is valid");

        List<RecordedObservation> reordered = new ArrayList<>(trace);
        reordered.set(0, withOffset(reordered.get(0), 11));
        reordered.set(1, withOffset(reordered.get(1), 10));
        assertFalse(referenceValid(pubs, reordered), "out-of-order offsets for one source key must fail");

        List<RecordedObservation> split = new ArrayList<>(observations(validRows.get(0)));
        split.set(2, withPartition(split.get(2), 1));
        assertFalse(referenceValid(publications(validRows.get(0)), split), "one source key cannot move partitions");

        List<RecordedObservation> falseBrokerClaim = new ArrayList<>(observations(validRows.get(0)));
        falseBrokerClaim.set(0, new RecordedObservation("CAP-55-synthetic-v1", "recorded-partition-trace-v1", true,
                "source-A", "v1", 1, 0, 10L));
        assertFalse(referenceValid(publications(validRows.get(0)), falseBrokerClaim), "fixture offsets cannot claim a live broker run");

        JsonNode blankKey = caseById(root, "CAP-55-CS-03");
        assertFalse(referenceValid(publications(blankKey), observations(blankKey)), "blank source keys fail before a producer");
        JsonNode missingEvidence = caseById(root, "CAP-55-CS-04");
        assertFalse(referenceValid(publications(missingEvidence), observations(missingEvidence)), "metadata and offsets cannot be invented");
    }

    private static JsonNode caseById(JsonNode root, String id) {
        for (JsonNode row : root.path("cases")) if (id.equals(row.path("id").asText())) return row;
        throw new IllegalArgumentException("Fixture case not found: " + id);
    }

    private static List<Integer> values(JsonNode rows, String key) {
        List<Integer> out = new ArrayList<>(); rows.forEach(row -> out.add(row.path(key).asInt())); return out;
    }

    private static List<RecordedObservation> observations(JsonNode row) {
        return JSON.convertValue(row.path("observations"), new TypeReference<>() { });
    }

    private static List<CatalogPublicationEnvelope> publications(JsonNode row) {
        return JSON.convertValue(row.path("publications"), new TypeReference<>() { });
    }

    private static RecordedObservation withOffset(RecordedObservation row, long offset) {
        return new RecordedObservation(row.evidenceSource(), row.fixtureVersion(), row.claimsActualBrokerRun(),
                row.sourceId(), row.version(), row.sequence(), row.partition(), offset);
    }

    private static RecordedObservation withPartition(RecordedObservation row, int partition) {
        return new RecordedObservation(row.evidenceSource(), row.fixtureVersion(), row.claimsActualBrokerRun(),
                row.sourceId(), row.version(), row.sequence(), partition, row.offset());
    }

    /** Independent instructor reference; not included in application source. */
    private static boolean referenceValid(List<CatalogPublicationEnvelope> pubs, List<RecordedObservation> rows) {
        if (rows.isEmpty() || pubs.size() != rows.size()) return false;
        var expected = new java.util.HashMap<String, CatalogPublicationEnvelope>();
        for (CatalogPublicationEnvelope pub : pubs) {
            if (pub.sourceId() == null || pub.sourceId().isBlank() || pub.version() == null || pub.version().isBlank()) return false;
            expected.put(pub.sourceId() + "\u0000" + pub.sequence(), pub);
        }
        var partitions = new java.util.HashMap<String, Integer>();
        var offsets = new java.util.HashMap<String, Long>();
        for (RecordedObservation row : rows) {
            if (!"CAP-55-synthetic-v1".equals(row.evidenceSource())
                    || !"recorded-partition-trace-v1".equals(row.fixtureVersion()) || row.claimsActualBrokerRun()
                    || row.sourceId() == null || row.sourceId().isBlank()
                    || row.version() == null || row.sequence() == null || row.partition() == null || row.offset() == null) return false;
            String identity = row.sourceId() + "\u0000" + row.sequence();
            CatalogPublicationEnvelope pub = expected.get(identity);
            if (pub == null || !pub.version().equals(row.version())) return false;
            Integer oldPartition = partitions.putIfAbsent(row.sourceId(), row.partition());
            if (oldPartition != null && oldPartition.intValue() != row.partition()) return false;
            Long oldOffset = offsets.put(row.sourceId(), row.offset());
            if (oldOffset != null && row.offset() <= oldOffset) return false;
        }
        return rows.size() == expected.size();
    }
}
