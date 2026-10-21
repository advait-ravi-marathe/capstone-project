package assignments.renewal;

import java.util.concurrent.atomic.AtomicBoolean;

/** A single admission permit with idempotent close semantics. */
public final class PreviewLease implements AutoCloseable {
    private final Runnable release;
    private final AtomicBoolean closed = new AtomicBoolean();

    public PreviewLease(Runnable release) {
        this.release = release;
    }

    @Override
    public void close() {
        // LEARNER TODO Day 74: run release exactly once, including concurrent close calls.
        throw new UnsupportedOperationException("Day 74 learner TODO: release preview lease exactly once");
    }
}
