package assignments.schema;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Independent witness for actual Flyway and SQL behavior; no completion is recorded. */
class SchemaEvolutionInfrastructureTest {
    private static final String JOB = "coverstory-policy-normalization-v1";

    @Test void expandMigrationPreservesRowsAndOldWriterCompatibility() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource db = postgres.getPostgresDatabase();
            migrateTo(db, "1");
            String before = legacyRows(db);
            migrateTo(db, "2");
            assertEquals(before, legacyRows(db), "additive expand must preserve exact baseline rows");
            try (Connection c = db.getConnection(); Statement s = c.createStatement()) {
                s.executeUpdate("INSERT INTO gap_backfill_item(item_id,legacy_value) VALUES (11,'Old writer row')");
            }
            assertEquals("Old writer row", scalar(db, "SELECT legacy_value FROM gap_backfill_item WHERE item_id=11"));
            assertEquals("11", scalar(db, "SELECT count(*) FROM gap_backfill_item"));
        }
    }

    @Test void restartResumesFromPersistedCursorWithoutSkippingOrDuplicating() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource db = expanded(postgres);
            assertEquals(3, ReferenceBackfill.runBatch(db, JOB, 3));
            assertEquals(6, ReferenceBackfill.runBatch(db, JOB, 3));
            assertPartial(db, 6);
            // New runner invocation simulates a process restart; only the database cursor survives.
            assertEquals(9, ReferenceBackfill.runBatch(db, JOB, 3));
            assertEquals(10, ReferenceBackfill.runBatch(db, JOB, 3));
            assertNull(ReferenceBackfill.runBatch(db, JOB, 3));
            assertRows(db, ExpectedBackfillRows.VALUES, true);
            assertEquals("10", scalar(db, "SELECT last_item_id FROM gap_backfill_checkpoint WHERE job_id='" + JOB + "'"));
        }
    }

    @Test void wrongCursorAndBatchMutantsFailTheExactRowAndCursorOracle() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource db = expanded(postgres);
            try (Connection c = db.getConnection(); Statement s = c.createStatement()) {
                s.executeUpdate("INSERT INTO gap_backfill_checkpoint(job_id,last_item_id) VALUES ('" + JOB + "',999)");
            }
            assertThrows(IllegalArgumentException.class, () -> ReferenceBackfill.runBatch(db, JOB, 3));
            assertEquals("10", scalar(db, "SELECT count(*) FROM gap_backfill_item WHERE normalized_value IS NULL"));
        }
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource db = expanded(postgres);
            try (Connection c = db.getConnection(); Statement s = c.createStatement()) {
                // Deliberate off-by-one implementation mutant skips item 1 and lies that the cursor is 3.
                s.executeUpdate("UPDATE gap_backfill_item SET normalized_value=lower(trim(legacy_value)), normalization_version=1, update_count=1 WHERE item_id BETWEEN 2 AND 3");
                s.executeUpdate("INSERT INTO gap_backfill_checkpoint(job_id,last_item_id) VALUES ('" + JOB + "',3)");
            }
            assertThrows(AssertionError.class, () -> assertPartial(db, 3), "independent exact-ID/cursor oracle must reject skipped row");
        }
    }

    @Test void oldAndNewReadersRemainCompatibleAcrossBackfill() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource db = expanded(postgres);
            assertEquals(" Policy Alpha ", scalar(db, "SELECT legacy_value FROM gap_backfill_item WHERE item_id=1"));
            while (ReferenceBackfill.runBatch(db, JOB, 4) != null) { }
            assertEquals(" Policy Alpha ", scalar(db, "SELECT legacy_value FROM gap_backfill_item WHERE item_id=1"));
            assertEquals("policy alpha", scalar(db, "SELECT normalized_value FROM gap_backfill_item WHERE item_id=1"));
            assertEquals("10", scalar(db, "SELECT count(*) FROM gap_backfill_item WHERE normalization_version=1"));
        }
    }

    @Test void forwardMigrationRepairsDriftAndContractRequiresRetiredReaders() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource db = expanded(postgres);
            while (ReferenceBackfill.runBatch(db, JOB, 4) != null) { }
            try (Connection c = db.getConnection(); Statement s = c.createStatement()) {
                s.executeUpdate("ALTER TABLE gap_backfill_item DROP COLUMN normalized_value");
            }
            assertThrows(AssertionError.class, () -> assertTrue(hasColumn(db, "normalized_value"),
                    "unsafe destructive candidate must preserve the expanded output column"),
                    "the independent schema acceptance assertion must reject the destructive mutant");
            migrateTo(db, "3");
            assertRows(db, ExpectedBackfillRows.VALUES, false);

            Set<String> required = Set.of("legacy-policy-reader", "legacy-reporting-job");
            assertFalse(attemptContract(db, Set.of("legacy-policy-reader"), required,
                    ReferenceSchemaContractGate::mayContract));
            assertTrue(hasColumn(db, "legacy_value"), "denied contract must leave the old column untouched");
            assertEquals("003", flyway(db).info().current().getVersion().toString());
            assertTrue(attemptContract(db, Set.of("legacy-policy-reader", "legacy-reporting-job"), required,
                    ReferenceSchemaContractGate::mayContract));
            assertFalse(hasColumn(db, "legacy_value"));
            assertEquals("policy alpha", scalar(db, "SELECT normalized_value FROM gap_backfill_item WHERE item_id=1"));
        }
    }

    @Test void contractGateMutantsAreRejectedByIndependentEligibilityOracle() {
        Set<String> required = Set.of("old-api-reader", "old-reporting-job");
        assertFalse(ReferenceSchemaContractGate.mayContract(Set.of("old-api-reader"), required));
        assertTrue(ReferenceSchemaContractGate.mayContract(Set.of("old-api-reader", "old-reporting-job", "unknown"), required),
                "a superset of required retirements remains eligible");
        assertTrue(ReferenceSchemaContractGate.mayContract(required, required));
        assertThrows(AssertionError.class, () -> assertEquals(false,
                alwaysPermitContract(Set.of("old-api-reader"), required)),
                "always-permit mutant must fail the actual eligibility assertion");
    }

    @Test void earlyContractMutantWouldExecuteRealDestructiveMigrationAndIsRejected() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource db = expanded(postgres);
            Set<String> required = Set.of("legacy-policy-reader", "legacy-reporting-job");
            assertThrows(AssertionError.class, () -> {
                boolean eligible = attemptContract(db, Set.of("legacy-policy-reader"), required,
                        SchemaEvolutionInfrastructureTest::alwaysPermitContract);
                assertEquals(false, eligible, "actual acceptance path must reject a premature contract");
            });
            assertFalse(hasColumn(db, "legacy_value"),
                    "the injected always-permit mutant did reach the disposable database's destructive migration path");
        }
    }

    private static boolean alwaysPermitContract(Set<String> active, Set<String> required) { return true; }

    private static boolean attemptContract(DataSource db, Set<String> active, Set<String> required,
                                           java.util.function.BiPredicate<Set<String>, Set<String>> gate) {
        if (!gate.test(active, required)) return false;
        migrateTo(db, "4");
        return true;
    }

    private static Flyway flyway(DataSource db) {
        return Flyway.configure().dataSource(db).locations("classpath:db/gap_schema").load();
    }

    private static void migrateTo(DataSource db, String target) {
        Flyway.configure().dataSource(db).locations("classpath:db/gap_schema").target(target).load().migrate();
    }

    private static DataSource expanded(EmbeddedPostgres postgres) {
        DataSource db = postgres.getPostgresDatabase();
        migrateTo(db, "2");
        return db;
    }

    private static void assertPartial(DataSource db, int cursor) throws Exception {
        Map<Integer, String> expected = ExpectedBackfillRows.VALUES;
        for (var row : expected.entrySet()) {
            boolean shouldBeDone = row.getKey() <= cursor;
            String actual = scalar(db, "SELECT COALESCE(normalized_value,'<null>') FROM gap_backfill_item WHERE item_id=" + row.getKey());
            assertEquals(shouldBeDone ? row.getValue() : "<null>", actual, "independent output for row " + row.getKey());
            int updates = Integer.parseInt(scalar(db, "SELECT update_count FROM gap_backfill_item WHERE item_id=" + row.getKey()));
            assertEquals(shouldBeDone ? 1 : 0, updates, "independent per-row update count for row " + row.getKey());
        }
        assertEquals(Integer.toString(cursor), scalar(db, "SELECT last_item_id FROM gap_backfill_checkpoint WHERE job_id='" + JOB + "'"));
    }

    private static void assertRows(DataSource db, Map<Integer, String> expected, boolean requireLegacy) throws Exception {
        assertEquals(Integer.toString(expected.size()), scalar(db, "SELECT count(*) FROM gap_backfill_item"));
        for (var row : expected.entrySet()) {
            String actual = scalar(db, "SELECT normalized_value FROM gap_backfill_item WHERE item_id=" + row.getKey());
            assertEquals(row.getValue(), actual, "independent normalized value for ID " + row.getKey());
            if (requireLegacy) assertNotNull(scalar(db, "SELECT legacy_value FROM gap_backfill_item WHERE item_id=" + row.getKey()));
        }
    }

    private static String legacyRows(DataSource db) throws Exception {
        StringBuilder out = new StringBuilder();
        try (Connection c = db.getConnection(); Statement s = c.createStatement();
             ResultSet rows = s.executeQuery("SELECT item_id || ':' || legacy_value FROM gap_backfill_item ORDER BY item_id")) {
            while (rows.next()) out.append(rows.getString(1)).append('\n');
        }
        return out.toString();
    }

    private static String scalar(DataSource db, String sql) throws Exception {
        try (Connection c = db.getConnection(); Statement s = c.createStatement(); ResultSet rows = s.executeQuery(sql)) {
            assertTrue(rows.next(), "scalar query produced no result: " + sql); return rows.getString(1);
        }
    }

    private static boolean hasColumn(DataSource db, String name) throws Exception {
        try (Connection c = db.getConnection(); PreparedStatement s = c.prepareStatement("""
                SELECT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_name='gap_backfill_item' AND column_name=?)
                """)) {
            s.setString(1, name); try (ResultSet rows = s.executeQuery()) { rows.next(); return rows.getBoolean(1); }
        }
    }
}
