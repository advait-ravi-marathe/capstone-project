package assignments.schema;

import javax.sql.DataSource;

/** Learner seam: bounded, restartable backfill over a stable primary-key cursor. */
public final class ResumableBackfill {
    private ResumableBackfill() { }

    /**
     * Apply at most batchSize rows after the durable cursor. A null return means the job is complete.
     * Persist cursor and row updates atomically; reject invalid batch sizes and malformed cursors.
     */
    public static Integer runBatch(DataSource dataSource, String jobId, int batchSize) {
        // TODO: read the durable cursor, update the next ordered batch, and commit cursor + rows together.
        throw new UnsupportedOperationException("TODO: implement a bounded restartable backfill");
    }
}
