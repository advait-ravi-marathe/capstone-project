package coverstory.application;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Independent disposable PostgreSQL oracle. It never invokes the learner seam. */
class CAP98InstructorInfrastructureTest {
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-98/synthetic-v1.json");
    private static final Path V1_SQL = Path.of("src/test/resources/coverstory/application/cap_98_v1.sql");
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test void syntheticInputsAndV1SchemaContainOnlyThreeOwnedRevisionsAndReferences() throws Exception {
        JsonNode fixture = fixture();
        assertEquals("SYNTHETIC_DISPOSABLE_POSTGRES_FIXTURE", fixture.path("classification").asText());
        assertFalse(fixture.path("production_database_access").asBoolean());
        assertFalse(fixture.path("migration_applied_to_production").asBoolean());
        assertEquals(3, fixture.path("revisions").size());
        assertEquals(3, fixture.path("payment_references").size());
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start();
             Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            loadV1(c);
            assertEquals(3, scalarInt(s, "SELECT count(*) FROM application.application_revision"));
            assertEquals(3, scalarInt(s, "SELECT count(*) FROM payments.payment_reference"));
            assertEquals(0, scalarInt(s, "SELECT count(*) FROM information_schema.columns WHERE table_schema='application' AND table_name='application_revision' AND column_name='source_version'"));
            assertEquals(3, scalarInt(s, "SELECT count(*) FROM payments.payment_reference p JOIN application.application_revision r USING(application_id,revision)"));
            assertEquals(fixtureRevisionRows(fixture), legacyRows(c), "all three fixture composite identities and immutable hashes match PostgreSQL");
            assertEquals(fixturePaymentRows(fixture), paymentRows(c), "all three fixture payment references match their application/revision pair");
            assertEquals(1, scalarInt(s, "SELECT count(*) FROM information_schema.table_constraints WHERE constraint_schema='application' AND table_name='application_revision' AND constraint_type='UNIQUE'"));
        }
    }

    @Test void additiveNullableExpansionPreservesV1ReaderRowsAndOwnedPaymentForeignKeys() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start();
             Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            loadV1(c);
            List<String> before = legacyRows(c);
            c.setAutoCommit(false);
            s.execute("ALTER TABLE application.application_revision ADD COLUMN source_version TEXT NULL");
            assertEquals(3, scalarInt(s, "SELECT count(*) FROM application.application_revision"));
            assertEquals(3, scalarInt(s, "SELECT count(*) FROM payments.payment_reference p JOIN application.application_revision r USING(application_id,revision)"));
            assertEquals(before, legacyRows(c), "the v1 projection reads the same revision and immutable hashes");
            assertEquals(3, scalarInt(s, "SELECT count(*) FROM application.application_revision WHERE source_version IS NULL"));
            c.commit();
            assertTrue(hasColumn(c, "source_version"));
            assertEquals(before, legacyRows(c));
        }
    }

    @Test void injectedPreCommitFailureRollsBackDdlAndAllSeededRows() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start();
             Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            loadV1(c);
            c.setAutoCommit(false);
            assertThrows(SQLException.class, () -> {
                s.execute("ALTER TABLE application.application_revision ADD COLUMN source_version TEXT NULL");
                s.execute("INSERT INTO application.application_revision(revision_id,application_id,revision,quote_hash,revision_hash,terms_hash) VALUES ('98000000-0000-4000-8000-000000000199','98000000-0000-4000-8000-000000000001',9,repeat('9',64),repeat('8',64),repeat('7',64))");
                throw new SQLException("injected CAP98 failure before commit");
            });
            c.rollback();
            c.setAutoCommit(true);
            assertFalse(hasColumn(c, "source_version"));
            assertEquals(3, scalarInt(s, "SELECT count(*) FROM application.application_revision"));
            assertEquals(3, scalarInt(s, "SELECT count(*) FROM payments.payment_reference p JOIN application.application_revision r USING(application_id,revision)"));
            assertEquals(0, scalarInt(s, "SELECT count(*) FROM application.application_revision WHERE revision=9"));
            assertEquals(3, legacyRows(c).size());
        }
    }

    @Test void destructiveDownIsRejectedWhenVersionTwoValuesWouldBeLost() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start();
             Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            loadV1(c);
            s.execute("ALTER TABLE application.application_revision ADD COLUMN source_version TEXT NULL");
            s.execute("UPDATE application.application_revision SET source_version='source-v2' WHERE revision_id='98000000-0000-4000-8000-000000000102'");
            assertThrows(DestructiveDownRejected.class, () -> guardedDown(c),
                    "a down migration must inspect loss before issuing DROP COLUMN");
            assertTrue(hasColumn(c, "source_version"));
            assertEquals(1, scalarInt(s, "SELECT count(*) FROM application.application_revision WHERE source_version='source-v2'"));
            assertEquals(3, scalarInt(s, "SELECT count(*) FROM application.application_revision"));
            assertEquals(3, scalarInt(s, "SELECT count(*) FROM payments.payment_reference p JOIN application.application_revision r USING(application_id,revision)"));
        }
    }

    @Test void destructiveDownMutantErasesTheV2CanaryAndTheOracleRejectsItsOutcome() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start();
             Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            loadV1(c);
            s.execute("ALTER TABLE application.application_revision ADD COLUMN source_version TEXT NULL");
            s.execute("UPDATE application.application_revision SET source_version='source-v2' WHERE revision_id='98000000-0000-4000-8000-000000000102'");
            s.execute("ALTER TABLE application.application_revision DROP COLUMN source_version"); // deliberate unsafe mutant
            assertEquals(3, scalarInt(s, "SELECT count(*) FROM application.application_revision"));
            assertEquals(3, scalarInt(s, "SELECT count(*) FROM payments.payment_reference p JOIN application.application_revision r USING(application_id,revision)"));
            assertFalse(hasColumn(c, "source_version"));
            boolean expectedV2DataRetained = hasColumn(c, "source_version")
                    && scalarInt(s, "SELECT count(*) FROM application.application_revision WHERE source_version='source-v2'") == 1;
            assertFalse(expectedV2DataRetained,
                    "the independent oracle rejects this down result even though row and FK counts still match");
            assertThrows(SQLException.class, () -> s.executeQuery("SELECT source_version FROM application.application_revision"),
                    "the required source-version evidence is gone even though table counts remain unchanged");
        }
    }

    @Test void isolatedRestoredSchemaMatchesCountsForeignKeysAndImmutableQuoteAndRevisionHashes() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start();
             Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            loadV1(c);
            s.execute("ALTER TABLE application.application_revision ADD COLUMN source_version TEXT NULL");
            s.execute("UPDATE application.application_revision SET source_version='source-v2' WHERE revision_id='98000000-0000-4000-8000-000000000102'");
            List<RevisionSnapshot> revisions = captureRevisions(c);
            List<PaymentSnapshot> payments = capturePayments(c);
            assertEquals(3, revisions.size());
            assertEquals(3, payments.size());

            // Restore the captured logical snapshot into an isolated namespace. Inserts below
            // use only the captured records, never SELECT from the source tables.
            s.execute("CREATE SCHEMA cap98_restored");
            s.execute("CREATE TABLE cap98_restored.application_revision (revision_id UUID PRIMARY KEY, application_id UUID NOT NULL, revision INTEGER NOT NULL CHECK (revision>0), quote_hash CHAR(64) NOT NULL, revision_hash CHAR(64) NOT NULL, terms_hash CHAR(64) NOT NULL, source_version TEXT NULL, UNIQUE(application_id,revision))");
            s.execute("CREATE TABLE cap98_restored.payment_reference (payment_id UUID PRIMARY KEY, application_id UUID NOT NULL, revision INTEGER NOT NULL)");
            s.execute("ALTER TABLE cap98_restored.payment_reference ADD CONSTRAINT payment_reference_owned_revision_fk FOREIGN KEY(application_id,revision) REFERENCES cap98_restored.application_revision(application_id,revision)");
            try (var ps = c.prepareStatement("INSERT INTO cap98_restored.application_revision(revision_id,application_id,revision,quote_hash,revision_hash,terms_hash,source_version) VALUES (?,?,?,?,?,?,?)")) {
                for (RevisionSnapshot row : revisions) {
                    ps.setObject(1,row.revisionId()); ps.setObject(2,row.applicationId()); ps.setInt(3,row.revision());
                    ps.setString(4,row.quoteHash()); ps.setString(5,row.revisionHash()); ps.setString(6,row.termsHash());
                    ps.setString(7,row.sourceVersion()); ps.addBatch();
                }
                ps.executeBatch();
            }
            try (var ps = c.prepareStatement("INSERT INTO cap98_restored.payment_reference(payment_id,application_id,revision) VALUES (?,?,?)")) {
                for (PaymentSnapshot row : payments) { ps.setObject(1,row.paymentId()); ps.setObject(2,row.applicationId()); ps.setInt(3,row.revision()); ps.addBatch(); }
                ps.executeBatch();
            }

            assertEquals(revisions.size(), scalarInt(s, "SELECT count(*) FROM cap98_restored.application_revision"));
            assertEquals(payments.size(), scalarInt(s, "SELECT count(*) FROM cap98_restored.payment_reference"));
            assertEquals(payments.size(), scalarInt(s, "SELECT count(*) FROM cap98_restored.payment_reference p JOIN cap98_restored.application_revision r USING(application_id,revision)"));
            assertEquals(0, scalarInt(s, "SELECT count(*) FROM cap98_restored.payment_reference p LEFT JOIN cap98_restored.application_revision r USING(application_id,revision) WHERE r.revision_id IS NULL"), "all restored payment references must resolve");
            assertEquals(legacyRows(c), legacyRows(c, "cap98_restored.application_revision"),
                    "quote/revision hashes and legacy revision fields remain byte-identical");
            assertEquals(1, scalarInt(s, "SELECT count(*) FROM cap98_restored.application_revision WHERE source_version='source-v2'"));
            assertEquals(1, scalarInt(s, "SELECT count(*) FROM information_schema.table_constraints WHERE constraint_schema='cap98_restored' AND table_name='payment_reference' AND constraint_type='FOREIGN KEY'"));
        }
    }

    private static void guardedDown(Connection c) throws Exception {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT count(*) FROM application.application_revision WHERE source_version IS NOT NULL")) {
            rs.next();
            if (rs.getInt(1) > 0) throw new DestructiveDownRejected("source_version contains v2-only data");
        }
        try (Statement s = c.createStatement()) { s.execute("ALTER TABLE application.application_revision DROP COLUMN source_version"); }
    }

    private static boolean hasColumn(Connection c, String name) throws Exception {
        try (var ps = c.prepareStatement("SELECT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema='application' AND table_name='application_revision' AND column_name=?)")) {
            ps.setString(1, name); try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getBoolean(1); }
        }
    }

    private static List<String> legacyRows(Connection c) throws Exception { return legacyRows(c, "application.application_revision"); }
    private static List<String> legacyRows(Connection c, String table) throws Exception {
        List<String> rows = new ArrayList<>();
        String sql = "SELECT application_id,revision_id,revision,quote_hash,revision_hash,terms_hash FROM " + table + " ORDER BY revision_id";
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) rows.add(rs.getString(1) + "|" + rs.getString(2) + "|" + rs.getInt(3) + "|" + rs.getString(4).trim() + "|" + rs.getString(5).trim() + "|" + rs.getString(6).trim());
        }
        return rows;
    }
    private static List<String> fixtureRevisionRows(JsonNode fixture) {
        List<String> rows=new ArrayList<>();
        fixture.path("revisions").forEach(r->rows.add(r.path("application_id").asText()+"|"+r.path("revision_id").asText()+"|"+r.path("revision").asInt()+"|"+r.path("quote_hash").asText()+"|"+r.path("revision_hash").asText()+"|"+r.path("terms_hash").asText()));
        rows.sort(String::compareTo); return rows;
    }
    private static List<String> paymentRows(Connection c) throws Exception {
        List<String> rows=new ArrayList<>();
        try(Statement s=c.createStatement();ResultSet rs=s.executeQuery("SELECT payment_id,application_id,revision FROM payments.payment_reference ORDER BY payment_id")){
            while(rs.next()) rows.add(rs.getString(1)+"|"+rs.getString(2)+"|"+rs.getInt(3));
        }
        return rows;
    }
    private static List<String> fixturePaymentRows(JsonNode fixture) {
        List<String> rows=new ArrayList<>();
        fixture.path("payment_references").forEach(r->rows.add(r.path("payment_id").asText()+"|"+r.path("application_id").asText()+"|"+r.path("revision").asInt()));
        rows.sort(String::compareTo); return rows;
    }

    private static List<RevisionSnapshot> captureRevisions(Connection c) throws Exception {
        List<RevisionSnapshot> rows = new ArrayList<>();
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT revision_id,application_id,revision,quote_hash,revision_hash,terms_hash,source_version FROM application.application_revision ORDER BY revision_id")) {
            while (rs.next()) rows.add(new RevisionSnapshot(rs.getObject(1, java.util.UUID.class), rs.getObject(2, java.util.UUID.class),
                    rs.getInt(3), rs.getString(4).trim(), rs.getString(5).trim(), rs.getString(6).trim(), rs.getString(7)));
        }
        return rows;
    }
    private static List<PaymentSnapshot> capturePayments(Connection c) throws Exception {
        List<PaymentSnapshot> rows = new ArrayList<>();
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT payment_id,application_id,revision FROM payments.payment_reference ORDER BY payment_id")) {
            while (rs.next()) rows.add(new PaymentSnapshot(rs.getObject(1, java.util.UUID.class), rs.getObject(2, java.util.UUID.class), rs.getInt(3)));
        }
        return rows;
    }
    private record RevisionSnapshot(java.util.UUID revisionId, java.util.UUID applicationId, int revision,
                                    String quoteHash, String revisionHash, String termsHash, String sourceVersion) { }
    private record PaymentSnapshot(java.util.UUID paymentId, java.util.UUID applicationId, int revision) { }

    private static void loadV1(Connection c) throws Exception {
        try (Statement s = c.createStatement()) {
            for (String sql : Files.readString(V1_SQL).split(";")) if (!sql.isBlank()) s.execute(sql);
        }
    }
    private static int scalarInt(Statement s, String sql) throws Exception {
        try (ResultSet rs = s.executeQuery(sql)) { rs.next(); return rs.getInt(1); }
    }
    private static JsonNode fixture() throws Exception { return JSON.readTree(Files.readString(FIXTURE)); }
    private static final class DestructiveDownRejected extends RuntimeException {
        DestructiveDownRejected(String message) { super(message); }
    }
}
