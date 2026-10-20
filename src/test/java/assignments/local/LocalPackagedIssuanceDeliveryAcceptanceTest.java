package assignments.local;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Real packaged application/route plus explicit one-shot command; prior learner seams are prerequisites. */
@Tag("learnerAcceptance")
class LocalPackagedIssuanceDeliveryAcceptanceTest {
    @Test
    void protectedIssueThenManualCommandLeavesOneReceiptAndNoPreviewWrites() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
             LocalPackagedRuntimeProcess runtime = LocalPackagedRuntimeProcess.start(postgres, "2028-01-25T12:00:00Z")) {
            long runtimePid = runtime.pid();
            Path jar = Path.of("target/capstone-starter-1.0-SNAPSHOT.jar").toAbsolutePath();
            var csrf = runtime.get("/api/csrf");
            assertEquals(200, csrf.statusCode());
            String csrfCookie = cookie(csrf, "XSRF-TOKEN");
            String token = new ObjectMapper().readTree(csrf.body()).path("token").asString();
            var selected = runtime.post("/api/session", "{\"ownerId\":\"owner-a\"}", csrfCookie, null, token);
            assertEquals(200, selected.statusCode(), selected.body());
            String session = cookie(selected, "JSESSIONID");
            String request = "{\"journeyId\":\"journey-day111\",\"quoteVersionId\":\"quote-day111\","
                    + "\"clientRequestId\":\"delivery-day111\",\"effectiveDate\":\"2027-02-20\"}";
            var issued = runtime.post("/api/policies", request, csrfCookie, session, token);
            assertEquals(201, issued.statusCode(), issued.body());
            String policyId = new ObjectMapper().readTree(issued.body()).path("policyId").asString();
            assertFalse(policyId.isBlank());
            JdbcState before = snapshot(postgres);
            assertEquals(0, before.workRows());
            assertEquals(0, before.receipts());

            CommandResult first = runPackagedCommand(jar, postgres, 10);
            assertEquals(0, first.exitCode(), first.output());
            JdbcState delivered = snapshot(postgres);
            assertEquals(4, delivered.workRows(), "three committed V003 ISSUED events plus this issuance");
            assertEquals(4, delivered.receipts());
            assertEquals(4, delivered.acknowledged());
            assertTrue(delivered.sourceKeys().contains("policy-demo-1:1"));
            assertTrue(delivered.sourceKeys().contains("policy-renewal-a:1"));
            assertTrue(delivered.sourceKeys().contains("policy-renewal-b:1"));
            assertTrue(delivered.sourceKeys().contains(policyId + ":1"));

            var preview = runtime.get("/api/policies/" + policyId + "/renewal-preview", session);
            assertEquals(200, preview.statusCode(), preview.body());
            assertEquals(delivered, snapshot(postgres), "renewal preview remains read-only for delivery tables");
            CommandResult replay = runPackagedCommand(jar, postgres, 10);
            assertEquals(0, replay.exitCode(), replay.output());
            assertEquals(delivered, snapshot(postgres), "manual command replay is duplicate-safe");
            System.out.printf("temporary packaged integration witness: runtime-pid=%d command-pid=%d replay-pid=%d state=%s%n",
                    runtimePid, first.pid(), replay.pid(), delivered);
        }
    }

    private static CommandResult runPackagedCommand(Path jar, EmbeddedPostgres postgres, int limit) throws Exception {
        Path log = Files.createTempFile("coverstory-delivery-command-", ".log");
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        List<String> command = List.of(java, "-jar", jar.toString(), "--spring.profiles.active=local",
                "--spring.datasource.url=" + postgres.getJdbcUrl("postgres", "postgres"),
                "--spring.datasource.username=postgres", "--spring.datasource.password=",
                "--coverstory.delivery.run-once", "--coverstory.delivery.batch-size=" + limit);
        Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            assertTrue(process.waitFor(40, TimeUnit.SECONDS), "one-shot command watchdog: " + Files.readString(log));
            return new CommandResult(process.exitValue(), Files.readString(log), process.pid());
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            }
            Files.deleteIfExists(log);
        }
    }

    private static JdbcState snapshot(EmbeddedPostgres postgres) throws Exception {
        try (var connection = postgres.getPostgresDatabase().getConnection(); var statement = connection.createStatement();
             var rows = statement.executeQuery("""
                     SELECT (SELECT count(*) FROM training_issuance_work),
                            (SELECT count(*) FROM training_issuance_delivery_receipt),
                            (SELECT count(*) FROM training_issuance_work WHERE acknowledged_at IS NOT NULL),
                            (SELECT coalesce(array_agg(policy_id || ':' || source_version ORDER BY policy_id,source_version), ARRAY[]::text[])
                               FROM training_issuance_work)
                     """)) {
            assertTrue(rows.next());
            return new JdbcState(rows.getInt(1), rows.getInt(2), rows.getInt(3), List.of((String[]) rows.getArray(4).getArray()));
        }
    }

    private static String cookie(HttpResponse<?> response, String name) {
        return response.headers().allValues("set-cookie").stream().filter(value -> value.startsWith(name + "="))
                .map(value -> value.split(";", 2)[0]).findFirst().orElseThrow();
    }

    private record JdbcState(int workRows, int receipts, int acknowledged, List<String> sourceKeys) { }
    private record CommandResult(int exitCode, String output, long pid) { }
}
