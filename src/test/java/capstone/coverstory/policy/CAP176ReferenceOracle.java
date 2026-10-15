package capstone.coverstory.policy;

import capstone.coverstory.policy.internal.IssuanceEvidenceSliceGate;
import java.util.*;

/** Independent fixed-fixture oracle; deliberately separate from the learner TODO. */
final class CAP176ReferenceOracle {
    private CAP176ReferenceOracle() { }
    record Slice(int n, int correct, Double rate, List<String> errorIds) { }
    record Evaluation(Slice answerable, Slice mustAbstain, int total, int correct,
                      Double overallRate, IssuanceEvidenceSliceGate.Promotion promotion) { }
    static Evaluation evaluate(List<IssuanceEvidenceSliceGate.ScoredCase> answerable,
                               List<IssuanceEvidenceSliceGate.ScoredCase> mustAbstain,
                               Evaluation baseline) {
        if (answerable == null || mustAbstain == null || hasMissing(answerable) || hasMissing(mustAbstain))
            return new Evaluation(invalid(), invalid(), 0, 0, null, IssuanceEvidenceSliceGate.Promotion.INVALID_DATASET);
        Slice a = score(answerable), m = score(mustAbstain);
        if (baseline != null && (baseline.mustAbstain().rate() == null || m.rate() == null))
            return new Evaluation(a, m, a.n() + m.n(), a.correct() + m.correct(), rate(a.n()+m.n(),a.correct()+m.correct()), IssuanceEvidenceSliceGate.Promotion.INVALID_DATASET);
        int n=a.n()+m.n(), correct=a.correct()+m.correct();
        var promotion = n == 0 || a.rate() == null || m.rate() == null
                ? IssuanceEvidenceSliceGate.Promotion.HOLD
                : baseline == null || m.rate() >= baseline.mustAbstain().rate()
                    ? IssuanceEvidenceSliceGate.Promotion.PROMOTE : IssuanceEvidenceSliceGate.Promotion.HOLD;
        return new Evaluation(a,m,n,correct,rate(n,correct),promotion);
    }
    static IssuanceEvidenceSliceGate.Projection project(IssuanceEvidenceSliceGate.PolicyRecord r) {
        boolean reference = r.insurerIssuanceReference()!=null && !r.insurerIssuanceReference().isBlank();
        boolean issued = reference && r.effectiveCoverEvidence();
        return new IssuanceEvidenceSliceGate.Projection(issued ? IssuanceEvidenceSliceGate.Status.ACTIVE_COVER : IssuanceEvidenceSliceGate.Status.NO_COVER_YET,
                issued, issued ? "SIMULATED_ISSUANCE_CONFIRMED" : "ISSUANCE_EVIDENCE_INCOMPLETE");
    }
    private static boolean hasMissing(List<IssuanceEvidenceSliceGate.ScoredCase> cs) {
        return cs.stream().anyMatch(c -> c == null || c.id()==null || c.gold()==null || c.predicted()==null);
    }
    private static Slice score(List<IssuanceEvidenceSliceGate.ScoredCase> cs) {
        int n=cs.size(), correct=0; var errors=new ArrayList<String>();
        for(var c:cs) if(c.gold()==c.predicted()) correct++; else errors.add(c.id());
        return new Slice(n,correct,rate(n,correct),List.copyOf(errors));
    }
    private static Slice invalid() { return new Slice(0,0,null,List.of("INVALID_DATASET")); }
    private static Double rate(int n,int correct) { return n==0 ? null : (double)correct/n; }
}
