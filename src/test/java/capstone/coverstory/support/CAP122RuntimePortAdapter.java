package capstone.coverstory.support;

import coverstory.modules.support.api.EvidenceRerankingPort;
import capstone.coverstory.support.internal.EvidenceRerankEvaluator;
import java.util.List;
import java.util.UUID;

/** Test-profile adapter over frozen candidates and the independent two-case oracle; no model client. */
public final class CAP122RuntimePortAdapter implements EvidenceRerankingPort {
    public static final UUID OWNER_A = UUID.fromString("12100000-0000-4000-8000-000000000001");
    private static final List<EvidenceRerankEvaluator.Candidate> Q1 = List.of(
            new EvidenceRerankEvaluator.Candidate("s1", true), new EvidenceRerankEvaluator.Candidate("a1", false),
            new EvidenceRerankEvaluator.Candidate("b1", false), new EvidenceRerankEvaluator.Candidate("c1", false));
    private static final List<String> BASELINE = List.of("a1", "b1", "s1", "c1");
    private static final List<String> CANDIDATE = List.of("s1", "a1", "b1", "c1");

    @Override public RankingResult evaluate(UUID owner, String id) {
        if (!OWNER_A.equals(owner)) return new RankingResult("NOT_FOUND", List.of(), 0, false, 0, 0, 0);
        var allowed = Q1;
        int elapsed = "timeout".equals(id) ? 101 : 99;
        List<String> proposed = "q1".equals(id) ? CANDIDATE : "invented".equals(id) ? List.of("z9", "s1") : BASELINE;
        var e = CAP122ReferenceOracle.evaluate(allowed, proposed, elapsed, 100);
        return new RankingResult(e.status().name(), e.ids(), e.elapsedMillis(), e.timeoutRecorded(), e.rerankCalls(),
                CAP122ReferenceOracle.recallAt2(allowed, e.ids()), CAP122ReferenceOracle.reciprocalRank(allowed, e.ids()));
    }
}
