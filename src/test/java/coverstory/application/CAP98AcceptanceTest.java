package coverstory.application;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Learner contract executes against a fresh disposable PostgreSQL instance per case. */
@Tag("learnerAcceptance")
class CAP98AcceptanceTest {
    private static final Path V1_SQL = Path.of("src/test/resources/coverstory/application/cap_98_v1.sql");
    private final RehearseCompatibleApplicationSchemaExpansion seam =
            new RehearseCompatibleApplicationSchemaExpansion();
    private final RehearseCompatibleApplicationSchemaExpansion.SchemaV1 contract =
            new RehearseCompatibleApplicationSchemaExpansion.SchemaV1(
                    List.of(
                            new RehearseCompatibleApplicationSchemaExpansion.Revision("98000000-0000-4000-8000-000000000001","98000000-0000-4000-8000-000000000101",1,"a".repeat(64),"4".repeat(64),"1".repeat(64),null),
                            new RehearseCompatibleApplicationSchemaExpansion.Revision("98000000-0000-4000-8000-000000000001","98000000-0000-4000-8000-000000000102",2,"b".repeat(64),"5".repeat(64),"2".repeat(64),null),
                            new RehearseCompatibleApplicationSchemaExpansion.Revision("98000000-0000-4000-8000-000000000002","98000000-0000-4000-8000-000000000103",1,"c".repeat(64),"6".repeat(64),"3".repeat(64),null)),
                    List.of(
                            new RehearseCompatibleApplicationSchemaExpansion.PaymentReference("98000000-0000-4000-8000-000000000201","98000000-0000-4000-8000-000000000001",1),
                            new RehearseCompatibleApplicationSchemaExpansion.PaymentReference("98000000-0000-4000-8000-000000000202","98000000-0000-4000-8000-000000000001",2),
                            new RehearseCompatibleApplicationSchemaExpansion.PaymentReference("98000000-0000-4000-8000-000000000203","98000000-0000-4000-8000-000000000002",1)));
    private static final String LEGACY = "SELECT revision_id, revision, quote_hash, revision_hash FROM application.application_revision ORDER BY revision_id";

    @Test void additiveNullableExpansionPreservesThreeRowsCompositeIdentityAndV1Reader() throws Exception {
        withDatabase(c -> {
            List<String> before = legacyRows(c, "application.application_revision");
            var result = seam.rehearseExpansion(c, contract,
                    RehearseCompatibleApplicationSchemaExpansion.Rehearsal.ADDITIVE_EXPANSION,
                    "ALTER TABLE application.application_revision ADD COLUMN source_version TEXT NULL", LEGACY);
            assertEquals(RehearseCompatibleApplicationSchemaExpansion.Status.VERIFIED, result.status());
            assertTrue(columnExists(c, "application", "application_revision", "source_version"));
            assertEquals(3, scalar(c, "SELECT count(*) FROM application.application_revision"));
            assertEquals(3, scalar(c, "SELECT count(*) FROM payments.payment_reference"));
            assertEquals(3, scalar(c, "SELECT count(*) FROM payments.payment_reference p JOIN application.application_revision r USING(application_id,revision)"));
            assertEquals(3, scalar(c, "SELECT count(*) FROM application.application_revision WHERE source_version IS NULL"));
            assertEquals(3, scalar(c, "SELECT count(*) FROM (" + LEGACY + ") v"));
            assertEquals(2, scalar(c, "SELECT count(DISTINCT application_id) FROM application.application_revision"));
            assertEquals(before, legacyRows(c, "application.application_revision"), "old reader and immutable hashes remain byte-identical");
            assertEquals(3, scalar(c, "SELECT count(*) FROM application.application_revision WHERE (application_id,revision) IN (( '98000000-0000-4000-8000-000000000001',1),('98000000-0000-4000-8000-000000000001',2),('98000000-0000-4000-8000-000000000002',1))"));
        });
    }

    @Test void failedMigrationRollsBackActualSchemaAndRows() throws Exception {
        withDatabase(c -> {
            List<String> before = legacyRows(c, "application.application_revision");
            var result = seam.rehearseExpansion(c, contract,
                    RehearseCompatibleApplicationSchemaExpansion.Rehearsal.INJECTED_FAILURE,
                    "ALTER TABLE application.application_revision ADD COLUMN source_version TEXT NULL; SELECT 1/0", LEGACY);
            assertEquals(RehearseCompatibleApplicationSchemaExpansion.Status.ROLLED_BACK, result.status());
            assertFalse(columnExists(c, "application", "application_revision", "source_version"));
            assertEquals(3, scalar(c, "SELECT count(*) FROM application.application_revision"));
            assertEquals(3, scalar(c, "SELECT count(*) FROM payments.payment_reference p JOIN application.application_revision r USING(application_id,revision)"));
            assertEquals(3, scalar(c, "SELECT count(*) FROM (" + LEGACY + ") v"));
            assertEquals(before, legacyRows(c, "application.application_revision"));
        });
    }

