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
import java.net.URI;
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
        for (JsonNode card : cards.get("cards")) {
            JsonNode engineering = card.get("engineering");
            assertEquals(engineering.get("acceptance_test_path").asString(),
                    engineering.get("acceptance_reference").asString());
            for (String field : new String[]{"contract_path", "acceptance_reference", "evidence_reference",
                    "acceptance_test_path", "infrastructure_test_path"}) {
                assertTrue(Files.isRegularFile(Path.of(engineering.get(field).asString())),
                        "Broken Scrum manifest link in " + field + " for Day " + card.get("day_number").asInt());
            }
            assertTrue(Files.isRegularFile(Path.of(card.get("java_dsa").get("guide_path").asString())));
            assertTrue(Files.isRegularFile(Path.of(card.get("java_dsa").get("review_queue_command").asString()
                    .split(" ")[1])));
            assertTrue(Files.isRegularFile(Path.of(card.get("java_dsa").get("review_queue_test_command").asString()
                    .split(" ")[3])));
            JsonNode concept = engineering.get("concept_course");
            assertEquals(25, concept.get("minutes").asInt());
            if (concept.has("study_segments")) assertStudyBlock(concept, card.get("day_number").asInt());
            assertTrue(concept.get("resources").size() > 0);
            for (JsonNode resource : concept.get("resources")) {
                assertTrue(URI.create(resource.get("url").asString()).getScheme().equals("https"));
                assertFalse(resource.get("title").asString().isBlank());
                assertFalse(resource.get("section").asString().isBlank());
            }
            JsonNode dsa = card.get("java_dsa");
            if (dsa.get("mode").asString().equals("guided_first_sequence") && !dsa.get("problem_id").isNull()) {
                assertTrue(dsa.get("problem_url").asString().startsWith("https://leetcode.com/problems/"));
                assertTrue(dsa.get("problem_url").asString().endsWith("/"));
                if (Set.of(206, 143).contains(dsa.get("problem_id").asInt())) {
                    assertTrue(dsa.get("plan_membership").asString().contains("supplemental"));
                    assertTrue(card.get("title").asString().contains("supplemental bridge"));
                }
            }
        }
        JsonNode revisionPolicy = manifest.get("dsa_revision_policy");
        assertEquals("earliest actual first-attempt date, including guided actual attempts",
                revisionPolicy.get("due_date_anchor").asString());
        assertEquals("fresh similar-pattern transfer before editorial; assistance is not independent credit",
                revisionPolicy.get("retrieval_method").asString());
        assertEquals(60, revisionPolicy.get("daily_budget_minutes_by_exercise_count").get("1_or_2").asInt());
        assertEquals(75, revisionPolicy.get("daily_budget_minutes_by_exercise_count").get("3").asInt());
        assertEquals(90, revisionPolicy.get("daily_budget_minutes_by_exercise_count").get("4").asInt());
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
            JsonNode concept = eng.get("concept_course");
            assertEquals(25, concept.get("minutes").asInt());
            if (concept.has("study_segments")) {
                assertStudyBlock(concept, day.get("day").asInt());
                assertTrue(Files.isRegularFile(Path.of(concept.get("guide_path").asString())));
            }
            assertTrue(concept.get("resources").size() > 0);
            for (JsonNode resource : concept.get("resources")) {
                assertEquals("https", URI.create(resource.get("url").asString()).getScheme());
                assertFalse(resource.get("section").asString().isBlank());
            }
            assertTrue(eng.get("acceptance_commands").isArray() && eng.get("acceptance_commands").size() > 0);
            assertTrue(eng.get("infrastructure_commands").isArray() && eng.get("infrastructure_commands").size() > 0);
            assertTrue(eng.get("acceptance_commands").get(0).asString().startsWith("./mvnw")
                    || eng.get("acceptance_commands").get(0).asString().startsWith("cd frontend && npm"));
            assertTrue(eng.get("infrastructure_commands").get(0).asString().startsWith("./mvnw")
                    || eng.get("infrastructure_commands").get(0).asString().startsWith("npm --prefix frontend"));
            JsonNode dsa = day.get("java_dsa");
            assertEquals(60, dsa.get("budget_minutes_by_exercise_count").get("1_or_2_substantive_exercises").asInt());
            assertEquals(75, dsa.get("budget_minutes_by_exercise_count").get("3_substantive_exercises").asInt());
            assertEquals(90, dsa.get("budget_minutes_by_exercise_count").get("4_substantive_exercises").asInt());
            JsonNode card = cards.get("cards").get(i);
            if (concept.has("study_segments"))
                assertEquals(concept, card.get("course_learning_block"), "revised canonical and owner course blocks match");
            assertEquals(31 + i, card.get("day_number").asInt());
            assertEquals(180, card.get("budget_minutes_min").asInt());
            assertEquals(210, card.get("budget_minutes_max").asInt());
            assertTrue(Files.isRegularFile(Path.of(dsa.get("guide_path").asString())));
            assertEquals("python3 scripts/dsa-review-queue.py --on-date \"$(TZ=Asia/Kolkata date +%F)\" --roadmap-day " + day.get("day").asInt(),
                    dsa.get("review_queue_command").asString());
            assertTrue(dsa.get("review_queue_test_command").asString().contains("test_dsa_guided_transfer_policy.py"));
            assertFalse(day.get("mandatory_new_problem").asBoolean());
            assertEquals("dynamic_actual_attempt_queue", dsa.get("mode").asString());
            assertTrue(dsa.get("problem_id").isNull());
            assertTrue(dsa.get("review_anchors").isEmpty());
            assertTrue(dsa.has("historical_planned_assignment"));
            assertTrue(card.get("title").asString().endsWith("DSA actual-attempt queue"));
            assertEquals("python3 scripts/dsa-review-queue.py --on-date \"$(TZ=Asia/Kolkata date +%F)\" --roadmap-day " + (31 + i),
                    dsa.get("review_queue_command").asString());
        }
    }

    private void assertStudyBlock(JsonNode concept, int day) {
        assertEquals(25, concept.get("minutes").asInt());
        int seconds = 0;
        for (JsonNode segment : concept.get("study_segments")) seconds += segment.get("seconds").asInt();
        assertEquals(1500, seconds, "Day " + day + " study segments must fit exactly 25 minutes");
        assertTrue(concept.get("resources").size() > 0 || concept.get("lessons").isEmpty());
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
