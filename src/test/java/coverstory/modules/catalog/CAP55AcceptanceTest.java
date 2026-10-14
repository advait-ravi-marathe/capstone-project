package coverstory.modules.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import coverstory.modules.catalog.api.CAP55Seam;
import coverstory.modules.catalog.api.CAP55Seam.CatalogPublicationEnvelope;
import coverstory.modules.catalog.api.CAP55Seam.RecordedObservation;
import coverstory.modules.catalog.api.CAP55Seam.ProbeReport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP55AcceptanceTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-55/synthetic-v1.json");
    private final CAP55Seam seam = new CAP55Seam();

    @Test void acceptsSameKeyMonotonicSequenceInOneRecordedPartition() throws Exception {
        JsonNode row = caseById("CAP-55-CS-01");
        ProbeReport report = seam.validatePartitionTrace(publications(row), observations(row));
        assertTrue(report.valid());
        assertEquals(3, report.observationCount());
    }

    @Test void acceptsDifferentKeysSharingOnePartitionWithoutInventingIsolation() throws Exception {
        JsonNode first = caseById("CAP-55-CS-01");
        JsonNode second = caseById("CAP-55-CS-02");
        List<CatalogPublicationEnvelope> pubs = new ArrayList<>(publications(first));
        pubs.addAll(publications(second));
        List<RecordedObservation> observations = new ArrayList<>(observations(first));
        observations.addAll(observations(second));
        ProbeReport report = seam.validatePartitionTrace(pubs, observations);
        assertTrue(report.valid());
        assertEquals(4, report.observationCount());
    }

    @Test void blankSourceKeyIsRejectedBeforeAnyExternalProducerCanBeUsed() throws Exception {
        JsonNode row = caseById("CAP-55-CS-03");
        ProbeReport report = seam.validatePartitionTrace(publications(row), observations(row));
        assertFalse(report.valid());
        assertEquals("INVALID_SOURCE_KEY", report.code());
    }

    @Test void missingOrFalseBrokerEvidenceIsRejectedWithoutFabricatedOffsets() throws Exception {
        JsonNode row = caseById("CAP-55-CS-04");
        ProbeReport report = seam.validatePartitionTrace(publications(row), observations(row));
        assertFalse(report.valid());
        assertEquals("INVALID_EVIDENCE", report.code());
        assertEquals(0, report.observationCount());
    }

    @Test void sequenceReorderingAndPartitionSplitAreDetected() throws Exception {
        JsonNode row = caseById("CAP-55-CS-01");
        List<CatalogPublicationEnvelope> pubs = publications(row);
        List<RecordedObservation> trace = observations(row);
        List<RecordedObservation> swapped = new ArrayList<>(trace);
        swapped.set(0, withOffset(swapped.get(0), 11L));
        swapped.set(1, withOffset(swapped.get(1), 10L));
        ProbeReport reordered = seam.validatePartitionTrace(pubs, swapped);
        assertFalse(reordered.valid());

        List<RecordedObservation> split = new ArrayList<>(trace);
        split.set(2, withPartition(split.get(2), 1));
        ProbeReport partitionSplit = seam.validatePartitionTrace(pubs, split);
        assertFalse(partitionSplit.valid());
    }

    private static JsonNode caseById(String id) throws Exception {
        for (JsonNode row : JSON.readTree(Files.readString(FIXTURE)).path("cases"))
            if (id.equals(row.path("id").asText())) return row;
        throw new IllegalArgumentException("Fixture case not found: " + id);
    }

    private static List<CatalogPublicationEnvelope> publications(JsonNode row) {
        return JSON.convertValue(row.path("publications"), new TypeReference<>() { });
    }

    private static List<RecordedObservation> observations(JsonNode row) {
        return JSON.convertValue(row.path("observations"), new TypeReference<>() { });
    }

    private static RecordedObservation withOffset(RecordedObservation row, long offset) {
        return new RecordedObservation(row.evidenceSource(), row.fixtureVersion(), row.claimsActualBrokerRun(),
                row.sourceId(), row.version(), row.sequence(), row.partition(), offset);
    }

    private static RecordedObservation withPartition(RecordedObservation row, int partition) {
        return new RecordedObservation(row.evidenceSource(), row.fixtureVersion(), row.claimsActualBrokerRun(),
                row.sourceId(), row.version(), row.sequence(), partition, row.offset());
    }
}
