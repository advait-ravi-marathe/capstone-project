package assignments.ops;

import assignments.delivery.recovery.InspectionReport;
import assignments.delivery.recovery.InspectionReportCodec;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Actual two-JAR, same-database matrix. Supply separately built immutable artifacts by property. */
@Tag("learnerAcceptance")
class TwoArtifactCompatibilityAcceptanceTest {
    private static final String POLICY = "policy-demo-1";
    private static final long VERSION = 1;
    private static ReleaseArtifactPair completedEvidence;

    @Test void day146RefusesAbsentSchemaWithoutStartingMigration() throws Exception {
        Path b=artifact("coverstory.release.candidate.jar");
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
            String url=pg.getJdbcUrl("postgres","postgres");
            int before=scalar(url,"SELECT count(*) FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace WHERE n.nspname='public' AND c.relkind IN ('r','p','v','m','f')");
            var args=List.of("--coverstory.release.inspect","--coverstory.release.policy-id=never-migrated","--coverstory.release.source-version=1");
            var result=runRaw(b,url,args);
            assertNotEquals(0,result.exitCode(),"absent schema is a nonzero refusal");
            assertFalse(result.capturedOutput().contains(InspectionReportCodec.PREFIX),"failure emits no success frame");
            assertEquals(before,scalar(url,"SELECT count(*) FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace WHERE n.nspname='public' AND c.relkind IN ('r','p','v','m','f')"),
                    "startup and schema preflight cannot migrate or seed the database");
        }
    }

    @Test void day151RejectsCannedRecommendationAgainstObservedEvidence() throws Exception {
        Path b=artifact("coverstory.release.candidate.jar");
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
            var ds=pg.getPostgresDatabase();Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();
            new JdbcTemplate(ds).update("INSERT INTO training_issuance_work(policy_id,source_version,event_type,effective_date,envelope_schema_version) VALUES (?, ?, 'ISSUED', DATE '2026-12-01', 1)",POLICY,VERSION);
            String url=pg.getJdbcUrl("postgres","postgres");String before=DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            String output=run(b,url,List.of("--coverstory.release.inspect","--coverstory.release.policy-id="+POLICY,"--coverstory.release.source-version="+VERSION));
            var frames=output.lines().filter(line->line.startsWith(InspectionReportCodec.PREFIX)).toList();assertEquals(1,frames.size());
            String falseSuccess=frames.getFirst().replace("ELIGIBLE_FOR_ONE_ATTEMPT","ALREADY_COMPLETE");
            assertNotEquals(frames.getFirst(),falseSuccess,"fixture must exercise a genuine misleading recommendation mutation");
            assertThrows(java.io.IOException.class,()->new assignments.delivery.recovery.InspectionReportCodec(new tools.jackson.databind.ObjectMapper())
                    .decodeSingleFrame(falseSuccess,new InspectionReport.RequestedKey(POLICY,VERSION)));
            assertEquals(before,DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url),"bad report witness cannot alter actual evidence");
        }
    }

    @Test void day148RunsFrozenAAndReportCapableB() throws Exception {
        Path a = artifact("coverstory.release.baseline.jar");
        Path b = artifact("coverstory.release.candidate.jar");
        String aHash = sha(a), bHash = sha(b);
        assertNotEquals(aHash, bHash, "baseline and candidate must be distinct actual JAR bytes");
        String recipe = System.getProperty("coverstory.release.artifact.provenance", "");
        assertTrue(recipe.contains("A=") && recipe.contains("B=") && recipe.contains("witness="),
                "record separate source/build/witness provenance for each artifact");
        String aSource=required("coverstory.release.baseline.source");
        String bSource=required("coverstory.release.candidate.source");
        String aBuild=required("coverstory.release.baseline.build");
        String bBuild=required("coverstory.release.candidate.build");
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start()) {
            var ds = pg.getPostgresDatabase();
            Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();
            JdbcTemplate jdbc = new JdbcTemplate(ds);
            jdbc.update("INSERT INTO training_issuance_work(policy_id,source_version,event_type,effective_date,envelope_schema_version) VALUES (?, ?, 'ISSUED', DATE '2026-12-01', 1)", POLICY, VERSION);
            String url = pg.getJdbcUrl("postgres", "postgres");
            String start = DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            assertReadOnly(a, url, "inspect");
            String afterAInspect = DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            assertEquals(start, afterAInspect);
            assertReadOnly(b, url, "inspect");
            assertEquals(start, DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url));
            String reportOutput = run(b, url, List.of("--coverstory.release.inspect",
                    "--coverstory.release.policy-id=" + POLICY,
                    "--coverstory.release.source-version=" + VERSION));
            var framed = reportOutput.lines().filter(s -> s.startsWith(InspectionReportCodec.PREFIX)).toList();
            assertEquals(1, framed.size(), "B emits exactly one framed report");
            InspectionReport report = new InspectionReportCodec(new ObjectMapper()).decodeSingleFrame(
                    framed.getFirst(), new InspectionReport.RequestedKey(POLICY, VERSION));
            assertEquals("policy-demo-1", report.key().policyId());
            assertEquals(start, DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url),
                    "B report startup and inspection must be read-only");

            String sourceBefore = sourceSnapshot(url);
            String immutableWorkBefore=immutableWorkSnapshot(url);
            run(b, url, action("quarantine", 0, "MANUAL_HOLD"));
            String quarantined=DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            assertTrue(run(a, url, action("inspect", null, null)).contains("MANUAL_HOLD"));
            assertEquals(quarantined,DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url),"A inspection must preserve all rows");
            assertEquals(immutableWorkBefore,immutableWorkSnapshot(url));
            run(a, url, action("release", 1, "REVIEWED_FOR_RETRY"));
            String released = DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            String historyAfterRelease=historySnapshot(url);
            assertEquals(2,scalar(url,"SELECT control_revision FROM training_issuance_work WHERE policy_id='policy-demo-1'"));
            assertEquals(immutableWorkBefore,immutableWorkSnapshot(url));
            run(b, url, action("retry", 2, null));
            String completed = DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            String receiptAfterDelivery=receiptSnapshot(url);
            assertEquals(sourceBefore, sourceSnapshot(url), "source/lifecycle/idempotency data is retained");
            assertEquals(1, scalar(url, "SELECT count(*) FROM training_issuance_delivery_receipt WHERE policy_id='policy-demo-1'"));
            assertEquals(2, scalar(url, "SELECT count(*) FROM training_issuance_recovery_history WHERE policy_id='policy-demo-1'"));
            assertTrue(released.contains("REVIEWED_FOR_RETRY"));
            assertEquals(1, scalar(url, "SELECT count(*) FROM training_issuance_work WHERE policy_id='policy-demo-1' AND acknowledged_at IS NOT NULL"));
            assertEquals(immutableWorkBefore,immutableWorkSnapshot(url),"delivery/ack may change only ack and receipt columns");
            assertEquals(historyAfterRelease,historySnapshot(url),"retry cannot rewrite recovery history");
            assertNotEquals(released, completed, "receipt then acknowledgment are visible persisted changes");
            String beforeAReplay = DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            run(a, url, action("inspect", null, null));
            assertEquals(beforeAReplay, DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url));
            run(a, url, action("retry", 2, null));
            assertEquals(beforeAReplay, DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url),
                    "A replays retained completion without replacing receipt or timestamps");
            assertEquals(receiptAfterDelivery,receiptSnapshot(url),"replay preserves original receipt payload/time");
            assertTrue(sha(a).equals(aHash) && sha(b).equals(bHash), "artifact bytes remain immutable during the matrix");
            String schemaFingerprint=migrationFingerprint();
            completedEvidence=new ReleaseArtifactPair(
                    new ReleaseArtifactPair.ArtifactIdentity(aHash,aSource,"legacy-inspect",aBuild,true),
                    new ReleaseArtifactPair.ArtifactIdentity(bHash,bSource,"legacy-inspect+report-v1",bBuild,true),
                    "embedded-postgres:"+url.substring(url.lastIndexOf('/')+1),schemaFingerprint,true,true,true,true);
        }
        new ReleaseArtifactPairPolicy().validate(completedEvidence);
        assertEquals(BinaryFallbackPolicy.FallbackDecision.ALLOW_TESTED_PAIR,
                new BinaryFallbackPolicy().decide(completedEvidence,aHash,bHash,completedEvidence.fixtureIdentity(),completedEvidence.schemaFingerprint()),
                "actual matrix and retained-state evidence, not supplied booleans alone, is required before fallback");
        System.out.println("TWO_ARTIFACT_MATRIX_PASSED A="+aHash+" B="+bHash+" sourceA="+aSource+" sourceB="+bSource+
                " buildA="+aBuild+" buildB="+bBuild+" provenance="+recipe+
                " fixture="+completedEvidence.fixtureIdentity()+" schema="+completedEvidence.schemaFingerprint());
    }

    private static Path artifact(String property) {
        String value = System.getProperty(property, "");
        assertFalse(value.isBlank(), "missing separately built artifact property " + property);
        Path path = Path.of(value).toAbsolutePath();
        assertTrue(Files.isRegularFile(path), "artifact must exist: " + property);
        return path;
    }

    private static List<String> action(String action, Integer revision, String reason) {
        var args = new java.util.ArrayList<>(List.of("--coverstory.recovery.action=" + action,
                "--coverstory.recovery.policy-id=" + POLICY, "--coverstory.recovery.source-version=" + VERSION));
        if (revision != null) args.add("--coverstory.recovery.revision=" + revision);
        if (reason != null) args.add("--coverstory.recovery.reason=" + reason);
        return args;
    }

    private static void assertReadOnly(Path jar, String url, String action) throws Exception {
        String out = run(jar, url, action(action, null, null));
        assertFalse(out.contains("release inspection failed"));
    }

    private static String run(Path jar, String url, List<String> args) throws Exception {
        var result=runRaw(jar,url,args);
        assertFalse(result.timedOut(), "actual child command exceeded watchdog");
        assertFalse(result.outputOverflow(), "actual child command exceeded one MiB bound");
        assertEquals(0, result.exitCode(), "actual child command failed: " + result.capturedOutput());
        return result.capturedOutput();
    }
    private static BoundedProcessRunner.Result runRaw(Path jar,String url,List<String> args) throws Exception {
        var cmd = new java.util.ArrayList<String>();
        cmd.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        cmd.add("-jar"); cmd.add(jar.toString()); cmd.addAll(args);
        var env=java.util.Map.of("SPRING_PROFILES_ACTIVE","local","COVERSTORY_DB_URL",url,
                "COVERSTORY_DB_USER","postgres","COVERSTORY_DB_PASSWORD","");
        return new BoundedProcessRunner().run(cmd,env,java.time.Duration.ofSeconds(30), 1024 * 1024);
    }

    private static String sourceSnapshot(String url) throws Exception {
        try (Connection c=DriverManager.getConnection(url,"postgres",""); Statement s=c.createStatement();
             ResultSet r=s.executeQuery("SELECT coalesce((SELECT jsonb_agg(to_jsonb(x) ORDER BY policy_id)::text FROM training_policy_lifecycle x),'[]')||'|'||coalesce((SELECT jsonb_agg(to_jsonb(x) ORDER BY policy_id,version)::text FROM training_policy_lifecycle_event x),'[]')||'|'||coalesce((SELECT jsonb_agg(to_jsonb(x) ORDER BY owner_id,client_request_id)::text FROM training_policy_idempotency x),'[]')")) {
            assertTrue(r.next()); return r.getString(1);
        }
    }
    private static String immutableWorkSnapshot(String url) throws Exception {
        try(Connection c=DriverManager.getConnection(url,"postgres",""); Statement s=c.createStatement(); ResultSet r=s.executeQuery(
                "SELECT coalesce(jsonb_agg((to_jsonb(w)-'quarantined_at'-'quarantine_reason'-'control_revision'-'acknowledged_at') ORDER BY policy_id,source_version),'[]'::jsonb)::text FROM training_issuance_work w")) {
            assertTrue(r.next()); return r.getString(1);
        }
    }
    private static String historySnapshot(String url) throws Exception {
        try(Connection c=DriverManager.getConnection(url,"postgres",""); Statement s=c.createStatement(); ResultSet r=s.executeQuery(
                "SELECT coalesce(jsonb_agg(to_jsonb(h) ORDER BY policy_id,source_version,control_revision),'[]'::jsonb)::text FROM training_issuance_recovery_history h")) {
            assertTrue(r.next()); return r.getString(1);
        }
    }
    private static String receiptSnapshot(String url) throws Exception {
        try(Connection c=DriverManager.getConnection(url,"postgres",""); Statement s=c.createStatement(); ResultSet r=s.executeQuery(
                "SELECT coalesce(jsonb_agg(to_jsonb(x) ORDER BY policy_id,source_version),'[]'::jsonb)::text FROM training_issuance_delivery_receipt x")) {
            assertTrue(r.next()); return r.getString(1);
        }
    }
    private static int scalar(String url, String sql) throws Exception {
        try(Connection c=DriverManager.getConnection(url,"postgres",""); Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)) {
            assertTrue(r.next()); return r.getInt(1);
        }
    }
    private static String sha(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }
    private static String required(String name) { String value=System.getProperty(name,""); assertFalse(value.isBlank(),"missing provenance property "+name); return value; }
    private static String migrationFingerprint() throws Exception {
        MessageDigest digest=MessageDigest.getInstance("SHA-256");
        try(var files=Files.list(Path.of("src/main/resources/db/migration"))) {
            for(Path path:files.filter(p->p.getFileName().toString().matches("V00[1-5].*\\.sql")).sorted().toList()) {
                digest.update(path.getFileName().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                digest.update(Files.readAllBytes(path));
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
