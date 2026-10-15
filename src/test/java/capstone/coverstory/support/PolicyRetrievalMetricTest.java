package capstone.coverstory.support;

import capstone.coverstory.support.internal.PolicyRetrievalMetric;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class PolicyRetrievalMetricTest {
    private final List<PolicyRetrievalMetric.Query> queries = List.of(
            new PolicyRetrievalMetric.Query("q1", "s1"), new PolicyRetrievalMetric.Query("q2", "s2"),
            new PolicyRetrievalMetric.Query("q3", "s3"), new PolicyRetrievalMetric.Query("q4", "s4"));
    private final Set<String> authorized = Set.of("s1", "s2", "s3", "s4", "d1", "d2");
    private PolicyRetrievalMetric.Candidate candidate(List<List<String>> lists) {
        List<PolicyRetrievalMetric.RankedQuery> rankings = new ArrayList<>();
        for (int i = 0; i < lists.size(); i++) {
            rankings.add(new PolicyRetrievalMetric.RankedQuery("q" + (i + 1), lists.get(i)));
        }
        return new PolicyRetrievalMetric.Candidate("fixture", rankings, authorized);
    }
    @Test void lockedLexicalAndEmbeddingMetricsMatchTheSavedExpectedValues() {
        var lexical = PolicyRetrievalMetric.score(queries, candidate(List.of(List.of("s1","d1"), List.of("d1","s2"), List.of("d1","d2","d1","s3"), List.of("d2"))), 3);
        var embedding = PolicyRetrievalMetric.score(queries, candidate(List.of(List.of("d1","s1"), List.of("d1","d2","s2"), List.of("s3","d1"), List.of("d2"))), 3);
        assertEquals(0.5, lexical.recallAtK()); assertEquals(0.4375, lexical.meanReciprocalRank());
        assertEquals(0.75, embedding.recallAtK()); assertEquals(0.4583333333333333, embedding.meanReciprocalRank(), 1e-12);
        assertEquals(Arrays.asList(1, 2, 4, null), lexical.firstRelevantRanks()); assertEquals(4, lexical.queryCount());
    }
    @Test void outOfScopeChunkIsSecurityFailureEvenIfRankingImproves() {
        var unsafe = new PolicyRetrievalMetric.Candidate("unsafe", List.of(
                new PolicyRetrievalMetric.RankedQuery("q1", List.of("s1")), new PolicyRetrievalMetric.RankedQuery("q2", List.of("s2")),
                new PolicyRetrievalMetric.RankedQuery("q3", List.of("br1", "s3")), new PolicyRetrievalMetric.RankedQuery("q4", List.of("s4"))), authorized);
        var score = PolicyRetrievalMetric.score(queries, unsafe, 3);
        assertFalse(score.scopeSafe()); assertTrue(score.errorIds().contains("UNAUTHORIZED_SOURCE:q3:br1"));
    }
    @Test void malformedOrEmptyDatasetIsNotReportedAsPerfect() {
        assertThrows(IllegalArgumentException.class, () -> PolicyRetrievalMetric.score(List.of(), candidate(List.of()), 3));
        assertThrows(IllegalArgumentException.class, () -> PolicyRetrievalMetric.score(queries, candidate(List.of(List.of(),List.of(),List.of(),List.of())), 3));
    }
    @Test void todoGuardKeepsLearnerMetricUnimplemented() {
        assertThrows(UnsupportedOperationException.class, () -> PolicyRetrievalMetric.score(queries, candidate(List.of(List.of(),List.of(),List.of(),List.of())), 3));
    }
}
