package assignments.renewal;

import io.micrometer.core.instrument.MeterRegistry;


/** Fixed-cardinality preview metrics; outcome values are a closed enum. */
public final class PreviewMetrics {
    public enum Outcome { SUCCESS, EMPTY, BUSY, UNAVAILABLE, FAILURE }

    private final MeterRegistry registry;

    public PreviewMetrics(MeterRegistry registry) { this.registry = registry; }

    public void record(Outcome outcome, long elapsedNanos) {
        // LEARNER TODO Day 80: record bounded timer/counter series using only the fixed
        // outcome enum. Never tag IDs, retain requests, or allocate per-request meters.
        throw new UnsupportedOperationException("Day 80 learner TODO: record bounded preview metrics");
    }
}
