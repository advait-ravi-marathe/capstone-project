package capstone.coverstory.application;

import java.util.List;
import java.util.UUID;

/** Day 56 learner seam for a deliberately small, exact allow/deny policy language. */
public final class ValidateExactApplicationAccessDecisions {
    public enum Action { READ_JOURNEY, READ_PROTECTED_REVISION }
    public record Decision(String effect, UUID principalId, Action action, UUID resourceId) { }
    public record Request(UUID principalId, Action action, UUID resourceId, UUID ownerId,
                          UUID assignedSupportPrincipal, boolean anonymous, String browserClaim) { }

    public boolean authorize(Request request, List<Decision> decisions) {
        // LEARNER TODO: deny anonymous, cross-owner and unassigned-support requests;
        // require exact action/resource allows and let an exact deny win. Reject wildcard
        // and unsupported conditions. Never trust browser paid/submitted/issued claims.
        throw new UnsupportedOperationException("CAP-172 learner TODO: validate exact access decisions");
    }
}
