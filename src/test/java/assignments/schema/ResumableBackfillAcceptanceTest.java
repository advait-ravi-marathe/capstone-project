package assignments.schema;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
            BackfillAtomicityAcceptanceOracle.checkpointWriteFailureRollsBackAndResumes(
                    db, "coverstory-policy-normalization-v1", ResumableBackfill::runBatch);
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

    private static void assertLearnerSnapshot(javax.sql.DataSource db, int cursor) throws Exception {
        BackfillAtomicityAcceptanceOracle.assertSnapshot(db, "coverstory-policy-normalization-v1", cursor);
    }
}
