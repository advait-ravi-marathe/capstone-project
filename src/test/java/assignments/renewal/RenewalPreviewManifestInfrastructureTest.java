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

        LocalDate date = LocalDate.parse("2026-12-07");
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
            assertFalse(course.has("allocation_minutes"), "nested course allocation values are seconds, never minutes");
            JsonNode allocationSeconds = course.get("allocation_seconds");
            assertNotNull(allocationSeconds, "the rendered study breakdown must name its unit");
            int allocatedStudySeconds = 0;
            for (String name : allocationSeconds.propertyNames()) allocatedStudySeconds += allocationSeconds.get(name).asInt();
            assertEquals(1500, allocatedStudySeconds);
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
            String renderedCourseBlock = renderCourseLearningBlock(course, eng.get("scope").asString());
            assertEquals(renderedCourseBlock, eng.get("course_learning_block").asString(),
                    "canonical learning block is rendered from current metadata");
            assertEquals(renderedCourseBlock, card.get("course_learning_block").asString(),
                    "owner export must carry the same rendered block");
            for (String stale : new String[]{"TanStack", "CSRF", "503/500", "packaged browser", "exclude the current annual premium"})
                assertFalse(renderedCourseBlock.toLowerCase().contains(stale.toLowerCase()), "stale course text: " + stale);
            assertEquals(course.get("study_segments"), entryCourseLearning(day).get("study_segments"));
            for (JsonNode resource : course.get("resources")) {
                assertEquals("https", URI.create(resource.get("url").asString()).getScheme());
                assertFalse(resource.get("title").asString().isBlank());
                assertFalse(resource.get("section").asString().isBlank());
            }

            assertFalse(day.has("java_dsa"), "DSA assignment is maintained in the separate repository");
            assertEquals("https://github.com/advait-ravi-marathe/dsa-prep/tree/b9d4706a9ead281a8881ab3767aa415ad6039f14",
                    manifest.get("separate_learning_repository").get("algorithm_practice").asString());
        }
    }

    private static JsonNode entryCourseLearning(JsonNode day) { return day.get("course_learning"); }

    private static String renderCourseLearningBlock(JsonNode course, String scope) {
        var lines = new java.util.ArrayList<String>();
        lines.add("**Learning route — exactly 25 minutes total.**");
        for (JsonNode lesson : course.path("lessons")) {
            String access = "unconfirmed";
            for (JsonNode entry : course.path("courses"))
                if (entry.get("course_id").asString().equals(lesson.get("course_id").asString()))
                    access = entry.get("access_status").asString();
            lines.add("- Required course reference: **" + lesson.get("title").asString() + "**, section **" +
                    lesson.get("section_title").asString() + "** (" + lesson.get("duration").asString() +
                    "); access: `" + access + "`. Viewing is not inferred.");
        }
        if (course.path("optional_reference_lessons").size() > 0) {
            var references = new java.util.ArrayList<String>();
            for (JsonNode lesson : course.path("optional_reference_lessons"))
                references.add("**" + lesson.get("title").asString() + "** (" + lesson.get("duration").asString() + ")");
            lines.add("- Optional prerequisite recovery only: " + String.join("; ", references) +
                    ". It replaces focused review if useful and adds no time or new-mastery claim.");
        }
        var segments = new java.util.ArrayList<String>();
        for (JsonNode segment : course.path("study_segments"))
            segments.add(segment.get("activity").asString().replace('_', ' ') + " " +
                    clock(segment.get("seconds").asInt()));
        lines.add("- Fixed study segments: " + String.join("; ", segments) +
                ". Stop at 25:00; carry unfinished work forward without extending the day.");
        String section = course.get("worked_example_section").asString();
        lines.add("- Full project-authored worked example before transfer: [" +
                section.substring(section.indexOf(" — ") + 3) + "](" + course.get("worked_example_path").asString() +
                "); this is not an official-source quotation.");
        var resources = new java.util.ArrayList<String>();
        for (JsonNode resource : course.path("resources"))
            resources.add("[" + resource.get("title").asString() + " — " + resource.get("section").asString() +
                    "](" + resource.get("url").asString() + ")");
        lines.add("- Focused official sources: " + String.join("; ", resources) + ".");
        lines.add("- Learner transfer: " + scope + ".");
        lines.add("- Course entries are source references only; no video viewing, access, or learner completion is claimed.");
        return String.join("\n", lines);
    }

    private static String clock(int seconds) { return (seconds / 60) + ":%02d".formatted(seconds % 60); }

    @Test void scopeStaysReadOnlyAndTransportTestsRemainInTheEngineeringRepo() throws Exception {
        JsonNode manifest = new ObjectMapper().readTree(Files.readString(BASE.resolve("manifest.json")));
        String scope = manifest.get("scope").asString().toLowerCase();
        assertTrue(scope.contains("read-only"));
        assertTrue(scope.contains("no renewal acceptance"));
        assertTrue(scope.contains("270-day horizon remains a planning frame"));
        assertTrue(Files.isRegularFile(Path.of("src/test/java/assignments/renewal/RenewalPreviewTransportInfrastructureTest.java")));
    }
}
