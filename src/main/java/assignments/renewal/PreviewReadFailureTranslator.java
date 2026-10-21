package assignments.renewal;

/** Narrowly classifies PostgreSQL lock-not-available and query-canceled failures. */
public final class PreviewReadFailureTranslator {
    private PreviewReadFailureTranslator() { }

    public static RuntimeException translate(RuntimeException failure) {
        // LEARNER TODO Day 92: find nested SQLExceptions; map only 55P03 and 57014 to a fixed,
        // non-sensitive response exception while retaining the original cause. 57014 means
        // query_canceled and does not by itself prove which cancellation source fired.
        throw new UnsupportedOperationException("Day 92 learner TODO: translate only bounded-read SQL states");
    }
}
