package assignments.ops;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Executable-JAR inspection after archive restore; uses the actual recovery-only CLI path. */
@Tag("learnerAcceptance")
class LocalRestoreApplicationAcceptanceTest {
    @Test void day132RequiresRestoredDataAndActualApplicationEvidence() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start()) {
            var source = pg.getPostgresDatabase();
            Flyway.configure().dataSource(source).locations("classpath:db/migration").load().migrate();
            JdbcTemplate jdbc = new JdbcTemplate(source);
            DatabaseRestoreAcceptanceTest.insertPolicy(jdbc, "jar-restore-held");
            jdbc.update("INSERT INTO training_issuance_work(policy_id,source_version,event_type,effective_date,envelope_schema_version,quarantined_at,quarantine_reason,control_revision) VALUES ('jar-restore-held',1,'ISSUED',DATE '2026-12-01',1,TIMESTAMPTZ '2026-10-01 12:30:00+00','MANUAL_HOLD',1)");
            jdbc.update("INSERT INTO training_issuance_recovery_history(policy_id,source_version,control_revision,action,reason_code,decided_at) VALUES ('jar-restore-held',1,1,'QUARANTINE','MANUAL_HOLD',TIMESTAMPTZ '2026-10-01 12:30:00+00')");
            String sourceUrl = DatabaseRestoreAcceptanceTest.url(pg, "postgres");
            String before = DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(sourceUrl);
            Path archive = Postgres18ToolHarness.dump(pg);
            String target = null;
            Process app = null;
            Path log = Files.createTempFile("coverstory-restored-app-", ".log");
            try {
                target = Postgres18ToolHarness.createFreshTarget(pg);
                Postgres18ToolHarness.restore(pg, archive, target);
                String targetUrl = DatabaseRestoreAcceptanceTest.url(pg, target);
                String restoredBeforeApp = DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(targetUrl);
                assertEquals(before, restoredBeforeApp, "restore gate requires full row/schema/migration equality");
                Path jar = Path.of("target/capstone-starter-1.0-SNAPSHOT.jar").toAbsolutePath();
                assertTrue(Files.isRegularFile(jar), "package the actual local JAR before this selector");
                String jarSha = sha256(Files.readAllBytes(jar));
                String serverVersion = DatabaseRestoreAcceptanceTest.scalar(sourceUrl, "SELECT current_setting('server_version')");
                String dumpVersion = Postgres18ToolHarness.toolVersion("pg_dump");
                Map<String, String> migrationDigests = migrationDigests();
                String sourceSha = gitHeadSha();
                var manifest = new ReleaseManifestPolicy.ReleaseManifest(sourceSha, jarSha,
                        System.getProperty("java.version"), serverVersion, dumpVersion, migrationDigests, false);
                System.out.println("RESTORED_APPLICATION_EVIDENCE source_sha=" + sourceSha + " jar_sha256=" + jarSha +
                        " java=" + System.getProperty("java.version") + " postgres=" + serverVersion +
                        " pg_dump=\"" + dumpVersion + "\" migration_sha256=" + migrationDigests + " target_db=" + target);
                List<String> command = List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                        "-jar", jar.toString(), "--spring.profiles.active=local", "--spring.datasource.url=" + targetUrl,
                        "--spring.datasource.username=postgres", "--spring.datasource.password=",
                        "--coverstory.recovery.action=inspect", "--coverstory.recovery.policy-id=jar-restore-held",
                        "--coverstory.recovery.source-version=1");
                app = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
                assertTrue(app.waitFor(30, TimeUnit.SECONDS), "packaged app watchdog; log=" + Files.readString(log));
                assertEquals(0, app.exitValue(), Files.readString(log));
                assertTrue(Files.readString(log).contains("MANUAL_HOLD"), "actual CLI must observe restored quarantine state");
                new ReleaseManifestPolicy().validate(manifest, jarSha, migrationDigests);
                new RehearsalEvidencePolicy().requireActivationEvidence(
                        new RehearsalEvidencePolicy.PhaseEvidence(true, true, true, true, before, restoredBeforeApp));
                assertEquals(restoredBeforeApp, DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(targetUrl),
                        "read-only inspection must not change restored state");
                assertEquals(before, DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(sourceUrl), "source remains unchanged");
            } finally {
                if (app != null && app.isAlive()) {
                    app.destroyForcibly();
                    assertTrue(app.waitFor(5, TimeUnit.SECONDS), "owned packaged app must terminate");
                }
                Files.deleteIfExists(log);
                Files.deleteIfExists(archive);
                if (target != null) Postgres18ToolHarness.dropOwnedTarget(pg, target);
            }
        }
    }

    private static String sha256(byte[] content) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
    }

    private static Map<String, String> migrationDigests() throws Exception {
        Path migrations = Path.of("src/main/resources/db/migration");
        var result = new TreeMap<String, String>();
        try (var files = Files.list(migrations)) {
            for (Path migration : files.filter(p -> p.getFileName().toString().matches("V00[1-5].*\\.sql"))
                    .sorted().toList()) {
                result.put(migration.getFileName().toString(), sha256(Files.readAllBytes(migration)));
            }
        }
        if (result.size() != 5) throw new IllegalStateException("expected actual V001–V005 migration files");
        return Map.copyOf(result);
    }

    private static String gitHeadSha() throws Exception {
        Process process = new ProcessBuilder("git", "rev-parse", "HEAD").redirectErrorStream(true).start();
        try {
            assertTrue(process.waitFor(5, TimeUnit.SECONDS), "git SHA lookup watchdog");
            assertEquals(0, process.exitValue(), "git SHA lookup must succeed");
            String sha = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
            assertTrue(sha.matches("[0-9a-f]{40}"), "release source identity must be a full commit SHA");
            return sha;
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                assertTrue(process.waitFor(5, TimeUnit.SECONDS), "git SHA process must terminate");
            }
        }
    }
}
