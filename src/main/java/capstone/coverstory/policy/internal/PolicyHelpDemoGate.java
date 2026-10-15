package capstone.coverstory.policy.internal;

import java.util.List;

/** Learner seam for a read-only, synthetic policy/help demonstration. */
public final class PolicyHelpDemoGate {
    private PolicyHelpDemoGate() { }

    public enum PrincipalKind { OWNER, SUPPORT, OTHER }
    public enum State { ISSUANCE_PENDING, ACTIVE_COVER, ABSTAIN, DENY }
    public record Principal(String id, PrincipalKind kind, String ownerId, String assignedCaseId) { }
    public record Policy(String id, String ownerId, String applicationId, String issuanceReference,
                         String effectiveDate, String termsVersion) { }
    public record Event(String id, String policyId, String occurredAt, String label) { }
    public record Citation(String sourceId, String version, String excerpt) { }
    public record Evidence(String ownerId, String question, boolean supportedQuestion,
                           boolean offerAccepted, boolean paymentConfirmed,
                           boolean effectiveCoverEvidence, List<Policy> policies,
                           List<Event> events, Citation citation) { }
    public record Answer(State state, String wording, List<Policy> policies,
                         List<Event> timeline, List<Citation> citations) { }

    public static Answer answer(Principal principal, Evidence evidence) {
        // TODO(CAP-123): authorize before reading; require verified simulated issuance
        // to disclose exactly one owned policy and chronology with exact source citation.
        // Otherwise return pending/abstain/deny without writes or cover claims.
        throw new UnsupportedOperationException("TODO(CAP-123): implement policy/help evidence gate");
    }
}
