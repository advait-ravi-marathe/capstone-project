package assignments.schema;

import org.junit.jupiter.api.Assertions;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Shared exact oracle for the learner seam, instructor reference, and non-transactional mutant. */
final class BackfillAtomicityAcceptanceOracle {
    private BackfillAtomicityAcceptanceOracle() { }

    @FunctionalInterface
    interface BatchRunner {
        Integer runBatch(DataSource db, String jobId, int batchSize) throws Exception;
    }

    static void checkpointWriteFailureRollsBackAndResumes(DataSource db, String jobId,
                                                           BatchRunner runner) throws Exception {
        try (Connection c = db.getConnection(); Statement s = c.createStatement()) {
            s.execute("""
                    CREATE FUNCTION fail_backfill_checkpoint_write() RETURNS trigger LANGUAGE plpgsql AS $$
                    BEGIN
                      IF EXISTS (SELECT 1 FROM gap_backfill_item WHERE normalized_value IS NOT NULL) THEN
                        RAISE EXCEPTION 'injected checkpoint write failure after row updates';
                      END IF;
                      RETURN NEW;
                    END; $$
                    """);
            s.execute("CREATE CONSTRAINT TRIGGER fail_backfill_checkpoint_write AFTER INSERT OR UPDATE "
                    + "ON gap_backfill_checkpoint DEFERRABLE INITIALLY DEFERRED "
                    + "FOR EACH ROW EXECUTE FUNCTION fail_backfill_checkpoint_write()");
        }
        Assertions.assertThrows(Exception.class, () -> runner.runBatch(db, jobId, 3),
                "deferred checkpoint failure after row changes must abort the batch");
        try (Connection c = db.getConnection(); Statement s = c.createStatement()) {
            s.execute("DROP TRIGGER fail_backfill_checkpoint_write ON gap_backfill_checkpoint");
            s.execute("DROP FUNCTION fail_backfill_checkpoint_write()");
        }
        assertSnapshot(db, jobId, 0);
        Assertions.assertEquals(3, runner.runBatch(db, jobId, 3), "a fresh batch invocation resumes after rollback");
        assertSnapshot(db, jobId, 3);
        Integer cursor;
        do { cursor = runner.runBatch(db, jobId, 3); } while (cursor != null);
        assertSnapshot(db, jobId, 10);
    }

    static void assertSnapshot(DataSource db, String jobId, int cursor) throws Exception {
        List<Row> actual = new ArrayList<>();
        try (Connection c = db.getConnection(); Statement s = c.createStatement();
             ResultSet rows = s.executeQuery("SELECT item_id,normalized_value,normalization_version,update_count "
                     + "FROM gap_backfill_item ORDER BY item_id")) {
            while (rows.next()) actual.add(new Row(rows.getInt(1), rows.getString(2),
                    rows.getObject(3, Integer.class), rows.getInt(4)));
        }
        List<Row> expected = new ArrayList<>();
        for (int id = 1; id <= 10; id++) {
            boolean complete = id <= cursor;
            expected.add(new Row(id, complete ? ExpectedBackfillRows.VALUES.get(id) : null,
                    complete ? 1 : null, complete ? 1 : 0));
        }
        Integer observedCursor = null;
        try (Connection c = db.getConnection(); var s = c.prepareStatement(
                "SELECT last_item_id FROM gap_backfill_checkpoint WHERE job_id=?")) {
            s.setString(1, jobId);
            try (ResultSet rows = s.executeQuery()) { if (rows.next()) observedCursor = rows.getInt(1); }
        }
        Assertions.assertEquals(expected, actual,
                "all ten ordered rows, exact normalized values, versions and update counts");
        Assertions.assertEquals(cursor == 0 ? null : cursor, observedCursor,
                "durable cursor must exactly match committed rows");
    }

    private record Row(int id, String normalized, Integer version, int updates) { }
}
