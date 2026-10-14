package coverstory.security;

import java.util.Set;

/**
 * CAP-88 learner seam: decide a synthetic access-matrix row only.
 * This is not a Spring Security filter, authenticated principal, or production authorization adapter.
 */
public final class CAP88AccessPolicySeam {
    public enum Role { APPLICANT, CATALOG_EDITOR, SUPPORT, SIMULATOR, READ_ONLY_ASSISTANT, UNKNOWN }
    public enum Action {
        READ_OWN_DRAFT, WRITE_OWN_DRAFT, READ_DISCLOSURES, EDIT_CATALOG,
        READ_SUPPORT_CASE, SUBMIT_SIMULATED_EVENT, READ_PERMITTED_EVIDENCE,
        PAYMENT_COMMAND, SUBMIT_APPLICATION, CHARGE, REFUND, UNDERWRITE, UNKNOWN
    }
    public enum ResourceKind { DRAFT, DISCLOSURES, CATALOG, SUPPORT_CASE, SIMULATED_EVENT, EVIDENCE, UNKNOWN }

    public record Actor(String id, Role role) { }
    public record Resource(ResourceKind kind, String ownerId, String assignedSupportId) { }
    public record AccessRequest(Actor actor, Action action, Resource resource) { }
    public record Decision(boolean allowed, Set<String> visibleFields, String reason) {
        public Decision { visibleFields = Set.copyOf(visibleFields); }
    }

    /** Learner implements the saved access matrix here. The seam has no persistence or side effects. */
    public Decision decide(AccessRequest request) {
        // LEARNER TODO: default deny, bind applicant access to the actor's own resource,
        // scope support to assigned cases and minimum fields, and limit role capabilities.
        throw new UnsupportedOperationException("CAP-88 learner TODO: implement access matrix");
    }
}
