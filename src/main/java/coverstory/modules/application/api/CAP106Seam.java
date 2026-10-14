package coverstory.modules.application.api;

import java.util.Objects;

/** CAP-106 bounded local policy seam. No HTTP server, cloud storage, or TLS adapter is supplied. */
public final class CAP106Seam {
    public enum Operation { GET, PUT, DELETE }
    public enum Transport { TLS, CLEAR_TEXT }
    public enum RevisionKind { HISTORICAL, CURRENT }
    public enum Decision { ALLOW, DENY }

    public record Principal(String id, String role) { }
    public record Owner(String id) { }
    public record Key(String value, String ownerId, String applicationId, String revisionId,
                      RevisionKind kind) { }
    public record AccessDecision(Decision decision, String safeCode) { }
    public record Snapshot(String revisionId, RevisionKind kind, String details) { }
    public interface LocalSnapshotStore {
        Snapshot fetch(Key key);
    }

    private final LocalSnapshotStore store;

    public CAP106Seam(LocalSnapshotStore store) { this.store = Objects.requireNonNull(store); }

    /** Learner implements exact allow/deny policy. Must deny before invoking store.fetch. */
    public AccessDecision authorizeSnapshotRead(Principal principal, Operation operation,
                                                Owner owner, Key key, Transport transport) {
        // LEARNER TODO: allow only TLS GET by an authorized reader for a safe, registry-known key
        // whose owner id matches principal and owner. Never mutate. Return generic denial without
        // foreign metadata. Keep historical/current identity attached to all returned records.
        throw new UnsupportedOperationException("CAP-106 learner TODO: implement owner-scoped snapshot read policy");
    }

    /** Reads only after the policy returns ALLOW; a denied response contains no snapshot metadata. */
    public Snapshot read(Principal principal, Operation operation, Owner owner, Key key, Transport transport) {
        AccessDecision decision = authorizeSnapshotRead(principal, operation, owner, key, transport);
        if (decision == null || decision.decision() != Decision.ALLOW) return null;
        return store.fetch(key);
    }
}
