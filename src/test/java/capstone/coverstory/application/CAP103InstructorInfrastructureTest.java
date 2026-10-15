package capstone.coverstory.application;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Independent deterministic fixture oracle; never calls the learner seam. */
class CAP103InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-103/synthetic-v1.json");

    @Test void fixtureIsLocalSyntheticAndMakesNoCapacityOrAvailabilityClaim() throws Exception {
        JsonNode root = fixture();
        assertEquals("LOCAL_SYNTHETIC_MEASUREMENTS", root.path("classification").asText());
        assertFalse(root.path("network_calls").asBoolean());
        assertFalse(root.path("production_traffic").asBoolean());
        assertTrue(root.path("claim_scope").asText().contains("no capacity"));
        assertEquals(4, root.path("cases").size());
    }

    @Test void independentReferenceSatisfiesAllSavedCases() throws Exception {
        for (JsonNode row : fixture().path("cases")) {
            Summary result = reference(readRequests(row.path("requests")));
            JsonNode expected = row.path("expected");
            assertEquals(expected.path("total").asInt(), result.total, row.path("id").asText());
            assertEquals(expected.path("successes").asInt(), result.successes, row.path("id").asText());
            assertEquals(expected.path("errors").asInt(), result.errors, row.path("id").asText());
            assertEquals(nullableLong(expected.path("p50_ms")), result.p50, row.path("id").asText());
            assertEquals(nullableLong(expected.path("p95_ms")), result.p95, row.path("id").asText());
            assertEquals(nullableLong(expected.path("max_success_ms")), result.maxSuccess, row.path("id").asText());
            assertEquals(nullableLong(expected.path("error_max_ms")), result.errorMax, row.path("id").asText());
            List<String> diagnosticCodes = new ArrayList<>();
            expected.path("diagnostics").forEach(code -> diagnosticCodes.add(code.asText()));
            assertEquals(diagnosticCodes, result.diagnostics, row.path("id").asText());
        }
    }

    @Test void witnessesRejectFloorPercentileMixedErrorsAndZeroForEmptyMutants() throws Exception {
        List<Sample> successes = readRequests(caseById("CAP-103-CS-01").path("requests"));
        Summary correct = reference(successes);
        assertEquals(50L, correct.p50);
        assertEquals(100L, correct.p95);
        assertNotEquals(correct.p95, floorRankMutant(successes, .95),
                "floor((n-1)q) selects 90ms and must be rejected for the declared nearest-rank method");

        List<Sample> mixed = readRequests(caseById("CAP-103-CS-02").path("requests"));
        Summary successOnly = reference(mixed);
        assertEquals(100L, successOnly.p95);
        assertEquals(500L, mixedAllOutcomesP95Mutant(mixed),
                "including failed requests in a success percentile contaminates the result");
        assertNull(reference(readRequests(caseById("CAP-103-CS-04").path("requests"))).p95);
        assertEquals(0L, emptyAsZeroMutant(List.of()), "the empty-population mutant must differ from null");
    }

    private static Summary reference(List<Sample> samples) {
        int total = samples.size();
        int successes = 0, errors = 0;
        List<Long> successful = new ArrayList<>();
        List<Long> failed = new ArrayList<>();
        List<String> diagnostics = new ArrayList<>();
        for (Sample sample : samples) {
            if (sample.end < sample.start) {
                diagnostics.add("INVALID_DURATION");
                continue;
            }
            long elapsed = sample.end - sample.start;
            if (sample.success) { successes++; successful.add(elapsed); }
            else { errors++; failed.add(elapsed); }
        }
        successful.sort(Comparator.naturalOrder());
        return new Summary(total, successes, errors, percentile(successful, .50), percentile(successful, .95),
                successful.isEmpty() ? null : successful.get(successful.size() - 1),
                failed.isEmpty() ? null : failed.stream().max(Long::compareTo).orElseThrow(), diagnostics);
    }

    private static Long percentile(List<Long> sorted, double q) {
        if (sorted.isEmpty()) return null;
        int oneBasedRank = (int) Math.ceil(q * sorted.size());
        return sorted.get(oneBasedRank - 1);
    }

    /** Mutant witness: uses floor((n-1)q) rather than the declared nearest rank. */
    private static long floorRankMutant(List<Sample> samples, double q) {
        List<Long> sorted = new ArrayList<>();
        for (Sample s : samples) if (s.success && s.end >= s.start) sorted.add(s.end - s.start);
        sorted.sort(Comparator.naturalOrder());
        return sorted.get((int) Math.floor((sorted.size() - 1) * q));
    }

    /** Mutant witness: incorrectly puts errors into the success-latency distribution. */
    private static long mixedAllOutcomesP95Mutant(List<Sample> samples) {
        List<Long> sorted = new ArrayList<>();
        for (Sample s : samples) if (s.end >= s.start) sorted.add(s.end - s.start);
        sorted.sort(Comparator.naturalOrder());
        return sorted.get((int) Math.ceil(.95 * sorted.size()) - 1);
    }

    /** Mutant witness: turns an absent success population into a fabricated zero. */
    private static long emptyAsZeroMutant(List<Long> values) { return values.isEmpty() ? 0L : values.get(0); }

    private static List<Sample> readRequests(JsonNode requests) {
        List<Sample> result = new ArrayList<>();
        requests.forEach(r -> result.add(new Sample(r.path("start_ms").asLong(), r.path("end_ms").asLong(),
                "SUCCESS".equals(r.path("status").asText()))));
        return result;
    }

    private static JsonNode fixture() throws Exception { return JSON.readTree(Files.readString(FIXTURE)); }
    private static JsonNode caseById(String id) throws Exception {
        for (JsonNode row : fixture().path("cases")) if (id.equals(row.path("id").asText())) return row;
        throw new IllegalArgumentException("Fixture case not found: " + id);
    }
    private static Long nullableLong(JsonNode node) { return node.isNull() ? null : node.asLong(); }

    private record Sample(long start, long end, boolean success) { }
    private record Summary(int total, int successes, int errors, Long p50, Long p95, Long maxSuccess,
                           Long errorMax, List<String> diagnostics) { }
}
