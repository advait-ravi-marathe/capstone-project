package assignments.local;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Real packaged app/route, idempotent issue replay, explicit one-shot command, and persisted row snapshots. */
@Tag("learnerAcceptance")
class LocalPackagedIssuanceDeliveryAcceptanceTest {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void protectedIssueReplayAndManualCommandLeaveOneReceiptAndNoPreviewWrites() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
             Sprint8PackagedRuntimeProcess runtime = Sprint8PackagedRuntimeProcess.start(postgres, "2028-01-25T12:00:00Z")) {
            long runtimePid = runtime.pid();
            Path jar = Path.of("target/capstone-starter-1.0-SNAPSHOT-sprint8.jar").toAbsolutePath();
            var csrf = runtime.get("/api/csrf");
            assertEquals(200, csrf.statusCode());
            String csrfCookie = cookie(csrf, "XSRF-TOKEN");
            String token = JSON.readTree(csrf.body()).path("token").asString();
            var selected = runtime.post("/api/session", "{\"ownerId\":\"owner-a\"}", csrfCookie, null, token);
            assertEquals(200, selected.statusCode(), selected.body());
            String session = cookie(selected, "JSESSIONID");
            String request = "{\"journeyId\":\"journey-day111\",\"quoteVersionId\":\"quote-day111\","
                    + "\"clientRequestId\":\"delivery-day111\",\"effectiveDate\":\"2027-02-20\"}";
            HttpResponse<String> issued = runtime.post("/api/policies", request, csrfCookie, session, token);
            assertEquals(201, issued.statusCode(), issued.body());
            JsonNode issuedBody = JSON.readTree(issued.body());
            String policyId = issuedBody.path("policyId").asString();
            assertFalse(policyId.isBlank());
            JdbcState afterFirstIssue = snapshot(postgres);
            assertEquals(0, afterFirstIssue.workRows(), "ordinary web startup and issuance do not deliver work");
            assertEquals(0, afterFirstIssue.receipts());

            HttpResponse<String> issueReplay = runtime.post("/api/policies", request, csrfCookie, session, token);
            assertEquals(200, issueReplay.statusCode(), issueReplay.body());
            assertEquals(issuedBody, JSON.readTree(issueReplay.body()), "same request key replays the same policy identity and snapshot");
            JdbcState afterIssueReplay = snapshot(postgres);
            assertEquals(afterFirstIssue, afterIssueReplay,
                    "replaying issuance changes no ordered source, lifecycle, idempotency, work, receipt, payload, or timestamp row");
            assertEquals(afterFirstIssue.policyRows(), afterIssueReplay.policyRows());
            assertEquals(afterFirstIssue.eventRows(), afterIssueReplay.eventRows());
            assertEquals(afterFirstIssue.idempotencyRows(), afterIssueReplay.idempotencyRows());
            assertEquals(afterFirstIssue.workPayloadRows(), afterIssueReplay.workPayloadRows());
            assertEquals(afterFirstIssue.receiptPayloadRows(), afterIssueReplay.receiptPayloadRows());

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
            assertEquals(delivered, snapshot(postgres), "renewal preview leaves complete lifecycle/idempotency/work/receipt rows unchanged");
            CommandResult empty = runPackagedCommand(jar, postgres, 10);
            assertEquals(0, empty.exitCode(), empty.output());
            assertTrue(empty.output().contains("captured=0 receipts=0 replayed=0 acknowledged=0 pending=0"),
                    "an empty manual pass reports its actual zero work");
            assertEquals(delivered, snapshot(postgres), "empty manual command replay is a no-op");

            CommandResult invalid = runPackagedCommand(jar, postgres, "21");
            assertEquals(2, invalid.exitCode(), invalid.output());
            assertTrue(invalid.output().contains("supply one integer in [1,20]"));
            assertEquals(delivered, snapshot(postgres), "invalid batch input never mutates any persisted row");
            System.out.printf("temporary packaged integration witness: runtime-pid=%d command-pid=%d empty-pid=%d invalid-pid=%d state=%s%n",
                    runtimePid, first.pid(), empty.pid(), invalid.pid(), delivered);
        }
    }

    @Test
    void partialCommandFailureExitsNonzeroAndPreservesOnlyEarlierCommittedProgress() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").target("4").load().migrate();
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            jdbc.update("""
                    INSERT INTO training_issuance_work(policy_id,source_version,event_type,effective_date,envelope_schema_version)
                    SELECT policy_id,version,event_type,effective_date,1 FROM training_policy_lifecycle_event
                     WHERE event_type='ISSUED' AND policy_id LIKE 'policy-%'
                    """);
            jdbc.update("""
                    INSERT INTO training_issuance_delivery_receipt(policy_id,source_version,event_type,effective_date,envelope_schema_version)
                    SELECT policy_id,source_version,event_type,effective_date,envelope_schema_version FROM training_issuance_work
                     WHERE policy_id LIKE 'policy-%'
                    """);
            jdbc.update("UPDATE training_issuance_work SET acknowledged_at=TIMESTAMPTZ '2027-01-26 00:00:00+00' WHERE policy_id LIKE 'policy-%'");

            insertSource(jdbc, "a-first", "2027-02-03");
            insertSource(jdbc, "b-conflict", "2027-02-04");
            jdbc.update("""
                    INSERT INTO training_issuance_work(policy_id,source_version,event_type,effective_date,envelope_schema_version)
                    VALUES ('b-conflict',1,'ISSUED',DATE '2027-02-04',1)
                    """);
            jdbc.update("""
                    INSERT INTO training_issuance_delivery_receipt(policy_id,source_version,event_type,effective_date,envelope_schema_version)
                    VALUES ('b-conflict',1,'ISSUED',DATE '2027-02-05',1)
                    """);
            JdbcState before = snapshot(postgres);
            String conflictingWorkBefore = rowJson(postgres, "training_issuance_work", "b-conflict");
            String conflictingReceiptBefore = rowJson(postgres, "training_issuance_delivery_receipt", "b-conflict");

            Path jar = Path.of("target/capstone-starter-1.0-SNAPSHOT-sprint8.jar").toAbsolutePath();
            CommandResult stopped = runPackagedCommand(jar, postgres, 10);
            assertEquals(1, stopped.exitCode(), stopped.output());
            assertTrue(stopped.output().contains("delivery batch stopped"));
            assertTrue(stopped.output().contains("captured=1 receipts=1 replayed=0 acknowledged=1 pending=1"), stopped.output());

            JdbcState after = snapshot(postgres);
            assertEquals(before.policyRows(), after.policyRows(), "delivery cannot mutate source policy payloads or timestamps");
            assertEquals(before.eventRows(), after.eventRows(), "delivery cannot mutate immutable source lifecycle events");
            assertEquals(before.idempotencyRows(), after.idempotencyRows(), "delivery cannot mutate request receipts");
            assertEquals(conflictingWorkBefore, rowJson(postgres, "training_issuance_work", "b-conflict"),
                    "the failing work row remains pending and byte-for-byte unchanged");
            assertEquals(conflictingReceiptBefore, rowJson(postgres, "training_issuance_delivery_receipt", "b-conflict"),
                    "a conflicting pre-existing receipt is not overwritten");
            assertNotNull(rowJson(postgres, "training_issuance_work", "a-first"));
            assertNotNull(rowJson(postgres, "training_issuance_delivery_receipt", "a-first"));
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM training_issuance_work WHERE policy_id='a-first' AND acknowledged_at IS NOT NULL", Integer.class));
            assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM training_issuance_work WHERE policy_id='b-conflict' AND acknowledged_at IS NOT NULL", Integer.class));
            assertEquals(before.workRows() + 1, after.workRows());
            assertEquals(before.receipts() + 1, after.receipts());
            assertEquals(before.acknowledged() + 1, after.acknowledged());
            System.out.printf("temporary partial command witness: command-pid=%d exit=%d before=%s after=%s%n",
                    stopped.pid(), stopped.exitCode(), before, after);
        }
    }

    private static CommandResult runPackagedCommand(Path jar, EmbeddedPostgres postgres, int limit) throws Exception {
        return runPackagedCommand(jar, postgres, List.of("--coverstory.delivery.batch-size=" + limit));
    }

    private static CommandResult runPackagedCommand(Path jar, EmbeddedPostgres postgres, String limit) throws Exception {
        return runPackagedCommand(jar, postgres, List.of("--coverstory.delivery.batch-size=" + limit));
    }

    private static CommandResult runPackagedCommand(Path jar, EmbeddedPostgres postgres, List<String> options) throws Exception {
        Path log = Files.createTempFile("coverstory-delivery-command-", ".log");
        String javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        var command = new java.util.ArrayList<String>(List.of(javaExecutable, "-jar", jar.toString(), "--spring.profiles.active=local,sprint8-local",
                "--spring.datasource.url=" + postgres.getJdbcUrl("postgres", "postgres"),
                "--spring.datasource.username=postgres", "--spring.datasource.password=", "--coverstory.delivery.run-once"));
        command.addAll(options);
        Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            assertTrue(process.waitFor(40, TimeUnit.SECONDS), "one-shot command watchdog: " + Files.readString(log));
            return new CommandResult(process.exitValue(), Files.readString(log), process.pid());
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                assertTrue(process.waitFor(5, TimeUnit.SECONDS), "packaged command process must be reaped");
            }
            Files.deleteIfExists(log);
        }
    }

    private static void insertSource(JdbcTemplate jdbc, String policyId, String effectiveDate) {
        jdbc.update("""
                INSERT INTO training_policy_lifecycle
                  (policy_id,owner_id,journey_id,quote_version_id,product_version,rate_version,
                   annual_premium_rupees,currency,effective_date,status,version)
                VALUES (?, 'owner-a', ?, ?, 'home-secure-v1', 'training-rate-v1',
                        125.00, 'INR', ?::date, 'ISSUED', 1)
                """, policyId, "journey-" + policyId, "quote-" + policyId, effectiveDate);
        jdbc.update("INSERT INTO training_policy_lifecycle_event(policy_id,version,event_type,effective_date) VALUES (?,1,'ISSUED',?::date)",
                policyId, effectiveDate);
    }

    private static JdbcState snapshot(EmbeddedPostgres postgres) throws Exception {
        int workRows;
        int receipts;
        int acknowledged;
        List<String> sourceKeys;
        try (Connection connection = postgres.getPostgresDatabase().getConnection(); Statement statement = connection.createStatement();
             var rows = statement.executeQuery("""
                     SELECT (SELECT count(*) FROM training_issuance_work),
                            (SELECT count(*) FROM training_issuance_delivery_receipt),
                            (SELECT count(*) FROM training_issuance_work WHERE acknowledged_at IS NOT NULL),
                            (SELECT coalesce(array_agg(policy_id || ':' || source_version ORDER BY policy_id,source_version), ARRAY[]::text[])
                               FROM training_issuance_work)
                     """)) {
            assertTrue(rows.next());
            workRows = rows.getInt(1);
            receipts = rows.getInt(2);
            acknowledged = rows.getInt(3);
            sourceKeys = List.of((String[]) rows.getArray(4).getArray());
        }
        return new JdbcState(workRows, receipts, acknowledged, sourceKeys,
                jsonRows(postgres, "SELECT coalesce(jsonb_agg(to_jsonb(t) ORDER BY t.policy_id),'[]'::jsonb)::text FROM training_policy_lifecycle t"),
                jsonRows(postgres, "SELECT coalesce(jsonb_agg(to_jsonb(t) ORDER BY t.policy_id,t.version),'[]'::jsonb)::text FROM training_policy_lifecycle_event t"),
                jsonRows(postgres, "SELECT coalesce(jsonb_agg(to_jsonb(t) ORDER BY t.owner_id,t.client_request_id),'[]'::jsonb)::text FROM training_policy_idempotency t"),
                jsonRows(postgres, "SELECT coalesce(jsonb_agg(to_jsonb(t) ORDER BY t.policy_id,t.source_version),'[]'::jsonb)::text FROM training_issuance_work t"),
                jsonRows(postgres, "SELECT coalesce(jsonb_agg(to_jsonb(t) ORDER BY t.policy_id,t.source_version),'[]'::jsonb)::text FROM training_issuance_delivery_receipt t"));
    }

    private static String jsonRows(EmbeddedPostgres postgres, String query) throws Exception {
        try (Connection connection = postgres.getPostgresDatabase().getConnection();
             Statement statement = connection.createStatement(); var rows = statement.executeQuery(query)) {
            assertTrue(rows.next());
            return rows.getString(1);
        }
    }

    private static String rowJson(EmbeddedPostgres postgres, String fixedTable, String policyId) throws Exception {
        if (!List.of("training_issuance_work", "training_issuance_delivery_receipt").contains(fixedTable)) {
            throw new IllegalArgumentException("only fixed delivery tables are accepted");
        }
        try (Connection connection = postgres.getPostgresDatabase().getConnection();
             var statement = connection.prepareStatement("SELECT to_jsonb(t)::text FROM " + fixedTable + " t WHERE policy_id=? AND source_version=1")) {
            statement.setString(1, policyId);
            try (var rows = statement.executeQuery()) { return rows.next() ? rows.getString(1) : null; }
        }
    }

    private static String cookie(HttpResponse<?> response, String name) {
        return response.headers().allValues("set-cookie").stream().filter(value -> value.startsWith(name + "="))
                .map(value -> value.split(";", 2)[0]).findFirst().orElseThrow();
    }

    private record JdbcState(int workRows, int receipts, int acknowledged, List<String> sourceKeys,
                             String policyRows, String eventRows, String idempotencyRows,
                             String workPayloadRows, String receiptPayloadRows) { }
    private record CommandResult(int exitCode, String output, long pid) { }
}
