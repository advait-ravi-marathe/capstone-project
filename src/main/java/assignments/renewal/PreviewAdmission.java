package assignments.renewal;

import java.util.Optional;
import java.util.concurrent.Semaphore;

/** Nonblocking process-local admission gate; no request waits for a permit. */
public final class PreviewAdmission {
    private final Semaphore permits;

    public PreviewAdmission(PreviewCapacityPolicy policy) {
        this.permits = new Semaphore(policy.permitCapacity());
    }

    public Optional<PreviewLease> tryEnter() {
        // LEARNER TODO Day 73: acquire immediately or return empty; never block or queue.
        throw new UnsupportedOperationException("Day 73 learner TODO: nonblocking preview admission");
    }

    int availablePermitsForTest() { return permits.availablePermits(); }
}
