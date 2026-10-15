package capstone.coverstory.policy;

import capstone.coverstory.policy.internal.IssuanceEvidenceSliceGate;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CAP176InstructorInfrastructureTest {
    private JsonNode fixture() throws Exception {
        return new ObjectMapper().readTree(Files.readString(Path.of("docs/coverstory/fixtures/cap-176.json")));
    }
    @Test void fixedSliceFixtureShowsEqualAggregateAndUnsafeAbstainRegression() throws Exception {
        byte[] bytes=Files.readAllBytes(Path.of("docs/coverstory/fixtures/cap-176.json"));
        assertEquals("0b432838595bd2078087cde878eaee67c43b5d29527d4da09ef8f0042800b566",HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
        JsonNode f=fixture(); assertFalse(f.path("network_enabled").asBoolean());
        JsonNode candidates=f.path("candidates");
        var baseline=CAP176ReferenceOracle.evaluate(cases(candidates.path("baseline").path("answerable")),
                cases(candidates.path("baseline").path("must_abstain")),null);
        var candidate=CAP176ReferenceOracle.evaluate(cases(candidates.path("candidate").path("answerable")),
                cases(candidates.path("candidate").path("must_abstain")),baseline);
        assertEquals(18,baseline.correct()); assertEquals(.9,baseline.overallRate());
        assertEquals(18,candidate.correct()); assertEquals(.9,candidate.overallRate());
        assertEquals(.8,candidate.mustAbstain().rate()); assertEquals(List.of("cm9","cm10"),candidate.mustAbstain().errorIds());
        assertEquals(IssuanceEvidenceSliceGate.Promotion.HOLD,candidate.promotion());
    }
    @Test void onlyBothSimulatedIssuanceFactsProjectActiveCover() throws Exception {
        for(JsonNode n:fixture().path("policy_records")) {
            String ref=n.path("issuance_reference").isNull() ? null : n.path("issuance_reference").asText();
            var record=new IssuanceEvidenceSliceGate.PolicyRecord(ref,n.path("effective_cover_evidence").asBoolean(),
                    n.path("payment_confirmed").asBoolean(),n.path("offer_accepted").asBoolean());
            var result=CAP176ReferenceOracle.project(record);
            assertEquals(n.path("expected").asText(),result.status().name(),n.path("id").asText());
            assertEquals("ACTIVE_COVER".equals(n.path("expected").asText()),result.maySayActiveCover());
        }
    }
    @Test void zeroDenominatorAndMissingGoldAreNotPerfectScores() {
        var empty=CAP176ReferenceOracle.evaluate(List.of(),List.of(),null);
        assertNull(empty.answerable().rate()); assertNull(empty.mustAbstain().rate()); assertNull(empty.overallRate());
        assertEquals(IssuanceEvidenceSliceGate.Promotion.HOLD,empty.promotion());
        var missing=CAP176ReferenceOracle.evaluate(List.of(new IssuanceEvidenceSliceGate.ScoredCase("x",null,IssuanceEvidenceSliceGate.Label.ANSWER)),List.of(),null);
        assertEquals(IssuanceEvidenceSliceGate.Promotion.INVALID_DATASET,missing.promotion());
        assertNull(missing.overallRate());
    }
    private static List<IssuanceEvidenceSliceGate.ScoredCase> cases(JsonNode node) {
        var out=new ArrayList<IssuanceEvidenceSliceGate.ScoredCase>();
        for(JsonNode c:node) out.add(new IssuanceEvidenceSliceGate.ScoredCase(c.path("id").asText(),label(c.path("gold").asText()),label(c.path("predicted").asText())));
        return out;
    }
    private static IssuanceEvidenceSliceGate.Label label(String value) { return IssuanceEvidenceSliceGate.Label.valueOf(value); }
}
