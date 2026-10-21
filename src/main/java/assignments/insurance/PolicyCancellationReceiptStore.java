package assignments.insurance;

import java.util.Optional;

/** Learner data seam for replaying or conflicting cancellation commands. */
@FunctionalInterface
public interface PolicyCancellationReceiptStore {
    Optional<PolicyCancellationReceipt> find(String ownerId, String clientRequestId);
}
