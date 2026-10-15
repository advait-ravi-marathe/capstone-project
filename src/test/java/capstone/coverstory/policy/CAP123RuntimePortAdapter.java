package capstone.coverstory.policy;

import capstone.coverstory.policy.internal.PolicyHelpDemoGate;
import coverstory.modules.policy.api.PolicyHelpReadPort;
import java.util.List;
import java.util.UUID;

/** Isolated instructor runtime adapter over the frozen issuance/payment fixture. */
public final class CAP123RuntimePortAdapter implements PolicyHelpReadPort {
    public static final UUID OWNER_A = UUID.fromString("12300000-0000-4000-8000-000000000001");
    public static final UUID OWNER_B = UUID.fromString("12300000-0000-4000-8000-000000000002");

    @Override public HelpResult read(UUID actor, String questionId) {
        boolean owner = OWNER_A.equals(actor);
        boolean issued = "issued".equals(questionId);
        var principal = owner ? new PolicyHelpDemoGate.Principal("owner-a", PolicyHelpDemoGate.PrincipalKind.OWNER, "owner-a", null)
                : new PolicyHelpDemoGate.Principal("owner-b", PolicyHelpDemoGate.PrincipalKind.OWNER, "owner-b", null);
        var policy = new PolicyHelpDemoGate.Policy("policy-sim-123", "owner-a", "app-sim-123",
                "issue-sim-123", "2027-01-11", "policy-terms-sim-v3");
        var events = List.of(new PolicyHelpDemoGate.Event("event-2", policy.id(), "2027-01-11T10:02:00Z", "COVER_EFFECTIVE"),
                new PolicyHelpDemoGate.Event("event-1", policy.id(), "2027-01-11T10:01:00Z", "ISSUANCE_CONFIRMED"));
        var evidence = new PolicyHelpDemoGate.Evidence("owner-a", "When does cover start?", !"unsupported".equals(questionId),
                true, true, issued, issued ? List.of(policy) : List.of(), issued ? events : List.of(),
                issued ? new PolicyHelpDemoGate.Citation("policy-terms", "policy-terms-sim-v3", "Effective date shown on simulated schedule.") : null);
        var answer = CAP123ReferenceOracle.answer(principal, evidence);
        return new HelpResult(answer.state().name(), answer.wording(), answer.policies().stream()
                .map(p -> new PolicyFact(p.id(), p.applicationId(), p.issuanceReference(), p.effectiveDate(), p.termsVersion())).toList(),
                answer.timeline().stream().map(e -> new TimelineEvent(e.id(), e.occurredAt(), e.label())).toList(),
                answer.citations().stream().map(c -> new Citation(c.sourceId(), c.version(), c.excerpt())).toList(), 0);
    }
}
