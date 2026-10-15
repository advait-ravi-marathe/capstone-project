package capstone.coverstory.support;

import java.util.List;
import java.util.Set;

/** Independent fixed-fixture oracle. It does not call the learner seam. */
final class CAP119ReferenceOracle {
    record Principal(String id, Role role, String ownerId) { }
    enum Role { OWNER, SUPPORT }
    record Request(String caseId, String requestedOwnerId) { }
    record Source(String id, String ownerId, String caseId, String kind, String text) { }
    record Result(String status, String code, List<Source> responseSources, List<Source> modelSources,
                  int lookupCount) { }
    interface AssignmentRead { String assignedOwner(String principalId, String caseId); }
    interface CandidateRead { List<Source> find(String ownerId, String caseId); }

    Result retrieve(Principal principal, Request request, AssignmentRead assignments, CandidateRead candidates) {
        String ownerId;
        if (principal.role() == Role.OWNER) {
            if (principal.ownerId() == null || (request.requestedOwnerId() != null
                    && !principal.ownerId().equals(request.requestedOwnerId()))) {
                return deny("OWNER_SCOPE_MISMATCH", 0);
            }
            ownerId = principal.ownerId();
        } else if (principal.role() == Role.SUPPORT) {
            ownerId = assignments.assignedOwner(principal.id(), request.caseId());
            if (ownerId == null) {
                return deny("SUPPORT_NOT_ASSIGNED", 0);
            }
        } else {
            return deny("UNKNOWN_PRINCIPAL", 0);
        }
        List<Source> rows = candidates.find(ownerId, request.caseId());
        if (rows.stream().anyMatch(row -> !ownerId.equals(row.ownerId())
                || !request.caseId().equals(row.caseId()))) {
            return deny("RESULT_SCOPE_VIOLATION", 1);
        }
        List<Source> safe = List.copyOf(rows);
        return new Result("ALLOW", null, safe, safe, 1);
    }

    private static Result deny(String code, int lookupCount) {
        return new Result("DENY", code, List.of(), List.of(), lookupCount);
    }
}
