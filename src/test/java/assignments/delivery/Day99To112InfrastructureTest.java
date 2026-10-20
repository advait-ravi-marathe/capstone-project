package assignments.delivery;

import org.junit.jupiter.api.Test;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.sql.SQLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Instructor-owned contract, migration, and continuation wiring checks. */
class Day99To112InfrastructureTest {
    private static final Path BASE = Path.of("docs/reboot/continuation-99-112");

    @Test
    void fourteenDatedContractsAndCourseCardsMatchTheVerifiedPlan() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode manifest = mapper.readTree(Files.readString(BASE.resolve("manifest.json")));
        JsonNode export = mapper.readTree(Files.readString(BASE.resolve("scrum-owner-manifest.json")));
        assertEquals(14, manifest.path("days").size());
        assertEquals(14, export.path("cards").size());
        LocalDate first = LocalDate.parse("2027-01-26");
        for (int index = 0; index < 14; index++) {
            JsonNode day = manifest.path("days").get(index);
            JsonNode card = export.path("cards").get(index);
            assertEquals(99 + index, day.path("day").asInt());
            assertEquals(first.plusDays(index).toString(), day.path("date").asString());
            assertEquals(day.path("title").asString(), card.path("title").asString());
            assertEquals(day.path("engineering"), card.path("engineering"),
                    "ticket export must preserve the canonical daily scope, selector, and course block");
            assertEquals(60, day.path("java_dsa").path("budget_minutes").asInt());
            String queue = day.path("java_dsa").path("review_queue_command").asString();
            assertTrue(queue.endsWith(first.plusDays(index).toString()));
            assertTrue(day.path("java_dsa").path("problem_id").isNull(), "no new DSA anchor is assigned");
            assertEquals("not_attempted", day.path("learner_status").asString());
            assertEquals(180, day.path("engineering").path("budget_minutes").asInt()
                    + day.path("java_dsa").path("budget_minutes").asInt());
            JsonNode study = day.path("engineering").path("concept_course");
            assertEquals(1500, study.path("video_seconds").asInt()
                    + study.path("worked_reference_seconds").asInt()
                    + study.path("teachback_seconds").asInt());
            String contract = Files.readString(Path.of(day.path("contract_file").asString()));
            for (JsonNode lesson : study.path("lessons")) {
                assertEquals("1341538", lesson.path("course_id").asString());
                assertFalse(lesson.path("source_item_id").asString().isBlank());
                assertFalse(lesson.path("section_id").asString().isBlank());
                assertTrue(contract.contains(lesson.path("title").asString()));
                assertTrue(contract.contains(lesson.path("section_title").asString()));
            }
            for (JsonNode course : study.path("courses")) {
                assertTrue(contract.contains(course.path("title").asString()));
                assertTrue(contract.contains(course.path("url").asString()));
            }
            for (JsonNode reference : study.path("resources")) {
                assertEquals("https", URI.create(reference.path("url").asString()).getScheme());
                assertTrue(contract.contains(reference.path("url").asString()));
                assertFalse(reference.path("reading_focus").asString().isBlank(),
                        "project-specific guidance is labeled as a reading focus, not an invented document heading");
                assertFalse(reference.has("section"), "do not label project reading guidance as an official source section");
            }
        }
    }

    @Test
    void migrationIsAdditiveAndDoesNotTurnPreviewIntoAWriter() throws Exception {
        String migration = Files.readString(Path.of("src/main/resources/db/migration/V004__event_log_delivery_work.sql"));
        assertTrue(migration.contains("REFERENCES training_policy_lifecycle_event"));
        assertTrue(migration.contains("PRIMARY KEY (policy_id, source_version)"));
        assertTrue(migration.contains("event_type = 'ISSUED'"));
        assertFalse(migration.toLowerCase().contains("truncate"));
        String preview = Files.readString(Path.of("src/main/java/assignments/insurance/JdbcPolicyRenewalReadStore.java"));
        assertFalse(preview.contains("training_issuance_work"));
        assertFalse(preview.contains("training_issuance_delivery_receipt"));
        String workflow = Files.readString(Path.of(".github/workflows/coverstory-starter.yml"));
        assertTrue(workflow.contains("learning/coverstory-days-99-112"));
        String app = Files.readString(Path.of("src/main/java/assignments/local/LocalCoverStoryApplication.java"));
        assertTrue(app.contains("coverstory.delivery.run-once"));
        assertTrue(app.contains("WebApplicationType.NONE"), "manual command mode must not bind a web server");
        String command = Files.readString(Path.of("src/main/java/assignments/delivery/LocalIssuanceDeliveryCommand.java"));
        assertTrue(command.contains("containsOption(\"coverstory.delivery.run-once\")"));
        assertTrue(command.contains("BatchDeliveryFailureException") && command.contains("committedProgress()"),
                "one-shot command reports the already committed portion of a stopped batch");
        assertTrue(Files.isRegularFile(Path.of("src/test/java/assignments/delivery/DurableWorkRestartAcceptanceTest.java")));
        assertTrue(Files.isRegularFile(Path.of("src/test/java/assignments/local/LocalPackagedIssuanceDeliveryAcceptanceTest.java")));
        String restart = Files.readString(Path.of("src/test/java/assignments/delivery/DurableWorkRestartAcceptanceTest.java"));
        assertTrue(restart.contains("destroyForcibly()") && restart.contains("DurableDeliveryChildMain.class.getName()")
                && restart.contains("awaitSignal"), "fresh-JVM harness has an explicit boundary and confirms termination");
        String packaged = Files.readString(Path.of("src/test/java/assignments/local/LocalPackagedIssuanceDeliveryAcceptanceTest.java"));
        assertTrue(packaged.contains("LocalPackagedRuntimeProcess.start(postgres, \"2028-01-25T12:00:00Z\")"),
                "renewal preview uses a deterministic fixed clock inside the seeded policy's window");
        assertTrue(packaged.contains("/api/policies"));
        assertTrue(packaged.contains("--coverstory.delivery.run-once"));
        assertTrue(packaged.contains("issueReplay") && packaged.contains("policyRows()")
                && packaged.contains("eventRows()") && packaged.contains("idempotencyRows()")
                && packaged.contains("workPayloadRows()") && packaged.contains("receiptPayloadRows()"),
                "packaged replay snapshots compare full source, idempotency, work, receipt, and timestamp payloads");
        assertTrue(Files.isRegularFile(Path.of("src/test/java/assignments/delivery/LocalIssuanceDeliveryCommandTest.java")),
                "the supplied command has fixed invalid/empty/partial result checks");
        String commandChecks = Files.readString(Path.of("src/test/java/assignments/delivery/LocalIssuanceDeliveryCommandTest.java"));
        assertTrue(commandChecks.contains("\"0\"") && commandChecks.contains("\"-1\"")
                && commandChecks.contains("\"21\"") && commandChecks.contains("partialFailureReturnsNonzero"));
        assertTrue(restart.contains("terminateAndAwait(restarted") && restart.contains("terminateAndAwait(child"),
                "both child JVMs are forcibly terminated and waited on every exit path");
        assertTrue(Files.isRegularFile(Path.of("src/test/java/assignments/delivery/DurableIssuanceAcceptanceTest.java")));
        assertFalse(Files.readString(Path.of("src/test/java/assignments/delivery/DurableIssuanceAcceptanceTest.java"))
                        .contains("day100SchemaConstraintsRejectDuplicateAndInvalidRows"),
                "Day 100 is instructor-owned migration assessment, not a learner TODO selector");
    }

    @Test
    void migrationEnforcesStableIdentityAndRequiredFields() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            var dataSource = postgres.getPostgresDatabase();
            Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            assertEquals(3, countSeedPolicies(jdbc),
                    "the additive migration preserves the three versioned V003 synthetic policies");
            insertSource(jdbc, "schema-a");
            insertSource(jdbc, "schema-b");

            String insertWork = """
                    INSERT INTO training_issuance_work
                    (policy_id,source_version,event_type,effective_date,envelope_schema_version)
                    VALUES (?,1,'ISSUED',DATE '2027-01-26',1)
                    """;
            jdbc.update(insertWork, "schema-a");
            DuplicateKeyException duplicate = assertThrows(DuplicateKeyException.class,
                    () -> jdbc.update(insertWork, "schema-a"));
            assertEquals("23505", sqlState(duplicate), "duplicate source identity violates the primary key");

            DataIntegrityViolationException invalidType = assertThrows(DataIntegrityViolationException.class,
                    () -> jdbc.update("""
                            INSERT INTO training_issuance_work
                            (policy_id,source_version,event_type,effective_date,envelope_schema_version)
                            VALUES ('schema-b',1,'ENDORSED',DATE '2027-01-26',1)
                            """));
            assertEquals("23514", sqlState(invalidType), "a distinct key reaches the ISSUED-only check constraint");

            DataIntegrityViolationException missingRequiredValue = assertThrows(DataIntegrityViolationException.class,
                    () -> jdbc.update("""
                            INSERT INTO training_issuance_work
                            (policy_id,source_version,event_type,effective_date,envelope_schema_version)
                            VALUES ('schema-b',1,'ISSUED',NULL,1)
                            """));
            assertEquals("23502", sqlState(missingRequiredValue), "required event payload fields are not nullable");

            DataIntegrityViolationException missingWork = assertThrows(DataIntegrityViolationException.class,
                    () -> jdbc.update("""
                            INSERT INTO training_issuance_delivery_receipt
                            (policy_id,source_version,event_type,effective_date,envelope_schema_version)
                            VALUES ('missing-policy',1,'ISSUED',DATE '2027-01-26',1)
                            """));
            assertEquals("23503", sqlState(missingWork), "receipt requires its source work row");
            assertEquals(3, countSeedPolicies(jdbc), "constraint probes do not alter prior lifecycle rows");
        }
    }

    private static void insertSource(JdbcTemplate jdbc, String policyId) {
        jdbc.update("""
                INSERT INTO training_policy_lifecycle
                  (policy_id,owner_id,journey_id,quote_version_id,product_version,rate_version,
                   annual_premium_rupees,currency,effective_date,status,version)
                VALUES (?, 'owner-a', ?, ?, 'home-secure-v1', 'training-rate-v1',
                        125.00, 'INR', DATE '2027-01-26', 'ISSUED', 1)
                """, policyId, "journey-" + policyId, "quote-" + policyId);
        jdbc.update("INSERT INTO training_policy_lifecycle_event(policy_id,version,event_type,effective_date) VALUES (?,1,'ISSUED',DATE '2027-01-26')",
                policyId);
    }

    private static int countSeedPolicies(JdbcTemplate jdbc) {
        return jdbc.queryForObject("SELECT count(*) FROM training_policy_lifecycle WHERE policy_id LIKE 'policy-%'", Integer.class);
    }

    private static String sqlState(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql) return sql.getSQLState();
        }
        return null;
    }
}
