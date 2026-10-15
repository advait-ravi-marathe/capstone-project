package capstone.coverstory.support.internal;

import java.util.List;
import java.util.Set;

/**
 * Learner-owned boundary seam for retrieving synthetic policy/help evidence.
 * The authenticated principal, never a request owner field, establishes scope.
 */
public final class OwnedEvidenceRetriever {
    private OwnedEvidenceRetriever() { }

    public enum Role { OWNER, SUPPORT }
    public record Principal(String id, Role role, String ownerId) { }
    public record Request(String caseId, String requestedOwnerId) { }
    public record Source(String id, String ownerId, String caseId, String kind, String text) { }
    public record Result(String status, String code, List<Source> sources) { }

    public interface AssignmentPort {
        /** Returns the case owner only when this principal is assigned, otherwise null. */
        String assignedOwnerId(String principalId, String caseId);
    }
    public interface EvidenceReadPort {
        List<Source> findCandidates(String ownerId, String caseId);
    }

    public static Result retrieve(Principal principal, Request request, AssignmentPort assignments,
                                  EvidenceReadPort evidenceReadPort) {
        // TODO(CAP-119): establish owner/assigned-support authority before lookup, then
        // recheck every returned row before exposing any text to a model or response.
        throw new UnsupportedOperationException("TODO(CAP-119): implement scoped evidence retrieval");
    }
}
