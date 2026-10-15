package capstone.coverstory.support;

import capstone.coverstory.support.internal.EvidenceRerankEvaluator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Learner acceptance contract; failures at the explicit TODO are expected before implementation. */
@Tag("learnerAcceptance")
class EvidenceRerankEvaluatorTest {
    @Test void validPrecomputedRankingImprovesOnlyTheTwoFixedExamples() {
        var a = List.of(new EvidenceRerankEvaluator.Candidate("s1", true), new EvidenceRerankEvaluator.Candidate("a1", false), new EvidenceRerankEvaluator.Candidate("b1", false), new EvidenceRerankEvaluator.Candidate("c1", false));
        var b = List.of(new EvidenceRerankEvaluator.Candidate("s2", true), new EvidenceRerankEvaluator.Candidate("a2", false), new EvidenceRerankEvaluator.Candidate("b2", false), new EvidenceRerankEvaluator.Candidate("c2", false));
        assertEquals(0.0, CAP122ReferenceOracle.recallAt2(a, List.of("a1", "b1", "s1", "c1")));
        assertEquals(0.0, CAP122ReferenceOracle.recallAt2(b, List.of("a2", "b2", "s2", "c2")));
        var evaluator = new EvidenceRerankEvaluator();
        var first = evaluator.evaluate(a, List.of("s1", "a1", "b1", "c1"), 99, 100);
        var second = evaluator.evaluate(b, List.of("a2", "s2", "b2", "c2"), 99, 100);
        assertEquals(EvidenceRerankEvaluator.Status.ACCEPTED, first.status());
        assertEquals(1.0, CAP122ReferenceOracle.recallAt2(a, first.ids()));
        assertEquals(1.0, CAP122ReferenceOracle.recallAt2(b, second.ids()));
        assertEquals(0.75, (CAP122ReferenceOracle.reciprocalRank(a, first.ids()) + CAP122ReferenceOracle.reciprocalRank(b, second.ids())) / 2.0);
    }

    @Test void deadlineFallbackUsesOriginalOrderOnceIncludingBoundary() {
        var candidates = List.of(new EvidenceRerankEvaluator.Candidate("r", true), new EvidenceRerankEvaluator.Candidate("n", false));
        var evaluator = new EvidenceRerankEvaluator();
        assertEquals(EvidenceRerankEvaluator.Status.ACCEPTED, evaluator.evaluate(candidates, List.of("n", "r"), 100, 100).status());
        var late = evaluator.evaluate(candidates, List.of("n", "r"), 101, 100);
        assertEquals(EvidenceRerankEvaluator.Status.TIMEOUT_FALLBACK, late.status());
        assertEquals(List.of("r", "n"), late.ids());
        assertTrue(late.timeoutRecorded());
        assertEquals(1, late.rerankCalls(), "fallback must not recursively retry");
    }

    @Test void inventedCandidateIsRejectedWithoutAnyFetchOrPolicyFact() {
        var candidates = List.of(new EvidenceRerankEvaluator.Candidate("s1", true), new EvidenceRerankEvaluator.Candidate("s2", false));
        var result = new EvidenceRerankEvaluator().evaluate(candidates, List.of("z9", "s1"), 20, 100);
        assertEquals(EvidenceRerankEvaluator.Status.INVALID_RERANK, result.status());
        assertTrue(result.ids().isEmpty());
    }
}
