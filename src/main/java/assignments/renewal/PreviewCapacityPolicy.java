package assignments.renewal;

/** Immutable safety limits for synchronous renewal-preview reads. */
public record PreviewCapacityPolicy(int permitCapacity, int poolMaximumSize, int connectionTimeoutMillis) {
    public void validate() {
        // LEARNER TODO Day 72: require positive capacities, permits <= poolMaximumSize,
        // and a finite Hikari connection timeout in the documented 250..30000ms range.
        throw todo(72);
    }

    private static UnsupportedOperationException todo(int day) {
        return new UnsupportedOperationException("Day %02d learner TODO: validate preview capacity policy".formatted(day));
    }
}
