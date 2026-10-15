package capstone.coverstory.support.internal;

import java.util.List;
import java.util.Set;

/**
 * Learner-owned metric seam for an offline, fixed-rank retrieval experiment.
 * A rank is one-based; null means the query has no relevant result in the candidate list.
 */
public final class PolicyRetrievalMetric {
    private PolicyRetrievalMetric() { }

    public record Query(String id, String relevantSourceId) { }
    public record RankedQuery(String queryId, List<String> sourceIds) { }
    public record Candidate(String name, List<RankedQuery> rankings, Set<String> authorizedSourceIds) { }
    public record Score(double recallAtK, double meanReciprocalRank, int queryCount,
                        List<Integer> firstRelevantRanks, boolean scopeSafe, List<String> errorIds) { }

    public static Score score(List<Query> queries, Candidate candidate, int k) {
        // TODO(CAP-118): validate the fixed query set, rank the first relevant source,
        // compute Recall@k and MRR, and fail closed when any candidate source is out of scope.
        throw new UnsupportedOperationException("TODO(CAP-118): implement the bounded retrieval metric");
    }
}
