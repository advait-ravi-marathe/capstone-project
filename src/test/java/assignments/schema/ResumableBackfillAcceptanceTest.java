package assignments.schema;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Learner acceptance seam; remains red while ResumableBackfill is TODO. */
@Tag("learnerAcceptance")
class ResumableBackfillAcceptanceTest {
    @Test void boundedRunCanRestartAndMustMatchEveryIndependentExpectedRow() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            var db = postgres.getPostgresDatabase();
            Flyway.configure().dataSource(db).locations("classpath:db/gap_schema").target("2").load().migrate();
            assertEquals(3, ResumableBackfill.runBatch(db, "coverstory-policy-normalization-v1", 3));
            assertEquals(6, ResumableBackfill.runBatch(db, "coverstory-policy-normalization-v1", 3));
            for (int id = 1; id <= 10; id++) {
                try (var c = db.getConnection(); var s = c.prepareStatement("SELECT normalized_value,update_count FROM gap_backfill_item WHERE item_id=?")) {
                    s.setInt(1, id);
                    try (var rows = s.executeQuery()) {
                        assertTrue(rows.next());
                        if (id <= 6) {
                            assertEquals(ExpectedBackfillRows.VALUES.get(id), rows.getString(1));
                            assertEquals(1, rows.getInt(2));
                        } else {
                            assertNull(rows.getString(1));
                            assertEquals(0, rows.getInt(2));
                        }
                    }
                }
            }
            while (ResumableBackfill.runBatch(db, "coverstory-policy-normalization-v1", 3) != null) { }
            assertEquals(10, scalar(db, "SELECT count(*) FROM gap_backfill_item WHERE normalization_version=1 AND update_count=1"));
        }
    }

    private static int scalar(javax.sql.DataSource db, String sql) throws Exception {
        try (var c = db.getConnection(); var s = c.createStatement(); var rows = s.executeQuery(sql)) { rows.next(); return rows.getInt(1); }
    }
}
