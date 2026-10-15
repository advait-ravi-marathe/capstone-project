package capstone.coverstory.support;

import capstone.coverstory.support.internal.LifecycleToolAuthorization;

/** Independent expected-outcome calculator for the frozen CAP-125 synthetic cases. */
final class CAP125ReferenceOracle {
    private CAP125ReferenceOracle() { }

    static LifecycleToolAuthorization.ReadResult invoke(
            LifecycleToolAuthorization.Principal p,
            LifecycleToolAuthorization.Request r,
            LifecycleToolAuthorization.FactsPort facts) {
        var a = facts.application(r.applicationId());
        boolean allowed = a != null && switch (p.role()) {
            case OWNER -> a.ownerId().equals(p.ownerId());
            case SUPPORT -> a.caseId().equals(p.supportCaseId())
                    && facts.isCurrentlyAssigned(p.id(), a.caseId());
            case MODEL, OTHER -> false;
        };
        return allowed
                ? new LifecycleToolAuthorization.ReadResult("FOUND", a.id(), a.cancellationState(), null)
                : new LifecycleToolAuthorization.ReadResult("NOT_FOUND", null, null, "Application not found.");
    }
}
