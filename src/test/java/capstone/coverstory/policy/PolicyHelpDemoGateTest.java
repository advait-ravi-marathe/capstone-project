package capstone.coverstory.policy;

import capstone.coverstory.policy.internal.PolicyHelpDemoGate;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Learner acceptance; expected to stop at TODO(CAP-123) until learner implementation. */
@Tag("learnerAcceptance")
class PolicyHelpDemoGateTest {
    private static final PolicyHelpDemoGate.Principal OWNER = new PolicyHelpDemoGate.Principal("owner-a", PolicyHelpDemoGate.PrincipalKind.OWNER, "owner-a", null);
    private static PolicyHelpDemoGate.Evidence issued() {
        var p = new PolicyHelpDemoGate.Policy("policy-sim-123", "owner-a", "app-sim-123", "issue-sim-123", "2027-01-11", "policy-terms-sim-v3");
        return new PolicyHelpDemoGate.Evidence("owner-a", "When does my cover start?", true, true, true, true, java.util.List.of(p),
                java.util.List.of(new PolicyHelpDemoGate.Event("e2", p.id(), "2027-01-11T10:02:00Z", "COVER_EFFECTIVE"), new PolicyHelpDemoGate.Event("e1", p.id(), "2027-01-11T10:01:00Z", "ISSUANCE_CONFIRMED")),
                new PolicyHelpDemoGate.Citation("policy-terms", "policy-terms-sim-v3", "Effective date shown on simulated schedule."));
    }
    @Test void issuedPolicyIsOwnedChronologicalAndExactlyCited() {
        var result = PolicyHelpDemoGate.answer(OWNER, issued());
        assertEquals(PolicyHelpDemoGate.State.ACTIVE_COVER, result.state()); assertEquals(1, result.policies().size());
        assertEquals("owner-a", result.policies().get(0).ownerId());
        assertEquals(java.util.List.of("ISSUANCE_CONFIRMED", "COVER_EFFECTIVE"), result.timeline().stream().map(PolicyHelpDemoGate.Event::label).toList());
        assertEquals("policy-terms-sim-v3", result.citations().get(0).version());
    }
    @Test void paymentAndAcceptedOfferWithoutIssuanceRemainPending() {
        var e = new PolicyHelpDemoGate.Evidence("owner-a", "When does my cover start?", true, true, true, false, java.util.List.of(), java.util.List.of(), null);
        var result = PolicyHelpDemoGate.answer(OWNER, e);
        assertEquals(PolicyHelpDemoGate.State.ISSUANCE_PENDING, result.state()); assertTrue(result.wording().contains("does not mean cover is active"));
    }
    @Test void foreignOwnerAndUnsupportedQuestionReturnNoPolicyOrCitation() {
        var foreign = new PolicyHelpDemoGate.Principal("owner-b", PolicyHelpDemoGate.PrincipalKind.OWNER, "owner-b", null);
        var denied = PolicyHelpDemoGate.answer(foreign, issued()); assertEquals(PolicyHelpDemoGate.State.DENY, denied.state());
        var e = new PolicyHelpDemoGate.Evidence("owner-a", "Will this investment double?", false, true, true, true, java.util.List.of(), java.util.List.of(), null);
        var abstained = PolicyHelpDemoGate.answer(OWNER, e); assertEquals(PolicyHelpDemoGate.State.ABSTAIN, abstained.state());
        assertTrue(denied.policies().isEmpty()); assertTrue(denied.citations().isEmpty()); assertTrue(abstained.policies().isEmpty());
    }
}
