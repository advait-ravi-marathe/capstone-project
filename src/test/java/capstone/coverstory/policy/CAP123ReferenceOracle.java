package capstone.coverstory.policy;

import capstone.coverstory.policy.internal.PolicyHelpDemoGate;
import java.util.*;

/** Fixed instructor model of the declared synthetic boundary; never calls production adapters. */
final class CAP123ReferenceOracle {
    private CAP123ReferenceOracle() { }
    static PolicyHelpDemoGate.Answer answer(PolicyHelpDemoGate.Principal principal,
                                            PolicyHelpDemoGate.Evidence e) {
        if (principal == null || e == null) return response(PolicyHelpDemoGate.State.DENY,
                "I cannot verify access to this policy.", List.of(), List.of(), List.of());
        boolean owner = principal.kind() == PolicyHelpDemoGate.PrincipalKind.OWNER
                && Objects.equals(principal.ownerId(), e.ownerId());
        // The current fixture contract has no owning-module assignment adapter or case ID
        // on evidence, so Support must deny rather than infer an assignment.
        if (!owner) return response(PolicyHelpDemoGate.State.DENY,
                "I cannot verify access to this policy.", List.of(), List.of(), List.of());
        if (!e.supportedQuestion()) return response(PolicyHelpDemoGate.State.ABSTAIN,
                "I cannot answer that from the available policy evidence.", List.of(), List.of(), List.of());
        boolean issued = e.effectiveCoverEvidence() && e.policies().size() == 1
                && e.policies().get(0).issuanceReference() != null
                && !e.policies().get(0).issuanceReference().isBlank();
        if (!issued) return response(PolicyHelpDemoGate.State.ISSUANCE_PENDING,
                "Issuance is pending; payment or offer acceptance alone does not mean cover is active.",
                List.of(), List.of(), List.of());
        var policy = e.policies().get(0);
        if (!Objects.equals(policy.ownerId(), e.ownerId())) return response(PolicyHelpDemoGate.State.DENY,
                "I cannot verify access to this policy.", List.of(), List.of(), List.of());
        var events = e.events().stream().filter(x -> Objects.equals(x.policyId(), policy.id()))
                .sorted(Comparator.comparing(PolicyHelpDemoGate.Event::occurredAt)).toList();
        return response(PolicyHelpDemoGate.State.ACTIVE_COVER,
                "Simulated policy evidence shows active cover from " + policy.effectiveDate() + ".",
                List.of(policy), events, e.citation() == null ? List.of() : List.of(e.citation()));
    }
    private static PolicyHelpDemoGate.Answer response(PolicyHelpDemoGate.State state, String wording,
            List<PolicyHelpDemoGate.Policy> policies, List<PolicyHelpDemoGate.Event> events,
            List<PolicyHelpDemoGate.Citation> citations) {
        return new PolicyHelpDemoGate.Answer(state, wording, policies, events, citations);
    }
}
