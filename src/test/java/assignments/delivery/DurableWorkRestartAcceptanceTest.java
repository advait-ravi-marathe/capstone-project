package assignments.delivery;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Requires temporary learner witnesses; this launches and kills real independent JVMs. */
@Tag("learnerAcceptance")
class DurableWorkRestartAcceptanceTest {
    @Test
    void freshJvmReplayFinishesAcknowledgmentWithoutDuplicatingCommittedReceipt() throws Exception {
        Path directory = Files.createTempDirectory("coverstory-delivery-restart-");
        Path signal = directory.resolve("receipt-committed-before-ack.txt");
        Path firstLog = directory.resolve("child-before-crash.log");
        Path recoveryLog = directory.resolve("child-after-restart.log");
        Process child = null;
        Process restarted = null;
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
          try {
            DataSource dataSource = postgres.getPostgresDatabase();
            Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
            insertCommittedSource(dataSource);
            child = child(postgres.getJdbcUrl("postgres", "postgres"), "block-before-ack", signal)
                    .redirectErrorStream(true).redirectOutput(firstLog.toFile()).start();
            long blockedChildPid = child.pid();
            try {
                awaitSignal(child, signal, firstLog, Duration.ofSeconds(30));
                JdbcTemplate jdbc = new JdbcTemplate(dataSource);
                assertEquals(1, count(jdbc, "training_issuance_delivery_receipt"),
                        "the first bounded receipt is independently visible after its commit");
                assertEquals(4, count(jdbc, "training_issuance_work"),
                        "the three V003 source events and this test source were captured");
                assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM training_issuance_delivery_receipt WHERE policy_id='policy-demo-1' AND source_version=1", Integer.class));
                assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM training_issuance_work WHERE acknowledged_at IS NOT NULL", Integer.class),
                        "worker has not acknowledged the committed effect");
                child.destroyForcibly();
                assertTrue(child.waitFor(5, TimeUnit.SECONDS), "parent confirms the first JVM terminated");
                assertFalse(child.isAlive());
            } finally {
                terminateAndAwait(child, "pre-crash child");
            }

            restarted = child(postgres.getJdbcUrl("postgres", "postgres"), "run", null)
                    .redirectErrorStream(true).redirectOutput(recoveryLog.toFile()).start();
            long recoveryChildPid = restarted.pid();
            assertTrue(restarted.waitFor(30, TimeUnit.SECONDS), "fresh JVM recovery watchdog");
            assertEquals(0, restarted.exitValue(), Files.readString(recoveryLog));
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            assertEquals(4, count(jdbc, "training_issuance_delivery_receipt"));
            assertEquals(4, jdbc.queryForObject("SELECT count(*) FROM training_issuance_work WHERE acknowledged_at IS NOT NULL", Integer.class));
            assertEquals(4, jdbc.queryForObject("SELECT count(*) FROM training_issuance_work", Integer.class));
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM training_issuance_delivery_receipt WHERE policy_id='restart-policy' AND source_version=1", Integer.class));
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM training_issuance_delivery_receipt WHERE policy_id='policy-demo-1' AND source_version=1", Integer.class));
            System.out.printf("temporary restart witness: killed-child-pid=%d exit=%d recovery-child-pid=%d exit=%d%n",
                    blockedChildPid, child.exitValue(), recoveryChildPid, restarted.exitValue());
          } finally {
            terminateAndAwait(child, "pre-crash child cleanup");
            terminateAndAwait(restarted, "recovery child cleanup");
          }
        } finally {
            Files.deleteIfExists(signal);
            Files.deleteIfExists(firstLog);
            Files.deleteIfExists(recoveryLog);
            Files.deleteIfExists(directory);
        }
    }

    private static void terminateAndAwait(Process process, String label) throws InterruptedException {
        if (process == null) return;
        if (process.isAlive()) process.destroyForcibly();
        assertTrue(process.waitFor(5, TimeUnit.SECONDS), label + " must terminate on every test path");
        assertFalse(process.isAlive(), label + " must not leak after its parent test ends");
    }

    private static ProcessBuilder child(String url, String mode, Path marker) {
        String javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        var command = new java.util.ArrayList<String>(java.util.List.of(javaExecutable, "-cp",
                System.getProperty("java.class.path"), DurableDeliveryChildMain.class.getName(), url));
        if (marker == null) command.add("run");
        else { command.add(mode); command.add(marker.toString()); }
        return new ProcessBuilder(command);
    }

    private static void awaitSignal(Process child, Path signal, Path log, Duration watchdog) throws Exception {
        long deadline = System.nanoTime() + watchdog.toNanos();
        while (!Files.exists(signal) && child.isAlive() && System.nanoTime() < deadline) {
            Thread.sleep(25); // bounded polling for the explicit child marker, not a timing assertion
        }
        assertTrue(Files.exists(signal), "child did not reach the post-receipt boundary; log=" + Files.readString(log));
    }

    private static int count(JdbcTemplate jdbc, String table) {
        if (!table.matches("training_issuance_(work|delivery_receipt)")) throw new IllegalArgumentException("fixed table only");
        return jdbc.queryForObject("SELECT count(*) FROM " + table, Integer.class);
    }

    private static void insertCommittedSource(DataSource dataSource) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update("""
                INSERT INTO training_policy_lifecycle
                  (policy_id,owner_id,journey_id,quote_version_id,product_version,rate_version,
                   annual_premium_rupees,currency,effective_date,status,version)
                VALUES ('restart-policy','owner-a','restart-journey','restart-quote','home-secure-v1','training-rate-v1',
                        125.00,'INR',DATE '2027-02-03','ISSUED',1)
                """);
        jdbc.update("INSERT INTO training_policy_lifecycle_event(policy_id,version,event_type,effective_date) VALUES ('restart-policy',1,'ISSUED',DATE '2027-02-03')");
    }
}
