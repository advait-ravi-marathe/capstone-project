package coverstory.modules.payments.api;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** CAP-74 read-only status boundary for synthetic payment attempts. */
public final class CAP74Seam {
    public enum NextAction { NONE, RECONCILE }

    public record Principal(UUID userId) { }

    /** Raw state is retained so impossible persisted values fail closed. */
    public record AttemptSnapshot(UUID attemptId, UUID ownerId, String state, Instant updatedAt,
                                  String requestId, String operationReference) { }

    @FunctionalInterface
    public interface OwnedAttemptReader {
        Optional<AttemptSnapshot> findOwned(UUID authenticatedOwnerId, UUID attemptId);
    }

    /** This is the complete successful response shape; provider references are intentionally absent. */
    public record SafeStatus(UUID attemptId, String state, Instant updatedAt,
                             String requestId, NextAction nextAction) { }

    public record Reply(int status, String errorCode, SafeStatus body) { }

    private final OwnedAttemptReader attempts;

    public CAP74Seam(OwnedAttemptReader attempts) { this.attempts = attempts; }

    public Reply getStatus(Principal principal, UUID attemptId) {
        // LEARNER TODO: authenticate, perform one owner-scoped lookup, map only
        // payment state to the safe response, and fail closed for unknown state.
        throw new UnsupportedOperationException("CAP-74 learner TODO: implement owned attempt status");
    }
}
