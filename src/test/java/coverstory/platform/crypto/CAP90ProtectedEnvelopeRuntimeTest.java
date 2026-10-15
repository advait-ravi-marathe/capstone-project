package coverstory.platform.crypto;

import coverstory.modules.application.persistence.PostgresProtectedPayloadEnvelopeStore;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.crypto.KeyGenerator;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Actual PostgreSQL persistence test for the instructor-owned ciphertext/revision adapter. */
class CAP90ProtectedEnvelopeRuntimeTest {
    private static final UUID APP = UUID.fromString("90000000-0000-4000-8000-000000000001");
    private static final UUID OWNER = UUID.fromString("90000000-0000-4000-8000-000000000002");
    private static final UUID FOREIGN = UUID.fromString("90000000-0000-4000-8000-000000000003");
    private static final String CANARY = "CAP90-SYNTHETIC-PRIVATE-MARKER-DO-NOT-LOG";

    @Test void encryptedEnvelopePersistsByOwnerAndExactApplicationRevision() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            String url; String user;
            try (Connection c = postgres.getPostgresDatabase().getConnection()) { url = c.getMetaData().getURL(); user = c.getMetaData().getUserName(); }
            DriverManagerDataSource ds = new DriverManagerDataSource(url, user, "");
            createSchemas(ds);
            apply(ds, "src/main/resources/db/schema/constraints.sql");
            apply(ds, "src/main/resources/db/schema/cap78_purchase.sql");
            apply(ds, "src/main/resources/db/schema/cap90_protected_payload.sql");
            insertApplicationAndRevision(ds);

            EncryptionKey key = key("synthetic-v1");
            EncryptionKeyProvider keyProvider = new SingleKey(ignored -> key);
            FieldCipher cipher = new AesGcmFieldCipher(keyProvider);
            var metadata = new CAP90ProtectedPayloadSeam.Metadata(OWNER + ":" + APP, 3, "APPLICATION_ANSWERS", 1);
            EncryptedField encrypted = cipher.encrypt(CANARY, CAP90ProtectedPayloadSeam.associatedData(metadata));
            var store = new PostgresProtectedPayloadEnvelopeStore(ds);
            store.save(new PostgresProtectedPayloadEnvelopeStore.Stored(UUID.randomUUID(), APP, OWNER, 3,
                    "APPLICATION_ANSWERS", 1, encrypted));

            assertEquals(CANARY, cipher.decrypt(store.find(OWNER, APP, 3, "APPLICATION_ANSWERS").orElseThrow().encrypted(),
                    CAP90ProtectedPayloadSeam.associatedData(metadata)));
            assertTrue(store.find(FOREIGN, APP, 3, "APPLICATION_ANSWERS").isEmpty(), "foreign owner cannot retrieve ciphertext via this adapter");
            assertTrue(store.find(OWNER, APP, 2, "APPLICATION_ANSWERS").isEmpty(), "stale revision does not resolve envelope");
            assertEquals(1, rawEnvelopeCount(ds));
            assertFalse(databaseText(ds).contains(CANARY), "database stores only ciphertext and non-sensitive revision metadata");
            assertEquals(java.util.Set.of("envelope_id", "application_id", "owner_id", "application_revision", "payload_kind",
                            "schema_version", "cipher_format_version", "key_id", "nonce", "ciphertext", "created_at"), envelopeColumns(ds));

