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
        assertEquals(cursor, actualCursor, "durable cursor must exactly match the rows committed");
    }

    private static int scalar(javax.sql.DataSource db, String sql) throws Exception {
        try (var c = db.getConnection(); var s = c.createStatement(); var rows = s.executeQuery(sql)) { rows.next(); return rows.getInt(1); }
    }
}