    @Test void downMigrationRefusesToEraseRealV2Value() throws Exception {
        withDatabase(c -> {
            try (Statement s = c.createStatement()) {
                s.execute("ALTER TABLE application.application_revision ADD COLUMN source_version TEXT NULL");
                s.execute("UPDATE application.application_revision SET source_version='source-v2' WHERE revision_id='98000000-0000-4000-8000-000000000102'");
            }
            var result = seam.rehearseExpansion(c, contract,
                    RehearseCompatibleApplicationSchemaExpansion.Rehearsal.GUARDED_DOWN,
                    "ALTER TABLE application.application_revision DROP COLUMN source_version", LEGACY);
            assertEquals(RehearseCompatibleApplicationSchemaExpansion.Status.REJECTED_DESTRUCTIVE_DOWN, result.status());
            assertTrue(columnExists(c, "application", "application_revision", "source_version"));
            assertEquals(1, scalar(c, "SELECT count(*) FROM application.application_revision WHERE source_version='source-v2'"));
            assertEquals(3, scalar(c, "SELECT count(*) FROM payments.payment_reference p JOIN application.application_revision r USING(application_id,revision)"));
        });
    }

    @Test void isolatedRestoreRecreatesRowsCompositeForeignKeysAndImmutableHashes() throws Exception {
        withDatabase(c -> {
            List<String> before = legacyRows(c, "application.application_revision");
            try(Statement s=c.createStatement()) {
                s.execute("ALTER TABLE application.application_revision ADD COLUMN source_version TEXT NULL");
                s.execute("UPDATE application.application_revision SET source_version='source-v2' WHERE revision_id='98000000-0000-4000-8000-000000000102'");
            }
            var result = seam.rehearseExpansion(c, contract,
                    RehearseCompatibleApplicationSchemaExpansion.Rehearsal.ISOLATED_RESTORE,
                    "cap98_restored", LEGACY);
            assertEquals(RehearseCompatibleApplicationSchemaExpansion.Status.VERIFIED, result.status());
            assertEquals(3, scalar(c, "SELECT count(*) FROM cap98_restored.application_revision"));
            assertEquals(3, scalar(c, "SELECT count(*) FROM cap98_restored.payment_reference"));
            assertEquals(3, scalar(c, "SELECT count(*) FROM cap98_restored.payment_reference p JOIN cap98_restored.application_revision r USING(application_id,revision)"));
            assertEquals(0, scalar(c, "SELECT count(*) FROM cap98_restored.payment_reference p LEFT JOIN cap98_restored.application_revision r USING(application_id,revision) WHERE r.revision_id IS NULL"));
            assertEquals(3, scalar(c, "SELECT count(*) FROM (SELECT revision_id, revision, quote_hash, revision_hash FROM cap98_restored.application_revision ORDER BY revision_id) v"));
            assertTrue(foreignKeyExists(c, "cap98_restored", "payment_reference"));
            assertEquals(2, scalar(c, "SELECT count(*) FROM information_schema.key_column_usage WHERE constraint_schema='cap98_restored' AND table_name='payment_reference' AND position_in_unique_constraint IS NOT NULL"), "the application_id/revision FK uses both identity columns");
            assertEquals(before, legacyRows(c, "cap98_restored.application_revision"));
            assertEquals(1, scalar(c, "SELECT count(*) FROM cap98_restored.application_revision WHERE source_version='source-v2'"));
        });
    }

    private static void withDatabase(SqlWork work) throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start(); Connection c = pg.getPostgresDatabase().getConnection()) {
            for (String sql : Files.readString(V1_SQL).split(";")) if (!sql.isBlank()) try (Statement s = c.createStatement()) { s.execute(sql); }
            work.run(c);
        }
    }
    private static int scalar(Connection c, String sql) throws Exception {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) { rs.next(); return rs.getInt(1); }
    }
    private static List<String> legacyRows(Connection c, String table) throws Exception {
        var rows = new java.util.ArrayList<String>();
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT application_id,revision_id,revision,quote_hash,revision_hash,terms_hash FROM " + table + " ORDER BY revision_id")) {
            while (rs.next()) rows.add(rs.getString(1)+"|"+rs.getString(2)+"|"+rs.getInt(3)+"|"+rs.getString(4).trim()+"|"+rs.getString(5).trim()+"|"+rs.getString(6).trim());
        }
        return rows;
    }
    private static boolean columnExists(Connection c, String schema, String table, String column) throws Exception {
        try (var ps = c.prepareStatement("SELECT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=? AND table_name=? AND column_name=?)")) {
            ps.setString(1, schema); ps.setString(2, table); ps.setString(3, column);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getBoolean(1); }
        }
    }
    private static boolean foreignKeyExists(Connection c, String schema, String table) throws Exception {
        try (var ps = c.prepareStatement("SELECT EXISTS(SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema=? AND table_name=? AND constraint_type='FOREIGN KEY')")) {
            ps.setString(1, schema); ps.setString(2, table);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getBoolean(1); }
        }
    }
    @FunctionalInterface private interface SqlWork { void run(Connection connection) throws Exception; }
}
