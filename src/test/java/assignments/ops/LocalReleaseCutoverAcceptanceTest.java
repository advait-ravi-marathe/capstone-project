package assignments.ops;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Uses the same packaged JAR against source and restored target under a local test-client switch. */
@Tag("learnerAcceptance")
class LocalReleaseCutoverAcceptanceTest {
    @Test void day134FailedCandidateCannotDisplaceKnownGoodAndDay135RollbackRespectsDivergence() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start()) {
            var source = pg.getPostgresDatabase();
            Flyway.configure().dataSource(source).locations("classpath:db/migration").load().migrate();
            JdbcTemplate sourceJdbc = new JdbcTemplate(source);
            DatabaseRestoreAcceptanceTest.insertPolicy(sourceJdbc, "cutover-held");
            sourceJdbc.update("INSERT INTO training_issuance_work(policy_id,source_version,event_type,effective_date,envelope_schema_version,quarantined_at,quarantine_reason,control_revision) VALUES ('cutover-held',1,'ISSUED',DATE '2026-12-01',1,TIMESTAMPTZ '2026-10-01 12:30:00+00','MANUAL_HOLD',1)");
            sourceJdbc.update("INSERT INTO training_issuance_recovery_history(policy_id,source_version,control_revision,action,reason_code,decided_at) VALUES ('cutover-held',1,1,'QUARANTINE','MANUAL_HOLD',TIMESTAMPTZ '2026-10-01 12:30:00+00')");
            String sourceUrl = DatabaseRestoreAcceptanceTest.url(pg, "postgres");
            String sourceState = DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(sourceUrl);
            Path archive = Postgres18ToolHarness.dump(pg);
            String target = null;
            try {
                target = Postgres18ToolHarness.createFreshTarget(pg);
                Postgres18ToolHarness.restore(pg, archive, target);
                String targetUrl = DatabaseRestoreAcceptanceTest.url(pg, target);
                String targetState = DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(targetUrl);
                assertEquals(sourceState, targetState, "candidate starts at the exact source recovery point");
                Path jar = Path.of("target/capstone-starter-1.0-SNAPSHOT.jar").toAbsolutePath();
                assertTrue(Files.isRegularFile(jar), "build the actual local JAR before the cutover selector");

                ActivationDecisionPolicy activation = new ActivationDecisionPolicy();
                ActivationDecisionPolicy.Selection selected = ActivationDecisionPolicy.Selection.CURRENT;
                ActivationDecisionPolicy.Selection failed = activation.choose(true, true, true, false, true);
                assertEquals(ActivationDecisionPolicy.Selection.CURRENT, failed, "failed business gate must leave A selected");
                assertEquals(sourceState, DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(sourceUrl));
                assertEquals(targetState, DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(targetUrl));
                assertTrue(inspect(jar, sourceUrl).contains("MANUAL_HOLD"), "known-good A remains usable after candidate rejection");

                selected = activation.choose(true, true, true, true, true);
                assertEquals(ActivationDecisionPolicy.Selection.CANDIDATE, selected);
                assertTrue(inspect(jar, targetUrl).contains("MANUAL_HOLD"), "same packaged JAR observes the restored target state");
                assertEquals(sourceState, DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(sourceUrl), "candidate inspection does not mutate S");
                assertEquals(targetState, DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(targetUrl), "candidate inspection does not mutate T");

                RollbackDecisionPolicy rollback = new RollbackDecisionPolicy();
                assertTrue(rollback.mayReturnToSource(true, true, true), "quiesced same-artifact configuration return is allowed");
                selected = ActivationDecisionPolicy.Selection.CURRENT;
                assertEquals(ActivationDecisionPolicy.Selection.CURRENT, selected);
                assertTrue(inspect(jar, sourceUrl).contains("MANUAL_HOLD"), "source remains available for quiesced return");

                selected = activation.choose(true, true, true, true, true);
                assertEquals(ActivationDecisionPolicy.Selection.CANDIDATE, selected);
                var targetDataSource = new DriverManagerDataSource(targetUrl, "postgres", "");
                DatabaseRestoreAcceptanceTest.insertPolicy(new JdbcTemplate(targetDataSource), "postcutover-write");
                String divergedTarget = DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(targetUrl);
                assertNotEquals(sourceState, divergedTarget, "accepted candidate write establishes divergence");
                assertFalse(rollback.mayReturnToSource(true, true, false), "stale source rollback is blocked after a T write");
                assertEquals(ActivationDecisionPolicy.Selection.CANDIDATE, selected, "blocked rollback leaves candidate selected");
                assertEquals(sourceState, DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(sourceUrl), "S is never modified by the rehearsal");
                assertEquals(divergedTarget, DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(targetUrl), "T retains the accepted post-cutover write");
            } finally {
                if (target != null) Postgres18ToolHarness.dropOwnedTarget(pg, target);
                Files.deleteIfExists(archive);
            }
        }
    }

    private static String inspect(Path jar, String url) throws Exception {
        Path log = Files.createTempFile("coverstory-cutover-app-", ".log");
        Process process = null;
        try {
            List<String> command = List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                    "-jar", jar.toString(), "--spring.profiles.active=local", "--spring.datasource.url=" + url,
                    "--spring.datasource.username=postgres", "--spring.datasource.password=",
                    "--coverstory.recovery.action=inspect", "--coverstory.recovery.policy-id=cutover-held",
                    "--coverstory.recovery.source-version=1");
            process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "packaged app watchdog; log=" + Files.readString(log));
            assertEquals(0, process.exitValue(), Files.readString(log));
            return Files.readString(log);
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
                assertTrue(process.waitFor(5, TimeUnit.SECONDS), "owned packaged app must terminate");
            }
            Files.deleteIfExists(log);
        }
    }
}
