package assignments.schema;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class SchemaContractGateAcceptanceTest {
    private static final Set<String> REQUIRED_READERS = Set.of("legacy-policy-reader", "legacy-reporting-job");
    private static final Set<String> REQUIRED_WRITERS = Set.of("legacy-policy-writer");

    @Test void readers_writers_and_complete_backfill_are_all_required_before_contract() {
        Set<String> readers = Set.of("legacy-policy-reader", "legacy-reporting-job");
        Set<String> writers = Set.of("legacy-policy-writer");
        assertFalse(SchemaContractGate.mayContract(readers, readers, writers, writers, false));
        assertFalse(SchemaContractGate.mayContract(Set.of("legacy-policy-reader"), readers, writers, writers, true));
        assertFalse(SchemaContractGate.mayContract(readers, readers, Set.of(), writers, true));
        assertFalse(SchemaContractGate.mayContract(null, readers, writers, writers, true));
        assertFalse(SchemaContractGate.mayContract(readers, Set.of(), writers, writers, true));
        assertFalse(SchemaContractGate.mayContract(readers, readers, writers, Set.of(), true));
        assertTrue(SchemaContractGate.mayContract(readers, readers, writers, writers, true));
    }

    @Test void learnerGateControlsActualContractMigrationAndDeniedInputsPreserveRows() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            var db = postgres.getPostgresDatabase();
            Flyway.configure().dataSource(db).locations("classpath:db/gap_schema").target("3").load().migrate();
            String before = legacyAndNormalizedRows(db);

            assertFalse(attemptLearnerContract(db, REQUIRED_READERS, REQUIRED_WRITERS, false),
                    "incomplete backfill must deny the actual contract migration");
            assertFalse(attemptLearnerContract(db, Set.of("legacy-policy-reader"), REQUIRED_WRITERS, true),
                    "an active old reader must deny the actual contract migration");
            assertFalse(attemptLearnerContract(db, REQUIRED_READERS, Set.of(), true),
                    "an active old writer must deny the actual contract migration");
            assertTrue(hasLegacyColumn(db));
            assertEquals(before, legacyAndNormalizedRows(db), "denial must leave every row and value unchanged");

            assertTrue(attemptLearnerContract(db, REQUIRED_READERS, REQUIRED_WRITERS, true));
            assertFalse(hasLegacyColumn(db), "only an eligible learner gate may run destructive V004");
            assertEquals("1:policy alpha\n2:policy bravo\n3:policy charlie\n4:policy delta\n5:policy echo\n"
                    + "6:policy foxtrot\n7:policy golf\n8:policy hotel\n9:policy india\n10:policy juliet\n",
                    normalizedRows(db), "allowed contract preserves the complete independently expected normalized dataset");
        }
    }

    private static boolean attemptLearnerContract(javax.sql.DataSource db, Set<String> readers,
                                                  Set<String> writers, boolean complete) {
        if (!SchemaContractGate.mayContract(readers, REQUIRED_READERS, writers, REQUIRED_WRITERS, complete)) return false;
        Flyway.configure().dataSource(db).locations("classpath:db/gap_schema").target("4").load().migrate();
        return true;
    }

    private static boolean hasLegacyColumn(javax.sql.DataSource db) throws Exception {
        try (Connection c = db.getConnection(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_name='gap_backfill_item' AND column_name='legacy_value')")) {
            r.next(); return r.getBoolean(1);
        }
    }

    private static String legacyAndNormalizedRows(javax.sql.DataSource db) throws Exception {
        StringBuilder out = new StringBuilder();
        try (Connection c = db.getConnection(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT item_id,legacy_value,normalized_value,normalization_version,update_count FROM gap_backfill_item ORDER BY item_id")) {
            while (r.next()) out.append(r.getInt(1)).append(':').append(r.getString(2)).append(':')
                    .append(r.getString(3)).append(':').append(r.getInt(4)).append(':').append(r.getInt(5)).append('\n');
        }
        return out.toString();
    }

    private static String normalizedRows(javax.sql.DataSource db) throws Exception {
        StringBuilder out = new StringBuilder();
        try (Connection c = db.getConnection(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT item_id,normalized_value FROM gap_backfill_item ORDER BY item_id")) {
            while (r.next()) out.append(r.getInt(1)).append(':').append(r.getString(2)).append('\n');
        }
        return out.toString();
    }
}
