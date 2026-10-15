package capstone.coverstory.support.internal;

/**
 * Learner-owned per-invocation authorization seam for cancellation-state reads (CAP-125).
 * This class can authorize one read. It does not expose cancellation, refund, or mutation
 * capabilities.
 */
public final class LifecycleToolAuthorization {
    private LifecycleToolAuthorization() { }

    public enum Role { OWNER, SUPPORT, MODEL, OTHER }
    public enum Decision { ALLOW, DENY }
    public record Principal(String id, Role role, String ownerId, String supportCaseId,
                            boolean conversationClaimsAdmin) { }
    public record Request(String applicationId, String toolInvocationId) { }
    public record Application(String id, String ownerId, String caseId,
                              String cancellationState) { }
    public record ReadResult(String status, String applicationId, String cancellationState,
                             String safeMessage) { }

    /** Read-only facts from owning modules; deliberately has no write or command method. */
    public interface FactsPort {
        Application application(String applicationId);
        /** Resolve current assignment for every call; never trust conversation history. */
        boolean isCurrentlyAssigned(String supportActorId, String caseId);
    }

    /** Learner implements only this decision. Every tool invocation must call it anew. */
    public static Decision authorize(Principal principal, Request request, FactsPort facts) {
        // TODO(CAP-125): resolve current owner/assignment authority for this invocation;
        // ignore model/conversation claims, fail closed, and do not expose foreign identifiers.
        throw new UnsupportedOperationException("TODO(CAP-125): implement per-invocation read authorization");
    }

    /** Mechanical read-only wiring supplied by the instructor. No mutation capability exists. */
    public static ReadResult invokeReadOnlyTool(Principal principal, Request request, FactsPort facts) {
        Decision decision = authorize(principal, request, facts);
        if (decision != Decision.ALLOW) {
            return new ReadResult("NOT_FOUND", null, null, "Application not found.");
        }
        Application application = facts.application(request.applicationId());
        // A disappearing row or changed owner after decision must fail closed.
        if (application == null || !principalMayStillRead(principal, application, facts)) {
            return new ReadResult("NOT_FOUND", null, null, "Application not found.");
        }
        return new ReadResult("FOUND", application.id(), application.cancellationState(), null);
    }

    private static boolean principalMayStillRead(Principal p, Application a, FactsPort facts) {
        return switch (p.role()) {
            case OWNER -> a.ownerId().equals(p.ownerId());
            case SUPPORT -> a.caseId().equals(p.supportCaseId())
                    && facts.isCurrentlyAssigned(p.id(), a.caseId());
            default -> false;
        };
    }
}
