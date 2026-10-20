package assignments.insurance;

import java.security.Principal;

/** Learner-owned owner-scoping rule; caller-supplied owner fields are never identity. */
public final class JourneyAccessPolicy {
    public void assertCanAccess(Principal authenticatedPrincipal, String persistedOwnerId,
            JourneyReadRequest request) {
        // LEARNER TODO: require an authenticated principal and compare its identity to
        // persisted ownership. Ignore request.ownerId; conceal another owner's journey.
        throw new UnsupportedOperationException("Day 12 learner TODO: enforce owner-scoped access");
    }
}
