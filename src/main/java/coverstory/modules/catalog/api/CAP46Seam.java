package coverstory.modules.catalog.api;

import java.util.Optional;

/** Bounded FIFO contract supplied for CAP-46; learner supplies the queue behavior. */
public final class CAP46Seam {
    private final int capacity;

    public CAP46Seam(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
        this.capacity = capacity;
    }

    public boolean enqueue(SourceVersionKey key) {
        // LEARNER TODO: enforce identity uniqueness while pending and capacity.
        throw new UnsupportedOperationException("CAP-46 learner TODO: implement enqueue");
    }

    public Optional<SourceVersionKey> poll() {
        // LEARNER TODO: remove and return the oldest key.
        throw new UnsupportedOperationException("CAP-46 learner TODO: implement poll");
    }

    public int size() {
        // LEARNER TODO: report current queue size.
        throw new UnsupportedOperationException("CAP-46 learner TODO: implement size");
    }

    public int capacity() { return capacity; }
}
