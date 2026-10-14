package capstone.coverstory.shortlist.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** CAP-68 learner seam for observing a lost response after shortlist commit. */
public final class CAP68Seam {
    public record Command(UUID ownerId, UUID journeyId, UUID operationId,
                          byte[] idempotencyKeyHash, byte[] payloadHash, long expectedVersion) {
        public Command {
            idempotencyKeyHash = idempotencyKeyHash.clone();
            payloadHash = payloadHash.clone();
        }
        @Override public byte[] idempotencyKeyHash() { return idempotencyKeyHash.clone(); }
        @Override public byte[] payloadHash() { return payloadHash.clone(); }
    }
    public record StoredResult(int status, long version, UUID operationId) { }
    public record GapReport(boolean clientStateUnknown, boolean durableCommitObserved,
                            boolean notificationGap, long committedVersion,
                            String recoverySource, List<String> gaps) { }

    @FunctionalInterface public interface ShortlistCommandPort {
        StoredResult commit(Command command) throws Exception;
    }
    @FunctionalInterface public interface OwnedJourneyReadPort {
        Optional<StoredResult> getOwned(UUID ownerId, UUID journeyId) throws Exception;
    }
    @FunctionalInterface public interface ResponseLossHook { void afterCommit() throws Exception; }
    @FunctionalInterface public interface OfflinePublicationPort {
        boolean publish(UUID operationId) throws Exception;
    }

    /**
     * Learner implements the commit / lost-response / safe-recovery observation.
     * Header interpretation and database transaction ownership belong to ports;
     * this seam must not manufacture a second operation after uncertainty.
     */
    public GapReport crashAfterShortlistCommitBeforeResponse(
            Command command,
            ShortlistCommandPort shortlist,
            OwnedJourneyReadPort ownedJourney,
            ResponseLossHook responseLoss,
            OfflinePublicationPort publication) {
        // LEARNER TODO: commit once, model the response as UNKNOWN when the hook
        // drops it, then resolve with the same operation key or an owner-scoped
        // read. Report optional publication gaps separately from durable audit.
        throw new UnsupportedOperationException("CAP-68 learner TODO: recover committed shortlist after response loss");
    }
}
