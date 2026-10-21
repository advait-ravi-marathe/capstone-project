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
    private static final Path CURRICULUM = BASE.resolve("fde45-56-v2-curriculum.json");

    @Test void twelveDatesAreCompleteBudgetedAndStillMarkedLearnerOwned() throws Exception {
        var mapper = new ObjectMapper();
        JsonNode manifest = mapper.readTree(Files.readString(BASE.resolve("manifest.json")));
        JsonNode scrum = mapper.readTree(Files.readString(BASE.resolve("scrum-owner-manifest.json")));
        JsonNode curriculum = mapper.readTree(Files.readString(CURRICULUM));
        assertEquals("AUTHORITATIVE_SPRINT4_DAYS45_56_COURSEWORK_V2", curriculum.get("status").asString());
        assertEquals(12, curriculum.get("days").size());
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
            JsonNode curriculumDay = curriculum.get("days").get(i);
            assertEquals(120, eng.get("budget_minutes").asInt());
            assertEquals(85, eng.get("allocation_minutes").get("implementation_and_testing").asInt());
            assertEquals(25, eng.get("allocation_minutes").get("concept_or_course").asInt());
            assertEquals(10, eng.get("allocation_minutes").get("evidence").asInt());
            for (String field : new String[]{"acceptance_test_path", "infrastructure_test_path"})
                assertTrue(Files.isRegularFile(Path.of(eng.get(field).asString())), "missing " + field + " for Day " + (45 + i));
            assertEquals(card.get("engineering").get("acceptance_test_path").asString(),
                    card.get("engineering").get("acceptance_reference").asString());
            assertEquals(eng.get("acceptance_commands"), card.get("engineering").get("acceptance_commands"));
            assertEquals(eng.get("infrastructure_commands"), card.get("engineering").get("infrastructure_commands"));
            assertEquals(curriculumDay.get("preserved_acceptance_commands"), eng.get("acceptance_commands"));
            assertEquals(curriculumDay.get("infrastructure_commands"), eng.get("infrastructure_commands"));
            assertTrue(eng.get("acceptance_commands").isArray() && !eng.get("acceptance_commands").isEmpty());
            assertTrue(eng.get("infrastructure_commands").isArray() && !eng.get("infrastructure_commands").isEmpty());
            JsonNode course = eng.get("concept_course");
            assertEquals(25, course.get("minutes").asInt());
            int studySeconds = 0;
            for (JsonNode segment : course.get("study_segments")) studySeconds += segment.get("seconds").asInt();
            assertEquals(1500, studySeconds, "Day " + (45 + i) + " study segments stay inside 25 minutes");
            assertEquals(1500, course.get("study_total_seconds").asInt());
            int curriculumSeconds = 0;
            for (JsonNode segment : curriculumDay.get("study_segments")) curriculumSeconds += segment.get("seconds").asInt();
            assertEquals(1500, curriculumSeconds);
            assertEquals(course.get("study_segments"), curriculumDay.get("study_segments"));
            assertEquals(course.get("worked_example_path"), curriculumDay.get("guide_path"));
            assertTrue(Files.isRegularFile(Path.of(course.get("worked_example_path").asString())));
            String guide = Files.readString(Path.of(course.get("worked_example_path").asString()));
            assertTrue(guide.contains(course.get("worked_example_section").asString()), "guide anchor must exist");
            int practiceMinutes = 0;
            for (JsonNode segment : curriculumDay.get("practice_segment_minutes")) practiceMinutes += segment.asInt();
            assertEquals(85, practiceMinutes);
            assertEquals(10, curriculumDay.get("evidence_minutes").asInt());
            assertEquals(course, card.get("engineering").get("concept_course"));
            assertEquals(course.get("study_segments"), entryCourseLearning(day).get("study_segments"));
            for (JsonNode resource : course.get("resources")) {
                assertEquals("https", URI.create(resource.get("url").asString()).getScheme());
                assertFalse(resource.get("title").asString().isBlank());
                assertFalse(resource.get("section").asString().isBlank());
            }

            JsonNode dsa = day.get("java_dsa");
            assertTrue(dsa.get("budget_minutes").isNull());
            assertEquals(60, dsa.get("budget_minutes_by_exercise_count").get("1_or_2_substantive_exercises").asInt());
            assertEquals(75, dsa.get("budget_minutes_by_exercise_count").get("3_substantive_exercises").asInt());
            assertEquals(90, dsa.get("budget_minutes_by_exercise_count").get("4_substantive_exercises").asInt());
            assertTrue(Files.isRegularFile(Path.of(dsa.get("guide_path").asString())));
            assertTrue(Files.isRegularFile(Path.of(dsa.get("revision_log_path").asString())));
            assertEquals("python3 scripts/dsa-review-queue.py --on-date \"$(TZ=Asia/Kolkata date +%F)\" --roadmap-day " + (45 + i),
                    dsa.get("review_queue_command").asString());
            int dayNumber = day.get("day").asInt();
            assertEquals("dynamic_actual_attempt_queue", dsa.get("mode").asString());
            assertTrue(dsa.get("problem_id").isNull());
            assertTrue(dsa.get("review_anchors").isEmpty());
            assertTrue(dsa.has("historical_planned_assignment"));
            assertFalse(day.get("mandatory_new_problem").asBoolean());
            assertTrue(card.get("title").asString().endsWith("DSA actual-attempt queue"));
        }
    }

    private static JsonNode entryCourseLearning(JsonNode day) { return day.get("course_learning"); }

    @Test void scopeStaysReadOnlyAndSharedQueueIncludesNewAnchors() throws Exception {
        JsonNode manifest = new ObjectMapper().readTree(Files.readString(BASE.resolve("manifest.json")));
        String scope = manifest.get("scope").asString().toLowerCase();
        assertTrue(scope.contains("read-only"));
        assertTrue(scope.contains("no renewal acceptance"));
        assertTrue(scope.contains("270-day horizon remains a planning frame"));
        assertTrue(Files.readString(Path.of("scripts/dsa-review-queue.py")).contains("canonical-anchors.json"));
        assertTrue(Files.isRegularFile(Path.of("scripts/test_dsa_review_queue_45_56.py")));
        assertTrue(Files.isRegularFile(Path.of("src/test/java/assignments/renewal/RenewalPreviewTransportInfrastructureTest.java")));
        String dsaQueue = Files.readString(Path.of("scripts/dsa-review-queue.py"));
        assertTrue(dsaQueue.contains("canonical"));
        JsonNode attemptLog = new ObjectMapper().readTree(Files.readString(Path.of("docs/reboot/continuation-31-44/dsa/actual-attempt-log.json")));
        assertTrue(attemptLog.get("events").isArray(), "preserve existing actual-attempt history, whether empty or populated");
    }
}
