package capstone.coverstory.support;

import capstone.coverstory.support.internal.PolicyRetrievalMetric;
import java.util.*;

/** Independent instructor oracle for the fixed synthetic ranking fixture. */
final class CAP118ReferencePolicyRetrievalMetric {
    private CAP118ReferencePolicyRetrievalMetric() { }

    static PolicyRetrievalMetric.Score score(List<PolicyRetrievalMetric.Query> queries,
                                               PolicyRetrievalMetric.Candidate candidate, int k) {
        if (k < 1 || queries.isEmpty()) throw new IllegalArgumentException("INVALID_DATASET");
        Map<String, String> relevance = new LinkedHashMap<>();
        for (var q : queries) {
            if (q.id() == null || q.relevantSourceId() == null || relevance.put(q.id(), q.relevantSourceId()) != null)
                throw new IllegalArgumentException("INVALID_DATASET");
        }
        Map<String, List<String>> rows = new HashMap<>();
        for (var row : candidate.rankings()) {
            if (!relevance.containsKey(row.queryId()) || rows.put(row.queryId(), row.sourceIds()) != null)
                throw new IllegalArgumentException("INVALID_DATASET");
        }
        if (!rows.keySet().equals(relevance.keySet())) throw new IllegalArgumentException("INVALID_DATASET");
        List<Integer> ranks = new ArrayList<>(); List<String> errors = new ArrayList<>();
        boolean safe = true; int hits = 0; double reciprocal = 0;
        for (var q : queries) {
            List<String> sources = rows.get(q.id());
            for (String source : sources) if (!candidate.authorizedSourceIds().contains(source)) {
                safe = false; errors.add("UNAUTHORIZED_SOURCE:" + q.id() + ":" + source);
            }
            int rank = -1;
            for (int i = 0; i < sources.size(); i++) if (q.relevantSourceId().equals(sources.get(i))) { rank = i + 1; break; }
            ranks.add(rank < 0 ? null : rank);
            if (rank > 0) { reciprocal += 1.0 / rank; if (rank <= k) hits++; }
        }
        return new PolicyRetrievalMetric.Score((double) hits / queries.size(), reciprocal / queries.size(),
                queries.size(), Collections.unmodifiableList(new ArrayList<>(ranks)), safe, List.copyOf(errors));
    }
}
