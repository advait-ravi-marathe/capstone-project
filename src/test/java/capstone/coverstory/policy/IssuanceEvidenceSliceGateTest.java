package capstone.coverstory.policy;

import capstone.coverstory.policy.internal.IssuanceEvidenceSliceGate;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class IssuanceEvidenceSliceGateTest {
    private static List<IssuanceEvidenceSliceGate.ScoredCase> cases(String prefix, int n, int wrong, IssuanceEvidenceSliceGate.Label gold) {
        var list=new ArrayList<IssuanceEvidenceSliceGate.ScoredCase>();
        for(int i=1;i<=n;i++) { var predicted=(i>n-wrong) ? (gold==IssuanceEvidenceSliceGate.Label.ANSWER ? IssuanceEvidenceSliceGate.Label.ABSTAIN : IssuanceEvidenceSliceGate.Label.ANSWER) : gold; list.add(new IssuanceEvidenceSliceGate.ScoredCase(prefix+i,gold,predicted)); }
        return list;
    }
    @Test void equalOverallAccuracyWithMustAbstainRegressionMustHold() {
        var baseline=IssuanceEvidenceSliceGate.evaluate(cases("ba",10,2,IssuanceEvidenceSliceGate.Label.ANSWER),cases("bm",10,0,IssuanceEvidenceSliceGate.Label.ABSTAIN),null);
        var candidate=IssuanceEvidenceSliceGate.evaluate(cases("ca",10,0,IssuanceEvidenceSliceGate.Label.ANSWER),cases("cm",10,2,IssuanceEvidenceSliceGate.Label.ABSTAIN),baseline);
        assertEquals(.9,baseline.overallRate()); assertEquals(.9,candidate.overallRate());
        assertEquals(.8,candidate.mustAbstain().rate()); assertEquals(IssuanceEvidenceSliceGate.Promotion.HOLD,candidate.promotion());
        assertEquals(List.of("cm9","cm10"),candidate.mustAbstain().errorIds());
    }
    @Test void paymentAndOfferAcceptanceDoNotCreateActiveCover() {
        var projection=IssuanceEvidenceSliceGate.project(new IssuanceEvidenceSliceGate.PolicyRecord(null,false,true,true));
        assertEquals(IssuanceEvidenceSliceGate.Status.NO_COVER_YET,projection.status()); assertFalse(projection.maySayActiveCover());
    }
    @Test void bothSimulatedIssuanceReferenceAndEffectiveEvidenceAreRequired() {
        assertFalse(IssuanceEvidenceSliceGate.project(new IssuanceEvidenceSliceGate.PolicyRecord("issue-sim-1",false,true,true)).maySayActiveCover());
        assertFalse(IssuanceEvidenceSliceGate.project(new IssuanceEvidenceSliceGate.PolicyRecord(" ",true,true,true)).maySayActiveCover());
        assertTrue(IssuanceEvidenceSliceGate.project(new IssuanceEvidenceSliceGate.PolicyRecord("issue-sim-1",true,false,false)).maySayActiveCover());
    }
    @Test void emptySliceHasUndefinedRateAndMissingGoldIsInvalid() {
        var evaluation=IssuanceEvidenceSliceGate.evaluate(List.of(),cases("m",1,0,IssuanceEvidenceSliceGate.Label.ABSTAIN),null);
        assertNull(evaluation.answerable().rate()); assertEquals(0,evaluation.answerable().n());
        assertEquals(IssuanceEvidenceSliceGate.Promotion.HOLD,evaluation.promotion());
        var missing=IssuanceEvidenceSliceGate.evaluate(List.of(new IssuanceEvidenceSliceGate.ScoredCase("x",null,IssuanceEvidenceSliceGate.Label.ANSWER)),List.of(),null);
        assertEquals(IssuanceEvidenceSliceGate.Promotion.INVALID_DATASET,missing.promotion());
    }
    @Test void todoGuardsKeepLearnerLogicUnimplemented() {
        assertThrows(UnsupportedOperationException.class,()->IssuanceEvidenceSliceGate.project(new IssuanceEvidenceSliceGate.PolicyRecord(null,false,true,true)));
    }
}