            assertThrows(SQLException.class, () -> store.save(new PostgresProtectedPayloadEnvelopeStore.Stored(
                    UUID.randomUUID(), APP, OWNER, 9, "APPLICATION_ANSWERS", 1, encrypted)), "orphan revision is rejected by FK");
            assertThrows(SQLException.class, () -> mutateEnvelope(ds), "envelope rows are append-only");
        }
    }

    @Test void capturedCiphertextDoesNotDecryptUnderOtherRevisionAad() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            String url; String user;
            try (Connection c = postgres.getPostgresDatabase().getConnection()) { url = c.getMetaData().getURL(); user = c.getMetaData().getUserName(); }
            DriverManagerDataSource ds = new DriverManagerDataSource(url, user, "");
            createSchemas(ds);
            apply(ds, "src/main/resources/db/schema/constraints.sql"); apply(ds, "src/main/resources/db/schema/cap78_purchase.sql");
            apply(ds, "src/main/resources/db/schema/cap90_protected_payload.sql"); insertApplicationAndRevision(ds);
            EncryptionKey key = key("synthetic-v1"); FieldCipher cipher = new AesGcmFieldCipher(new SingleKey(provider -> key));
            var current = new CAP90ProtectedPayloadSeam.Metadata(OWNER + ":" + APP, 3, "APPLICATION_ANSWERS", 1);
            EncryptedField encrypted = cipher.encrypt(CANARY, CAP90ProtectedPayloadSeam.associatedData(current));
            var store = new PostgresProtectedPayloadEnvelopeStore(ds);
            store.save(new PostgresProtectedPayloadEnvelopeStore.Stored(UUID.randomUUID(), APP, OWNER, 3,
                    "APPLICATION_ANSWERS", 1, encrypted));
            var stale = new CAP90ProtectedPayloadSeam.Metadata(OWNER + ":" + APP, 2, "APPLICATION_ANSWERS", 1);
            assertThrows(java.security.GeneralSecurityException.class,
                    () -> cipher.decrypt(store.find(OWNER, APP, 3, "APPLICATION_ANSWERS").orElseThrow().encrypted(),
                            CAP90ProtectedPayloadSeam.associatedData(stale)));
        }
    }

    private static void insertApplicationAndRevision(DriverManagerDataSource ds) throws Exception {
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            s.execute("INSERT INTO application.application_record(application_id,owner_id,revision,quote_id,status) VALUES ('"+APP+"','"+OWNER+"',3,'90000000-0000-4000-8000-000000000004','ONBOARDING')");
            s.execute("INSERT INTO application.application_revision(application_id,owner_id,revision,quote_id) VALUES ('"+APP+"','"+OWNER+"',3,'90000000-0000-4000-8000-000000000004')");
        }
    }
    private static void apply(DriverManagerDataSource ds, String path) throws Exception {
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) { s.execute(Files.readString(Path.of(path))); }
    }
    private static void createSchemas(DriverManagerDataSource ds) throws Exception {
        try (Connection c=ds.getConnection(); Statement s=c.createStatement()) {
            for (String schema : new String[]{"catalog", "shortlist", "application", "payments", "policy", "support", "business_audit", "analytics"})
                s.execute("CREATE SCHEMA " + schema);
        }
    }
    private static void mutateEnvelope(DriverManagerDataSource ds) throws Exception {
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) { s.execute("UPDATE application.protected_payload_envelope SET key_id='changed' "); }
    }
    private static int rawEnvelopeCount(DriverManagerDataSource ds) throws Exception {
        try (Connection c=ds.getConnection(); Statement s=c.createStatement(); var r=s.executeQuery("SELECT count(*) FROM application.protected_payload_envelope")) { r.next(); return r.getInt(1); }
    }
    private static String databaseText(DriverManagerDataSource ds) throws Exception {
        try (Connection c=ds.getConnection(); Statement s=c.createStatement(); var r=s.executeQuery("""
                SELECT concat_ws('|', envelope_id::text, application_id::text, owner_id::text, application_revision::text,
                    payload_kind, schema_version::text, cipher_format_version::text, key_id, encode(nonce,'hex'), encode(ciphertext,'hex'))
                  FROM application.protected_payload_envelope
                """)) { assertTrue(r.next()); return r.getString(1); }
    }
    private static java.util.Set<String> envelopeColumns(DriverManagerDataSource ds) throws Exception {
        try (Connection c=ds.getConnection(); Statement s=c.createStatement(); var r=s.executeQuery("""
                SELECT column_name FROM information_schema.columns
                 WHERE table_schema='application' AND table_name='protected_payload_envelope'
                """)) {
            java.util.Set<String> columns = new java.util.HashSet<>();
            while (r.next()) columns.add(r.getString(1));
            return columns;
        }
    }
    private static EncryptionKey key(String id) throws Exception { KeyGenerator g=KeyGenerator.getInstance("AES"); g.init(256); return new EncryptionKey(id,g.generateKey()); }
    private static final class SingleKey implements EncryptionKeyProvider {
        private final EncryptionKey key; private SingleKey(java.util.function.Function<Object, EncryptionKey> source) { this.key=source.apply(null); }
        @Override public EncryptionKey activeKey(){return key;} @Override public Optional<EncryptionKey> findById(String id){return key.keyId().equals(id)?Optional.of(key):Optional.empty();}
    }
}
