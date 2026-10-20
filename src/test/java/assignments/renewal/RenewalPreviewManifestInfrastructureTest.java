package assignments.renewal;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Instructor integrity checks for the additive 12-day contract batch. */
class RenewalPreviewManifestInfrastructureTest {
    private static final Path BASE = Path.of("docs/reboot/continuation-45-56");

    @Test void twelveDatesAreCompleteBudgetedAndStillMarkedLearnerOwned() throws Exception {
        var mapper = new ObjectMapper();
        JsonNode manifest = mapper.readTree(Files.readString(BASE.resolve("manifest.json")));
        JsonNode scrum = mapper.readTree(Files.readString(BASE.resolve("scrum-owner-manifest.json")));
        assertEquals("350aa931c15f7a4e4c54a8b572bc4b9c5596902c", manifest.get("base_sha").asString());
        assertEquals(12, manifest.get("days").size());
        assertEquals(12, scrum.get("story_count").asInt());
        assertEquals(12, scrum.get("cards").size());
        assertEquals(14, scrum.get("sprint_boundaries").get(0).get("sprint_story_count_after_batch").asInt());
        assertEquals(12, scrum.get("sprint_boundaries").get(0).get("batch_story_count").asInt());

        LocalDate date = LocalDate.parse("2026-12-03");
        for (int i = 0; i < 12; i++) {
            JsonNode day = manifest.get("days").get(i);
            JsonNode card = scrum.get("cards").get(i);
            assertEquals(45 + i, day.get("day").asInt());
            assertEquals(45 + i, card.get("day_number").asInt());
            assertEquals(date.plusDays(i).toString(), day.get("date").asString());
            assertEquals(day.get("date").asString(), card.get("date").asString());
            assertEquals("Sprint 4", card.get("sprint").asString());
            assertEquals("not_attempted", day.get("learner_status").asString());
            assertEquals("blocked", day.get("integration_status").asString());
            assertEquals("partial", day.get("scaffold_status").asString());
            assertTrue(Files.isRegularFile(Path.of(day.get("contract_file").asString())));
            assertTrue(Files.isRegularFile(Path.of("evidence/continuation-45-56/day-%02d.json".formatted(45 + i))));

            JsonNode eng = day.get("engineering");
            assertEquals(120, eng.get("budget_minutes").asInt());
            assertEquals(85, eng.get("allocation_minutes").get("implementation_and_testing").asInt());
            assertEquals(25, eng.get("allocation_minutes").get("concept_or_course").asInt());
            assertEquals(10, eng.get("allocation_minutes").get("evidence").asInt());
            for (String field : new String[]{"acceptance_test_path", "infrastructure_test_path"})
                assertTrue(Files.isRegularFile(Path.of(eng.get(field).asString())), "missing " + field + " for Day " + (45 + i));
            assertEquals(card.get("engineering").get("acceptance_test_path").asString(),
                    card.get("engineering").get("acceptance_reference").asString());
            assertTrue(eng.get("acceptance_commands").isArray() && !eng.get("acceptance_commands").isEmpty());
            assertTrue(eng.get("infrastructure_commands").isArray() && !eng.get("infrastructure_commands").isEmpty());
            JsonNode course = eng.get("concept_course");
            assertEquals(25, course.get("minutes").asInt());
            assertEquals(10, course.get("allocation_minutes").get("focused_reading").asInt());
            assertEquals(10, course.get("allocation_minutes").get("design_sketch").asInt());
            assertEquals(5, course.get("allocation_minutes").get("teachback").asInt());
            for (JsonNode resource : course.get("resources")) {
                assertEquals("https", URI.create(resource.get("url").asString()).getScheme());
                assertFalse(resource.get("title").asString().isBlank());
                assertFalse(resource.get("section").asString().isBlank());
            }

            JsonNode dsa = day.get("java_dsa");
            assertEquals(60, dsa.get("budget_minutes").asInt());
            assertTrue(Files.isRegularFile(Path.of(dsa.get("guide_path").asString())));
            assertTrue(Files.isRegularFile(Path.of(dsa.get("revision_log_path").asString())));
            assertEquals("python3 scripts/dsa-review-queue.py --on-date " + day.get("date").asString(),
                    dsa.get("review_queue_command").asString());
            if ((45 + i) % 2 == 1) {
                assertEquals("new_pattern_anchor", dsa.get("mode").asString());
                assertTrue(dsa.get("actual_first_attempt_date").isNull());
                assertEquals(3, dsa.get("review_due_dates_if_attempted_on_plan").size());
                assertTrue(card.get("title").asString().contains("LC " + dsa.get("problem_id").asInt()));
            } else {
                assertEquals("review_and_consolidation", dsa.get("mode").asString());
                assertFalse(day.get("mandatory_new_problem").asBoolean());
                for (JsonNode review : dsa.get("review_anchors")) {
                    LocalDate anchor = LocalDate.parse(review.get("planned_anchor_date").asString());
                    assertEquals(anchor.plusDays(review.get("review_interval_days").asInt()).toString(),
                            review.get("planned_due_date").asString());
                    assertEquals("pending_actual_attempt", review.get("status").asString());
                }
            }
        }
    }

    @Test void scopeStaysReadOnlyAndSharedQueueIncludesNewAnchors() throws Exception {
        JsonNode manifest = new ObjectMapper().readTree(Files.readString(BASE.resolve("manifest.json")));
        String scope = manifest.get("scope").asString().toLowerCase();
        assertTrue(scope.contains("read-only"));
        assertTrue(scope.contains("no renewal acceptance"));
        assertTrue(scope.contains("270-day horizon remains a planning frame"));
        assertTrue(Files.readString(Path.of("scripts/dsa-review-queue.py")).contains("continuation-45-56/manifest.json"));
        assertTrue(Files.isRegularFile(Path.of("scripts/test_dsa_review_queue_45_56.py")));
        assertTrue(Files.isRegularFile(Path.of("src/test/java/assignments/renewal/RenewalPreviewTransportInfrastructureTest.java")));
    }
}
