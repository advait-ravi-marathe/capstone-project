package capstone.coverstory.support;

import capstone.coverstory.support.internal.PolicyRetrievalMetric;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CAP118InstructorInfrastructureTest {
    @Test void fixtureIsLockedAndIndependentOracleReproducesExpectedMetrics() throws Exception {
        byte[] fixtureBytes = Files.readAllBytes(Path.of("docs/coverstory/fixtures/cap-118.json"));
        assertEquals("d52841de5407ab03fd3e5890b4d1d7009505e9d285ecb4b89043bccce9c334e8", HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(fixtureBytes)));
        var fixture = new ObjectMapper().readTree(new String(fixtureBytes, java.nio.charset.StandardCharsets.UTF_8));
        assertEquals("SYNTHETIC_OFFLINE_FIXED_RANKS", fixture.path("classification").asText()); assertFalse(fixture.path("network_enabled").asBoolean());
        assertEquals(List.of("q1","q2","q3","q4"), fixture.path("queries").valueStream().map(n -> n.path("id").asText()).toList());
        var queries = new ArrayList<PolicyRetrievalMetric.Query>();
        fixture.path("queries").forEach(n -> queries.add(new PolicyRetrievalMetric.Query(n.path("id").asText(), n.path("relevant_source_id").asText())));
        var scope = new HashSet<String>(); fixture.path("authorized_sources").forEach(n -> scope.add(n.asText()));
        var lexical = CAP118ReferencePolicyRetrievalMetric.score(queries, candidate("lexical", rankings(fixture.path("candidates").path("lexical").path("rankings")), scope), 3);
        var embedding = CAP118ReferencePolicyRetrievalMetric.score(queries, candidate("precomputed-embedding", rankings(fixture.path("candidates").path("precomputed_embedding").path("rankings")), scope), 3);
        var unsafe = CAP118ReferencePolicyRetrievalMetric.score(queries,
                candidate("unsafe", rankings(fixture.path("candidates").path("unsafe_scope_probe").path("rankings")), scope), 3);
        assertEquals(Arrays.asList(1,2,4,null), lexical.firstRelevantRanks()); assertEquals(.5, lexical.recallAtK()); assertEquals(.4375, lexical.meanReciprocalRank());
        assertEquals(Arrays.asList(2,3,1,null), embedding.firstRelevantRanks()); assertEquals(.75, embedding.recallAtK());
        assertEquals(0.4583333333333333, embedding.meanReciprocalRank(), 1e-12);
        assertFalse(unsafe.scopeSafe()); assertTrue(unsafe.errorIds().contains("UNAUTHORIZED_SOURCE:q3:br1"));
        assertEquals(.75, unsafe.recallAtK()); // Valid ranking score is observable; security veto remains independent.
    }
    @Test void wrongKAndDuplicateQueryDatasetFailClosed() {
        var queries = List.of(new PolicyRetrievalMetric.Query("q1","s1"));
        var dup = List.of(new PolicyRetrievalMetric.Query("q1","s1"), new PolicyRetrievalMetric.Query("q1","s1"));
        var candidate = candidate("x", List.of(List.of("s1")), Set.of("s1"));
        assertThrows(IllegalArgumentException.class, () -> CAP118ReferencePolicyRetrievalMetric.score(queries,candidate,0));
        assertThrows(IllegalArgumentException.class, () -> CAP118ReferencePolicyRetrievalMetric.score(dup,candidate,1));
        assertThrows(IllegalArgumentException.class, () -> CAP118ReferencePolicyRetrievalMetric.score(
                List.of(new PolicyRetrievalMetric.Query("q1", null)), candidate, 1));
    }
    private static PolicyRetrievalMetric.Candidate candidate(String name,List<List<String>> rows,Set<String> scope) {
        var ranked=new ArrayList<PolicyRetrievalMetric.RankedQuery>();
        for(int i=0;i<rows.size();i++) ranked.add(new PolicyRetrievalMetric.RankedQuery("q"+(i+1),rows.get(i)));
        return new PolicyRetrievalMetric.Candidate(name,ranked,scope);
    }
    private static List<List<String>> rankings(tools.jackson.databind.JsonNode rows) {
        return List.of("q1","q2","q3","q4").stream().<List<String>>map(q -> {
            var values = new ArrayList<String>(); rows.path(q).forEach(n -> values.add(n.asText())); return values;
        }).toList();
    }
}
