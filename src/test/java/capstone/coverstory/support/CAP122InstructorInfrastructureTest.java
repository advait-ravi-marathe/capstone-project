package capstone.coverstory.support;

import capstone.coverstory.support.internal.EvidenceRerankEvaluator;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Offline fixture verifier; it never invokes a model, retrieval store, or external service. */
class CAP122InstructorInfrastructureTest {
    private static final String HASH = "2e9bfd5c96a776e3e86fdc9dc8169f3055593ae6a85441198d23f0d979e98f55";

    @Test void fixtureMetricsAndLatencyPolicyAreReproducibleOffline() throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of("docs/coverstory/fixtures/cap-122.json"));
        assertEquals(HASH, HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
        JsonNode fixture = new ObjectMapper().readTree(bytes);
        assertFalse(fixture.path("network_enabled").asBoolean());
        assertFalse(fixture.path("production_model_or_retriever").asBoolean());
        assertEquals("045c51a3c86bfa376c5a485363b233219dbb2648c27fb52b092fb52309e271ec", fixture.path("source_contract_sha256").asText());
        int baselineHits = 0, candidateHits = 0;
        double reciprocalRanks = 0;
        int exampleCount = 0;
        for (JsonNode example : fixture.path("examples")) {
            List<EvidenceRerankEvaluator.Candidate> candidates = new ArrayList<>();
            example.path("candidates").forEach(c -> candidates.add(new EvidenceRerankEvaluator.Candidate(c.path("id").asText(), c.path("relevant").asBoolean())));
            List<String> baseline = new ArrayList<>(), candidate = new ArrayList<>();
            example.path("baseline").forEach(v -> baseline.add(v.asText()));
            example.path("candidate").forEach(v -> candidate.add(v.asText()));
            baselineHits += CAP122ReferenceOracle.recallAt2(candidates, baseline) == 1.0 ? 1 : 0;
            var accepted = CAP122ReferenceOracle.evaluate(candidates, candidate, 99, fixture.path("deadline_ms").asInt());
            assertEquals(EvidenceRerankEvaluator.Status.ACCEPTED, accepted.status());
            candidateHits += CAP122ReferenceOracle.recallAt2(candidates, accepted.ids()) == 1.0 ? 1 : 0;
            reciprocalRanks += CAP122ReferenceOracle.reciprocalRank(candidates, accepted.ids());
            exampleCount++;
        }
        assertEquals(fixture.path("expected_metrics").path("example_count").asInt(), exampleCount);
        assertEquals(0.0, (double) baselineHits / exampleCount);
        assertEquals(1.0, (double) candidateHits / exampleCount);
        assertEquals(0.75, reciprocalRanks / exampleCount);
        for (JsonNode latency : fixture.path("latency_cases")) {
            var candidates = List.of(new EvidenceRerankEvaluator.Candidate("r", true), new EvidenceRerankEvaluator.Candidate("n", false));
            var result = CAP122ReferenceOracle.evaluate(candidates, List.of("n", "r"), latency.path("elapsed_ms").asInt(), fixture.path("deadline_ms").asInt());
            assertEquals(latency.path("expected").asText(), result.status().name());
            if (result.status() == EvidenceRerankEvaluator.Status.TIMEOUT_FALLBACK) {
                assertEquals(List.of("r", "n"), result.ids());
                assertTrue(result.timeoutRecorded());
                assertEquals(1, result.rerankCalls());
            }
        }
        JsonNode invalid = fixture.path("invalid_case");
        var candidateList = new ArrayList<EvidenceRerankEvaluator.Candidate>();
        fixture.path("examples").get(0).path("candidates").forEach(c -> candidateList.add(new EvidenceRerankEvaluator.Candidate(c.path("id").asText(), c.path("relevant").asBoolean())));
        List<String> invented = new ArrayList<>(); invalid.path("proposed").forEach(x -> invented.add(x.asText()));
        var rejected = CAP122ReferenceOracle.evaluate(candidateList, invented, 25, fixture.path("deadline_ms").asInt());
        assertEquals(invalid.path("expected").asText(), rejected.status().name());
        assertTrue(rejected.ids().isEmpty());
        assertEquals(0, invalid.path("fetch_count").asInt());
        assertEquals(0, invalid.path("response_evidence_count").asInt());
    }
}
