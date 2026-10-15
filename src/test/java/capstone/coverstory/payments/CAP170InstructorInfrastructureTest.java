package capstone.coverstory.payments;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Independent test-only oracle; does not call ValidatePaymentIdentities. */
class CAP170InstructorInfrastructureTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test void referenceMatchesEverySavedSyntheticCase() throws Exception {
        JsonNode fixture = fixture();
        assertEquals("CAP-170-synthetic-v1", fixture.path("fixture_id").asText());
        assertEquals(8, fixture.path("cases").size());
        for (JsonNode item : fixture.path("cases")) {
            var observed = reference(item.path("rows"));
            assertEquals(item.path("expected").asText(), observed.status().name(), item.path("id").asText());
            assertEquals(item.path("accepted_rows").asInt(), observed.acceptedRows(), item.path("id").asText());
        }
    }

    @Test void correctOwnerEventEpochReferenceAcceptsResetAndSeparatedStreams() throws Exception {
        JsonNode cases = fixture().path("cases");
        assertEquals(new AttemptLedgerReport(AttemptLedgerReport.Status.VALID, 3), reference(cases.get(0).path("rows")));
        assertEquals(new AttemptLedgerReport(AttemptLedgerReport.Status.VALID, 2), reference(cases.get(6).path("rows")));
        assertEquals(new AttemptLedgerReport(AttemptLedgerReport.Status.VALID, 2), reference(cases.get(7).path("rows")));
    }

    @Test void knownBadGlobalOwnerKeyAndSortBeforeValidationMutantsAreRejected() throws Exception {
        JsonNode cases = fixture().path("cases");
        assertNotEquals(AttemptLedgerReport.Status.VALID, ownerOmittingMutant(cases.get(6).path("rows")));
        assertNotEquals(AttemptLedgerReport.Status.VALID, reference(cases.get(2).path("rows")));
        assertEquals(AttemptLedgerReport.Status.VALID, sortBeforeCheckMutant(cases.get(2).path("rows")).status(),
                "the deliberately bad sorted-input mutant would hide the saved 2,1 ordering defect");
    }

    private AttemptLedgerReport reference(JsonNode rows) {
        Set<Key> ids = new HashSet<>();
        Map<Stream, Integer> last = new HashMap<>();
        Set<Stream> invalidStarts = new HashSet<>();
        for (JsonNode row : rows) {
            String owner = row.path("owner").asText(null);
            String event = row.path("event").asText(null);
            if (owner == null || owner.isBlank() || event == null || event.isBlank()
                    || row.path("epoch").isMissingNode() || !row.path("epoch").canConvertToLong()
                    || row.path("epoch").longValue() < 0 || row.path("attempt").isMissingNode()
                    || !row.path("attempt").canConvertToInt() || row.path("attempt").intValue() < 1)
                return new AttemptLedgerReport(AttemptLedgerReport.Status.INVALID_DOMAIN, 0);
            long epoch = row.path("epoch").longValue();
            int attempt = row.path("attempt").intValue();
            if (!ids.add(new Key(owner, event, epoch, attempt)))
                return new AttemptLedgerReport(AttemptLedgerReport.Status.DUPLICATE_ATTEMPT_ID, 0);
            Stream stream = new Stream(owner, event, epoch);
            Integer previous = last.put(stream, attempt);
            if (previous == null && attempt != 1) invalidStarts.add(stream);
            if (previous != null && attempt <= previous)
                return new AttemptLedgerReport(AttemptLedgerReport.Status.NON_MONOTONIC, 0);
        }
        if (!invalidStarts.isEmpty())
            return new AttemptLedgerReport(AttemptLedgerReport.Status.INVALID_START, 0);
        return new AttemptLedgerReport(AttemptLedgerReport.Status.VALID, rows.size());
    }

    private AttemptLedgerReport ownerOmittingMutant(JsonNode rows) {
        Set<String> ids = new HashSet<>();
        for (JsonNode row : rows) {
            String key = row.path("event").asText() + ":" + row.path("epoch").asText() + ":" + row.path("attempt").asText();
            if (!ids.add(key)) return new AttemptLedgerReport(AttemptLedgerReport.Status.DUPLICATE_ATTEMPT_ID, 0);
        }
        return new AttemptLedgerReport(AttemptLedgerReport.Status.VALID, rows.size());
    }

    private AttemptLedgerReport sortBeforeCheckMutant(JsonNode rows) {
        var sorted = mapper.createArrayNode();
        rows.forEach(sorted::add);
        var list = new java.util.ArrayList<JsonNode>();
        sorted.forEach(list::add);
        list.sort(java.util.Comparator.comparingInt(row -> row.path("attempt").asInt()));
        var normalized = mapper.createArrayNode(); list.forEach(normalized::add);
        return reference(normalized);
    }

    private JsonNode fixture() throws Exception {
        return mapper.readTree(Files.readString(Path.of("fixtures/coverstory/CAP-170/synthetic-v1.json")));
    }
    private record Key(String owner, String event, long epoch, int attempt) { }
    private record Stream(String owner, String event, long epoch) { }
}
