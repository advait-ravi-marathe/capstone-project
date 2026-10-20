package assignments.reboot;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Instructor checks for manifest integrity and the isolated, synthetic SQL harness. */
class ContinuationManifestInfrastructureTest {
    private static final Path BASE = Path.of("docs/reboot/continuation-31-44");

    @Test void fourteenContractsAreDatedBudgetedAndNotMarkedComplete() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode manifest = mapper.readTree(Files.readString(BASE.resolve("manifest.json")));
        JsonNode cards = mapper.readTree(Files.readString(BASE.resolve("scrum-owner-manifest.json")));
        assertEquals(14, manifest.get("days").size());
        assertEquals(14, cards.get("cards").size());
        JsonNode revisionPolicy = manifest.get("dsa_revision_policy");
        assertEquals("actual_first_attempt_date from docs/reboot/continuation-31-44/dsa/actual-attempt-log.json",
                revisionPolicy.get("due_date_anchor").asString());
        assertEquals("cold recall and re-solve; do not reread", revisionPolicy.get("retrieval_method").asString());
        assertEquals(60, revisionPolicy.get("daily_budget_minutes").asInt());
        assertFalse(revisionPolicy.get("actual_attempt_dates_supplied").asBoolean());
        assertEquals(15, revisionPolicy.get("carryover_anchors_from_days_01_30").size());
        for (JsonNode carryover : revisionPolicy.get("carryover_anchors_from_days_01_30")) {
            assertTrue(carryover.get("actual_first_attempt_date").isNull());
            assertEquals("awaiting_actual_attempt", carryover.get("status").asString());
            assertFalse(carryover.get("completion_claimed").asBoolean());
        }
        JsonNode attemptLog = mapper.readTree(Files.readString(BASE.resolve("dsa/actual-attempt-log.json")));
        assertEquals(0, attemptLog.get("events").size());
        assertTrue(Files.isRegularFile(Path.of("scripts/dsa-review-queue.py")));
        assertTrue(Files.isRegularFile(Path.of("scripts/test_dsa_review_queue.py")));
        assertEquals(12, cards.get("sprint_boundaries").get(0).get("batch_story_count").asInt());
        assertEquals(2, cards.get("sprint_boundaries").get(1).get("batch_story_count").asInt());
        assertEquals(14, cards.get("sprint_boundaries").get(0).get("sprint_story_count_after_batch").asInt());
        assertEquals(2, cards.get("sprint_boundaries").get(1).get("sprint_story_count_after_batch").asInt());
        LocalDate date = LocalDate.parse("2026-11-19");
        for (int i = 0; i < 14; i++) {
            JsonNode day = manifest.get("days").get(i);
            assertEquals(31 + i, day.get("day").asInt());
            assertEquals(date.plusDays(i).toString(), day.get("date").asString());
            assertEquals("not_attempted", day.get("learner_status").asString());
            assertEquals("blocked", day.get("integration_status").asString());
            assertTrue(Set.of("partial", "not_installed").contains(day.get("scaffold_status").asString()));
            assertTrue(Files.isRegularFile(Path.of(day.get("contract_file").asString())));
            var eng = day.get("engineering");
            assertEquals(120, eng.get("budget_minutes").asInt());
            assertEquals(85, eng.get("allocation_minutes").get("implementation_and_testing").asInt());
            assertEquals(25, eng.get("allocation_minutes").get("concept_or_course").asInt());
            assertEquals(10, eng.get("allocation_minutes").get("evidence").asInt());
            assertTrue(Files.isRegularFile(Path.of(eng.get("evidence_reference").asString())));
            assertTrue(Files.isRegularFile(Path.of(eng.get("acceptance_test_path").asString())));
            assertTrue(Files.isRegularFile(Path.of(eng.get("infrastructure_test_path").asString())));
            assertTrue(eng.get("acceptance_commands").isArray() && eng.get("acceptance_commands").size() > 0);
            assertTrue(eng.get("infrastructure_commands").isArray() && eng.get("infrastructure_commands").size() > 0);
            assertTrue(eng.get("acceptance_commands").get(0).asString().startsWith("./mvnw")
                    || eng.get("acceptance_commands").get(0).asString().startsWith("cd frontend && npm"));
            assertTrue(eng.get("infrastructure_commands").get(0).asString().startsWith("./mvnw"));
            assertEquals(60, day.get("java_dsa").get("budget_minutes").asInt());
            JsonNode dsa = day.get("java_dsa");
            JsonNode card = cards.get("cards").get(i);
            assertEquals(31 + i, card.get("day_number").asInt());
            assertEquals(180, card.get("budget_minutes").asInt());
            assertTrue(Files.isRegularFile(Path.of(dsa.get("guide_path").asString())));
            assertEquals("python3 scripts/dsa-review-queue.py --on-date " + day.get("date").asString(),
                    dsa.get("review_queue_command").asString());
            assertEquals("python3 -m unittest scripts/test_dsa_review_queue.py",
                    dsa.get("review_queue_test_command").asString());
            if (dsa.get("mode").asString().equals("new_pattern_anchor")) {
                assertTrue(day.get("mandatory_new_problem").asBoolean());
                assertTrue(dsa.get("actual_first_attempt_date").isNull());
                assertEquals(3, dsa.get("review_due_dates_if_attempted_on_plan").size());
                assertTrue(card.get("title").asString().contains("LC " + dsa.get("problem_id").asInt()));
            } else {
                assertFalse(day.get("mandatory_new_problem").asBoolean());
                for (JsonNode review : dsa.get("review_anchors")) {
                    LocalDate anchor = LocalDate.parse(review.get("planned_anchor_date").asString());
                    assertEquals(anchor.plusDays(review.get("review_interval_days").asInt()).toString(),
                            review.get("planned_due_date").asString());
                    assertEquals("pending_actual_attempt", review.get("status").asString());
                    assertTrue(card.get("title").asString().contains("LC " + review.get("problem_id").asInt()
                            + " D+" + review.get("review_interval_days").asInt()));
                }
            }
        }
    }

    @Test void isolatedTrainingSchemaEnforcesLifecycleUniquenessAndNoCustomerPiiColumns() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            try (Connection connection = postgres.getPostgresDatabase().getConnection(); Statement statement = connection.createStatement()) {
                for (String sql : Files.readString(Path.of("src/test/resources/fixtures/continuation-31-44/policy-lifecycle.sql")).split(";"))
                    if (!sql.isBlank()) statement.execute(sql);
                try (var columns = connection.getMetaData().getColumns(null, "public", "training_policy_lifecycle", null)) {
                    while (columns.next()) assertFalse(Set.of("full_name", "address", "email", "phone", "birth_date").contains(columns.getString("COLUMN_NAME")));
                }
                statement.executeUpdate("INSERT INTO training_policy_lifecycle VALUES ('p-1','owner-demo','journey-demo','quote-demo','home-secure-v1','training-rate-v1',125.00,'INR',DATE '2026-12-01','ISSUED',1,NULL)");
                statement.executeUpdate("INSERT INTO training_policy_idempotency VALUES ('owner-demo','request-demo','fingerprint-demo','p-1')");
                assertThrows(SQLException.class, () -> statement.executeUpdate("INSERT INTO training_policy_idempotency VALUES ('owner-demo','request-demo','other-fingerprint','p-1')"));
                assertThrows(SQLException.class, () -> statement.executeUpdate("INSERT INTO training_policy_lifecycle_event VALUES ('missing-policy',1,'ISSUED',DATE '2026-12-01',NULL,NULL)"));
                try (var rows = statement.executeQuery("SELECT count(*) FROM training_policy_lifecycle")) { rows.next(); assertEquals(1, rows.getInt(1)); }
            }
        }
    }
}
