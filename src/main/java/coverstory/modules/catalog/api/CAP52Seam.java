package coverstory.modules.catalog.api;

import org.springframework.stereotype.Component;

/** Learner-owned import decision port. The principal must come from trusted server context. */
@Component
public final class CAP52Seam {
    public enum Role { EDITOR, APPLICANT }
    public enum Decision { ACCEPTED, DUPLICATE, CONFLICT, INVALID }
    public record Principal(String subjectId, Role role) { }
    public record ImportRequest(String sourceId, String sourceVersion, String canonicalHash) { }

    /** TODO(CAP-52): implement stable-key/hash idempotency and conflict semantics. */
    public Decision accept(Principal trustedEditor, ImportRequest request) {
        throw new UnsupportedOperationException("TODO(CAP-52): implement import decision adapter");
    }
}
