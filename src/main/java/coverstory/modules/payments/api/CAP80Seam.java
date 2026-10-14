package coverstory.modules.payments.api;

import java.util.UUID;
import java.util.function.Function;

/** Ordered replay of validated synthetic payment confirmations; no provider client exists here. */
public final class CAP80Seam {
    public enum Status { APPLIED, ALREADY_APPLIED, GAP, FORBIDDEN }

    /** actorOwnerId is the authenticated principal; feedOwnerId scopes the immutable feed item. */
    public record OwnerPaymentItem(UUID actorOwnerId, UUID feedOwnerId, String eventId, int sequence,
                                   UUID applicationId, String quoteId, long amountMinor,
                                   String currency, boolean confirmed) { }

    /** Snapshot handed to the bounded operation; the transaction re-reads its locked authoritative checkpoint. */
    public record PaymentCheckpoint(UUID ownerId, int lastAppliedSequence) { }

    public record ReplayResult(Status status, int checkpointAfter) { }

    /** Public transaction boundary supplied by Payments; all callback operations share its database transaction. */
    @FunctionalInterface
    public interface PaymentEffectPort {
        ReplayResult inUnitOfWork(UUID ownerId, Function<ReplayTransaction, ReplayResult> work);
    }

    /** Public module operations available inside the shared unit of work, with no repository access. */
    public interface ReplayTransaction {
        PaymentCheckpoint lockCheckpoint(UUID ownerId);
        void appendMockCapture(OwnerPaymentItem item);
        void unlockOnboarding(UUID ownerId, OwnerPaymentItem item);
        void advanceCheckpoint(UUID ownerId, int sequence);
    }

    public ReplayResult applyConfirmation(OwnerPaymentItem item, PaymentCheckpoint checkpoint,
                                         PaymentEffectPort port) {
        // LEARNER TODO: deny a principal/feed/checkpoint owner mismatch before
        // opening the transaction; lock and read the current checkpoint; return
        // ALREADY_APPLIED for old/duplicate sequences or GAP for a skipped one;
        // atomically append one mock effect, unlock one owned onboarding case,
        // write its timeline, and advance the checkpoint. Never call a provider.
        throw new UnsupportedOperationException("CAP-80 learner TODO: apply ordered mock confirmation");
    }
}
