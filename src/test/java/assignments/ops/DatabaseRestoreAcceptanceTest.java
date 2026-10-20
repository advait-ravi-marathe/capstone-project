package assignments.ops;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Instructor-owned real custom-archive restore proof; the target starts empty and is run-owned. */
class DatabaseRestoreAcceptanceTest {
    @Test void day130RealPgDumpRestoresFullSyntheticRecoveryStateIntoFreshDatabase() throws Exception {
        assertTrue(Postgres18ToolHarness.toolVersion("pg_dump").contains("18.6"));
        assertTrue(Postgres18ToolHarness.toolVersion("pg_restore").contains("18.6"));
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start()) {
            DataSource source = pg.getPostgresDatabase();
            Flyway.configure().dataSource(source).locations("classpath:db/migration").load().migrate();
            seedRecoveryStates(new JdbcTemplate(source));
            String sourceUrl = url(pg, "postgres");
            String before = fullDatabaseSnapshot(sourceUrl);
            Path archive = Postgres18ToolHarness.dump(pg);
            String target = null;
            try {
                target = Postgres18ToolHarness.createFreshTarget(pg);
                String dumpVersion = Postgres18ToolHarness.toolVersion("pg_dump");
                String restoreVersion = Postgres18ToolHarness.toolVersion("pg_restore");
                String serverVersion = scalar(sourceUrl, "SELECT current_setting('server_version')");
                System.out.println("LOCAL_RESTORE_EVIDENCE source_db=postgres target_db=" + target +
                        " server_version=" + serverVersion + " pg_dump=\"" + dumpVersion + "\" pg_restore=\"" + restoreVersion +
                        "\" archive_size_bytes=" + Files.size(archive) + " archive_sha256=" + sha256(archive));
                assertNotEquals("postgres", target);
                assertEquals(0, tableCount(url(pg, target)), "restore destination must be empty before pg_restore");
                Postgres18ToolHarness.restore(pg, archive, target);
                String restored = fullDatabaseSnapshot(url(pg, target));
                assertEquals(before, restored, "full ordered row payloads/timestamps, schema and migration history survive logical restore");
                try (Connection c = DriverManager.getConnection(url(pg, target), "postgres", ""); Statement s = c.createStatement()) {
                    s.executeUpdate("UPDATE training_issuance_delivery_receipt SET received_at=received_at + INTERVAL '1 second' WHERE policy_id='restore-complete'");
                }
                assertEquals(3, scalarInt(url(pg, target), "SELECT count(*) FROM training_issuance_delivery_receipt"));
                assertNotEquals(restored, fullDatabaseSnapshot(url(pg, target)),
                        "a payload/timestamp mutation must fail the full oracle even while row counts remain unchanged");
                assertEquals(before, fullDatabaseSnapshot(sourceUrl), "dump/restore must not mutate source");
            } finally {
                if (target != null) Postgres18ToolHarness.dropOwnedTarget(pg, target);
                Files.deleteIfExists(archive);
            }
        }
    }

    @Test void day131CorruptArchiveFailsWithoutPartialTargetData() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start()) {
            DataSource source = pg.getPostgresDatabase();
            Flyway.configure().dataSource(source).locations("classpath:db/migration").load().migrate();
            seedRecoveryStates(new JdbcTemplate(source));
            String sourceBefore = fullDatabaseSnapshot(url(pg, "postgres"));
            String target = Postgres18ToolHarness.createFreshTarget(pg);
            Path corrupt = Files.createTempFile("coverstory-corrupt-", ".dump");
            try {
                Files.write(corrupt, "not-a-postgresql-custom-archive".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
                assertEquals(0, tableCount(url(pg, target)));
                Postgres18ToolHarness.Result result = Postgres18ToolHarness.restoreResult(pg, corrupt, target);
                assertNotEquals(0, result.exitCode(), "corrupt archive must fail, not be treated as a restore");
                assertEquals(0, tableCount(url(pg, target)), "single-transaction restore leaves no partial application schema");
                assertEquals(sourceBefore, fullDatabaseSnapshot(url(pg, "postgres")), "failed restore must not touch source");
            } finally {
                Postgres18ToolHarness.dropOwnedTarget(pg, target);
                Files.deleteIfExists(corrupt);
            }
        }
    }

    private static void seedRecoveryStates(JdbcTemplate jdbc) {
        jdbc.update("INSERT INTO journeys(journey_id) VALUES ('restore-journey')");
        jdbc.update("INSERT INTO quote_versions(journey_id,quote_version_id,product_code,product_version,rate_version,rebuild_amount_rupees,coverage_code,currency,annual_premium_rupees,created_at,expires_at) VALUES ('restore-journey','restore-quote','HOME_SECURE','home-secure-v1','training-rate-v1',NULL,'BUILDING','INR',125.00,TIMESTAMPTZ '2026-10-01 09:00:00+00',TIMESTAMPTZ '2026-10-02 09:00:00+00')");
        jdbc.update("INSERT INTO selections(journey_id,quote_version_id,selection_order) VALUES ('restore-journey','restore-quote',0)");
        insertPolicy(jdbc, "restore-pending");
        insertPolicy(jdbc, "restore-receipt-unacked");
        insertPolicy(jdbc, "restore-complete");
        insertPolicy(jdbc, "restore-held");
        insertPolicy(jdbc, "restore-conflict");
        jdbc.update("INSERT INTO training_policy_idempotency(owner_id,client_request_id,request_fingerprint,policy_id) VALUES ('restore-owner','restore-request','sha256:synthetic','restore-complete')");
        jdbc.update("INSERT INTO training_issuance_work(policy_id,source_version,event_type,effective_date,envelope_schema_version,captured_at) VALUES " +
                "('restore-pending',1,'ISSUED',DATE '2026-12-01',1,TIMESTAMPTZ '2026-10-01 10:00:00+00')," +
                "('restore-receipt-unacked',1,'ISSUED',DATE '2026-12-01',1,TIMESTAMPTZ '2026-10-01 10:01:00+00')," +
                "('restore-complete',1,'ISSUED',DATE '2026-12-01',1,TIMESTAMPTZ '2026-10-01 10:02:00+00')," +
                "('restore-held',1,'ISSUED',DATE '2026-12-01',1,TIMESTAMPTZ '2026-10-01 10:03:00+00')," +
                "('restore-conflict',1,'ISSUED',DATE '2026-12-01',1,TIMESTAMPTZ '2026-10-01 10:04:00+00')");
        jdbc.update("INSERT INTO training_issuance_delivery_receipt(policy_id,source_version,event_type,effective_date,envelope_schema_version,received_at) VALUES " +
                "('restore-receipt-unacked',1,'ISSUED',DATE '2026-12-01',1,TIMESTAMPTZ '2026-10-01 11:00:00+00')," +
                "('restore-complete',1,'ISSUED',DATE '2026-12-01',1,TIMESTAMPTZ '2026-10-01 11:01:00+00')," +
                "('restore-conflict',1,'ISSUED',DATE '2026-12-02',1,TIMESTAMPTZ '2026-10-01 11:02:00+00')");
        jdbc.update("UPDATE training_issuance_work SET acknowledged_at=TIMESTAMPTZ '2026-10-01 12:00:00+00' WHERE policy_id='restore-complete'");
        jdbc.update("UPDATE training_issuance_work SET quarantined_at=TIMESTAMPTZ '2026-10-01 12:30:00+00', quarantine_reason='MANUAL_HOLD', control_revision=3 WHERE policy_id='restore-held'");
        jdbc.update("INSERT INTO training_issuance_recovery_history(policy_id,source_version,control_revision,action,reason_code,decided_at) VALUES " +
                "('restore-held',1,1,'QUARANTINE','OBSERVED_FAILURE',TIMESTAMPTZ '2026-10-01 12:10:00+00')," +
                "('restore-held',1,2,'RELEASE','REVIEWED_FOR_RETRY',TIMESTAMPTZ '2026-10-01 12:20:00+00')," +
                "('restore-held',1,3,'QUARANTINE','MANUAL_HOLD',TIMESTAMPTZ '2026-10-01 12:30:00+00')");
    }

    static void insertPolicy(JdbcTemplate jdbc, String id) {
        jdbc.update("INSERT INTO training_policy_lifecycle(policy_id,owner_id,journey_id,quote_version_id,product_version,rate_version,annual_premium_rupees,currency,effective_date,status,version) VALUES (?, 'restore-owner', ?, ?, 'home-secure-v1','training-rate-v1',125.00,'INR',DATE '2026-12-01','ISSUED',1)", id, "journey-" + id, "quote-" + id);
        jdbc.update("INSERT INTO training_policy_lifecycle_event(policy_id,version,event_type,effective_date) VALUES (?,1,'ISSUED',DATE '2026-12-01')", id);
    }

    private static int tableCount(String url) throws Exception {
        try (Connection c = DriverManager.getConnection(url, "postgres", ""); Statement s = c.createStatement(); var rs = s.executeQuery("SELECT count(*) FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace WHERE n.nspname='public' AND c.relkind IN ('r','p','v','m','f')")) {
            assertTrue(rs.next()); return rs.getInt(1);
        }
    }

    static String scalar(String url, String query) throws Exception {
        try (Connection c = DriverManager.getConnection(url, "postgres", ""); Statement s = c.createStatement(); var rs = s.executeQuery(query)) {
            assertTrue(rs.next()); return rs.getString(1);
        }
    }

    private static int scalarInt(String url, String query) throws Exception {
        try (Connection c = DriverManager.getConnection(url, "postgres", ""); Statement s = c.createStatement(); var rs = s.executeQuery(query)) {
            assertTrue(rs.next()); return rs.getInt(1);
        }
    }

    private static String sha256(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    static String url(EmbeddedPostgres pg, String database) {
        return pg.getJdbcUrl("postgres", "postgres").replaceFirst("/[^/?]+(?=\\?|$)", "/" + database);
    }

    static String fullDatabaseSnapshot(String url) throws Exception {
        try (Connection connection = DriverManager.getConnection(url, "postgres", ""); Statement statement = connection.createStatement();
             var result = statement.executeQuery("""
                 SELECT current_setting('server_version')||'|'||
                   (SELECT coalesce(jsonb_agg(jsonb_build_object('table',t.table_name,'rows',t.rows) ORDER BY t.table_name),'[]'::jsonb)::text FROM (
                     SELECT 'journeys' table_name,coalesce(jsonb_agg(to_jsonb(x) ORDER BY journey_id),'[]'::jsonb) rows FROM journeys x
                     UNION ALL SELECT 'quote_versions',coalesce(jsonb_agg(to_jsonb(x) ORDER BY journey_id,quote_version_id),'[]'::jsonb) FROM quote_versions x
                     UNION ALL SELECT 'selections',coalesce(jsonb_agg(to_jsonb(x) ORDER BY journey_id,selection_order),'[]'::jsonb) FROM selections x
                     UNION ALL SELECT 'training_policy_lifecycle',coalesce(jsonb_agg(to_jsonb(x) ORDER BY policy_id),'[]'::jsonb) FROM training_policy_lifecycle x
                     UNION ALL SELECT 'training_policy_lifecycle_event',coalesce(jsonb_agg(to_jsonb(x) ORDER BY policy_id,version),'[]'::jsonb) FROM training_policy_lifecycle_event x
                     UNION ALL SELECT 'training_policy_idempotency',coalesce(jsonb_agg(to_jsonb(x) ORDER BY owner_id,client_request_id),'[]'::jsonb) FROM training_policy_idempotency x
                     UNION ALL SELECT 'training_issuance_work',coalesce(jsonb_agg(to_jsonb(x) ORDER BY policy_id,source_version),'[]'::jsonb) FROM training_issuance_work x
                     UNION ALL SELECT 'training_issuance_delivery_receipt',coalesce(jsonb_agg(to_jsonb(x) ORDER BY policy_id,source_version),'[]'::jsonb) FROM training_issuance_delivery_receipt x
                     UNION ALL SELECT 'training_issuance_recovery_history',coalesce(jsonb_agg(to_jsonb(x) ORDER BY policy_id,source_version,control_revision),'[]'::jsonb) FROM training_issuance_recovery_history x
                     UNION ALL SELECT 'flyway_schema_history',coalesce(jsonb_agg(to_jsonb(x) ORDER BY installed_rank),'[]'::jsonb) FROM flyway_schema_history x
                   ) t)||'|'||
                   (SELECT coalesce(jsonb_agg(jsonb_build_object('table',table_name,'column',column_name,'type',data_type,'nullable',is_nullable,'default',column_default) ORDER BY table_name,ordinal_position),'[]'::jsonb)::text FROM information_schema.columns WHERE table_schema='public')||'|'||
                   (SELECT coalesce(jsonb_agg(jsonb_build_object('table',conrelid::regclass::text,'name',conname,'definition',pg_get_constraintdef(oid)) ORDER BY conrelid::regclass::text,conname),'[]'::jsonb)::text FROM pg_constraint WHERE connamespace='public'::regnamespace)||'|'||
                   (SELECT coalesce(jsonb_agg(jsonb_build_object('table',tablename,'name',indexname,'definition',indexdef) ORDER BY tablename,indexname),'[]'::jsonb)::text FROM pg_indexes WHERE schemaname='public')
                 """)) {
            assertTrue(result.next()); return result.getString(1);
        }
    }
}
