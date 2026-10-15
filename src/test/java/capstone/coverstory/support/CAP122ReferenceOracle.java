package capstone.coverstory.support;

import capstone.coverstory.support.internal.EvidenceRerankEvaluator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Independent offline reference rules; this is not a production retriever or model client. */
final class CAP122ReferenceOracle {
    private CAP122ReferenceOracle() {}

    static EvidenceRerankEvaluator.Evaluation evaluate(List<EvidenceRerankEvaluator.Candidate> authorized,
            List<String> proposed, int elapsedMillis, int deadlineMillis) {
        if (elapsedMillis > deadlineMillis) {
            return new EvidenceRerankEvaluator.Evaluation(EvidenceRerankEvaluator.Status.TIMEOUT_FALLBACK,
                    authorized.stream().map(EvidenceRerankEvaluator.Candidate::id).toList(), elapsedMillis, true, 1);
        }
        Set<String> known = new HashSet<>();
        for (var item : authorized) known.add(item.id());
        if (proposed.size() != known.size() || new HashSet<>(proposed).size() != proposed.size()
                || !known.containsAll(proposed)) {
            return new EvidenceRerankEvaluator.Evaluation(EvidenceRerankEvaluator.Status.INVALID_RERANK,
                    List.of(), elapsedMillis, false, 1);
        }
        return new EvidenceRerankEvaluator.Evaluation(EvidenceRerankEvaluator.Status.ACCEPTED,
                List.copyOf(proposed), elapsedMillis, false, 1);
    }

    static double recallAt2(List<EvidenceRerankEvaluator.Candidate> candidates, List<String> ranked) {
        long relevant = candidates.stream().filter(EvidenceRerankEvaluator.Candidate::relevant).count();
        if (relevant == 0) return 0;
        Set<String> top = Set.copyOf(ranked.subList(0, Math.min(2, ranked.size())));
        return (double) candidates.stream().filter(c -> c.relevant() && top.contains(c.id())).count() / relevant;
    }

    static double reciprocalRank(List<EvidenceRerankEvaluator.Candidate> candidates, List<String> ranked) {
        for (int i = 0; i < ranked.size(); i++) {
            String id = ranked.get(i);
            if (candidates.stream().anyMatch(c -> c.id().equals(id) && c.relevant())) return 1.0 / (i + 1);
        }
        return 0;
    }
}
