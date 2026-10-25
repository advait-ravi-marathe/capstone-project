package assignments.schema;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Learner acceptance seam; remains red while ResumableBackfill is TODO. */
@Tag("learnerAcceptance")
class ResumableBackfillAcceptanceTest {
    @Test void midBatchFailureRollsBackLearnerRowsAndCursorThenFreshConnectionResumesExactly() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            var db = postgres.getPostgresDatabase();
            Flyway.configure().dataSource(db).locations("classpath:db/gap_schema").target("2").load().migrate();
            try (var c = db.getConnection(); var s = c.createStatement()) {
                s.execute("""
                        CREATE FUNCTION fail_backfill_second_row() RETURNS trigger LANGUAGE plpgsql AS $$
                        BEGIN
                          IF NEW.item_id = 2 THEN RAISE EXCEPTION 'injected mid-batch failure'; END IF;
                          RETURN NEW;
                        END; $$
                        """);
                s.execute("CREATE TRIGGER fail_backfill_second_row BEFORE UPDATE ON gap_backfill_item "
                        + "FOR EACH ROW EXECUTE FUNCTION fail_backfill_second_row()");
            }
            assertThrows(Exception.class,
                    () -> ResumableBackfill.runBatch(db, "coverstory-policy-normalization-v1", 3),
                    "the trigger must interrupt the real learner write path after row 1");
            try (var c = db.getConnection(); var s = c.createStatement()) {
                s.execute("DROP TRIGGER fail_backfill_second_row ON gap_backfill_item");
                s.execute("DROP FUNCTION fail_backfill_second_row()");
            }
            // The independent snapshot opens new connections and catches a committed row without its cursor.
            assertLearnerSnapshot(db, 0);
            assertEquals(3, ResumableBackfill.runBatch(db, "coverstory-policy-normalization-v1", 3),
                    "a new learner invocation must resume from the rolled-back durable cursor");
            assertLearnerSnapshot(db, 3);
            Integer cursor;
            do {
                cursor = ResumableBackfill.runBatch(db, "coverstory-policy-normalization-v1", 3);
            } while (cursor != null);
            assertLearnerSnapshot(db, 10);
        }
    }

    @Test void checkpointWriteFailureRollsBackEarlierLearnerRowUpdatesThenFreshConnectionResumesExactly() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            var db = postgres.getPostgresDatabase();
            Flyway.configure().dataSource(db).locations("classpath:db/gap_schema").target("2").load().migrate();
            try (var c = db.getConnection(); var s = c.createStatement()) {
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
            assertThrows(Exception.class,
                    () -> ResumableBackfill.runBatch(db, "coverstory-policy-normalization-v1", 3),
                    "checkpoint failure after row updates must abort the learner transaction");
            try (var c = db.getConnection(); var s = c.createStatement()) {
                s.execute("DROP TRIGGER fail_backfill_checkpoint_write ON gap_backfill_checkpoint");
                s.execute("DROP FUNCTION fail_backfill_checkpoint_write()");
            }
            // A non-transactional row UPDATE followed by an UPSERT would leave rows changed but no cursor.
            assertLearnerSnapshot(db, 0);
            assertEquals(3, ResumableBackfill.runBatch(db, "coverstory-policy-normalization-v1", 3));
            assertLearnerSnapshot(db, 3);
            Integer cursor;
            do {
                cursor = ResumableBackfill.runBatch(db, "coverstory-policy-normalization-v1", 3);
            } while (cursor != null);
            assertLearnerSnapshot(db, 10);
        }
    }

    @Test void boundedRunCanRestartAndMustMatchEveryIndependentExpectedRow() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            var db = postgres.getPostgresDatabase();
            Flyway.configure().dataSource(db).locations("classpath:db/gap_schema").target("2").load().migrate();
            assertEquals(3, ResumableBackfill.runBatch(db, "coverstory-policy-normalization-v1", 3));
            assertLearnerSnapshot(db, 3);
            assertEquals(6, ResumableBackfill.runBatch(db, "coverstory-policy-normalization-v1", 3));
            assertLearnerSnapshot(db, 6);
            // A new batch invocation resumes from only the durable cursor recorded by the learner.
            assertEquals(9, ResumableBackfill.runBatch(db, "coverstory-policy-normalization-v1", 3));
            assertLearnerSnapshot(db, 9);
            assertEquals(10, ResumableBackfill.runBatch(db, "coverstory-policy-normalization-v1", 3));
            assertLearnerSnapshot(db, 10);
            assertNull(ResumableBackfill.runBatch(db, "coverstory-policy-normalization-v1", 3));
        }
    }

    @Test void wrongLastRowValueMutantFailsTheSameExactLearnerAcceptanceOracle() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            var db = postgres.getPostgresDatabase();
            Flyway.configure().dataSource(db).locations("classpath:db/gap_schema").target("2").load().migrate();
            for (int cursor = 0; cursor < 10; cursor += 3) ResumableBackfill.runBatch(db, "coverstory-policy-normalization-v1", 3);
            try (var c = db.getConnection(); var s = c.createStatement()) {
                s.executeUpdate("UPDATE gap_backfill_item SET normalized_value='wrong-last-row' WHERE item_id=10");
            }
            assertThrows(AssertionError.class, () -> assertLearnerSnapshot(db, 10),
                    "an incorrect value for row 10 must fail the learner's exact all-row oracle");
        }
    }

    private record ObservedRow(int id, String normalized, Integer version, int updates) { }

    private static void assertLearnerSnapshot(javax.sql.DataSource db, int cursor) throws Exception {
        List<ObservedRow> actual = new ArrayList<>();
        try (var c = db.getConnection(); var s = c.createStatement();
             var rows = s.executeQuery("SELECT item_id,normalized_value,normalization_version,update_count FROM gap_backfill_item ORDER BY item_id")) {
            while (rows.next()) actual.add(new ObservedRow(rows.getInt(1), rows.getString(2),
                    rows.getObject(3, Integer.class), rows.getInt(4)));
        }
        List<ObservedRow> expected = new ArrayList<>();
        for (int id = 1; id <= 10; id++) {
            boolean done = id <= cursor;
            expected.add(new ObservedRow(id, done ? ExpectedBackfillRows.VALUES.get(id) : null,
                    done ? 1 : null, done ? 1 : 0));
        }
        Integer actualCursor = null;
        try (var c = db.getConnection(); var s = c.prepareStatement(
                "SELECT last_item_id FROM gap_backfill_checkpoint WHERE job_id=?")) {
            s.setString(1, "coverstory-policy-normalization-v1");
            try (var rows = s.executeQuery()) { if (rows.next()) actualCursor = rows.getInt(1); }
        }
        assertEquals(expected, actual, "all ten ordered rows, exact normalized values, versions and update counts");
        assertEquals(cursor == 0 ? null : cursor, actualCursor, "durable cursor must exactly match the rows committed");
    }

    private static int scalar(javax.sql.DataSource db, String sql) throws Exception {
        try (var c = db.getConnection(); var s = c.createStatement(); var rows = s.executeQuery(sql)) { rows.next(); return rows.getInt(1); }
    }
}
