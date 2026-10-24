package assignments.reboot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Instructor integrity check: statuses are evidence-driven and valid completion is allowed. */
class Day30ContractTest {
    @TempDir Path temp;

    @Test
    void currentOpenChecklistHasThirtyContractsAndLocalEvidenceLinks() throws Exception {
        JsonNode manifest = new ObjectMapper().readTree(Files.readString(Path.of("docs/reboot/manifest.json")));
        assertTrue(manifest.get("days").size() == 30);
        String checklist = Files.readString(Path.of("docs/reboot/acceptance-checklist.md"));
        assertTrue(checklist.contains("| Browser-to-database acceptance |"));
        assertTrue(checklist.contains("| DSA attempts and due reviews |"));
        for (String row : checklist.lines().filter(line -> line.startsWith("| ") && !line.startsWith("| Area") && !line.startsWith("| Dimension") && !line.startsWith("|---"))
                .toList()) {
            String[] cells = row.split("\\|", -1);
            if (cells.length < 5) continue;
            String status = cells[3].trim();
            assertTrue(isValidStatusAndEvidence(status, cells[2], Path.of("docs/reboot")),
                    "Checklist status/evidence is invalid: " + row);
        }
        Matcher links = Pattern.compile("\\[[^]]+\\]\\(([^)]+)\\)").matcher(checklist);
        while (links.find()) {
            String target = links.group(1);
            assertTrue(!target.startsWith("http"), "Checklist evidence links must be repository-local");
            assertTrue(Files.exists(Path.of("docs/reboot").resolve(target).normalize()),
                    "Broken checklist evidence/template link: " + target);
        }
        for (JsonNode day : manifest.get("days")) {
            assertTrue(Set.of("not_attempted", "attempted", "contract_passed").contains(day.get("learner_status").asString()),
                    "Manifest learner status is outside the allowed state machine");
        }
    }

    @Test
    void fixedEvaluatorFixturesAllowHonestOpenBlockedAndEvidenceBackedPassedRows() throws Exception {
        Path receipt = temp.resolve("passed-receipt.json");
        Files.writeString(receipt, """
                {"command":"focused-test","exit_code":0}
                """);
        assertTrue(isValidStatusAndEvidence("open", "learner evidence missing", temp));
        assertTrue(isValidStatusAndEvidence("blocked", "prerequisites incomplete", temp));
        assertTrue(isValidStatusAndEvidence("passed", "[receipt](passed-receipt.json)", temp));
        assertFalse(isValidStatusAndEvidence("passed", "evidence not linked", temp));
        assertFalse(isValidStatusAndEvidence("complete", "[receipt](passed-receipt.json)", temp));
    }

    @Test
    void scrumManifestHasThirtyConsolidatedDailyStoriesAndTwoWeekSprintCounts() throws Exception {
        JsonNode owner = new ObjectMapper().readTree(Files.readString(Path.of("docs/reboot/scrum-owner-manifest.json")));
        JsonNode cards = owner.get("cards");
        assertEquals(30, owner.get("story_count").asInt());
        assertEquals(30, cards.size());
        assertEquals("2026-10-24", cards.get(0).get("date").asString());
        assertEquals("2026-11-22", cards.get(29).get("date").asString());
        assertEquals(14, owner.get("sprint_boundaries").get(0).get("story_count").asInt());
        assertEquals(14, owner.get("sprint_boundaries").get(1).get("story_count").asInt());
        assertEquals(2, owner.get("sprint_boundaries").get(2).get("story_count").asInt());
        JsonNode canonical = new ObjectMapper().readTree(Files.readString(Path.of("docs/reboot/manifest.json")));
        assertEquals(30, canonical.get("board_story_count").asInt());
        assertEquals(30, canonical.get("assignments_by_track").get("engineering").asInt());
        assertEquals(30, canonical.get("assignments_by_track").get("java_dsa").asInt());
        assertEquals(canonical.get("board_story_count").asInt(), owner.get("story_count").asInt());
        for (int sprintIndex = 0; sprintIndex < 3; sprintIndex++)
            assertEquals(owner.get("sprint_boundaries").get(sprintIndex).get("story_count").asInt(),
                    canonical.get("sprint_boundaries").get(sprintIndex).get("story_count").asInt());
        for (int dayIndex = 0; dayIndex < 30; dayIndex++) {
            JsonNode story = cards.get(dayIndex);
            JsonNode engineering = story.get("engineering");
            JsonNode dsa = story.get("java_dsa");
            int day = dayIndex + 1;
            assertEquals(day, story.get("sequence").asInt());
            assertEquals(day, story.get("day_number").asInt());
            assertTrue(story.get("title").asString().startsWith("Day%02d:".formatted(day)));
            assertTrue(story.get("budget_minutes").isNull());
            assertEquals(180, story.get("budget_minutes_min").asInt());
            assertEquals(210, story.get("budget_minutes_max").asInt());
            assertEquals(120, engineering.get("budget_minutes").asInt());
            assertTrue(dsa.get("budget_minutes").isNull());
            assertEquals(60, dsa.get("budget_minutes_by_exercise_count").get("1_or_2_substantive_exercises").asInt());
            assertEquals(75, dsa.get("budget_minutes_by_exercise_count").get("3_substantive_exercises").asInt());
            assertEquals(90, dsa.get("budget_minutes_by_exercise_count").get("4_substantive_exercises").asInt());
            assertEquals(85, engineering.get("allocation_minutes").get("implementation_and_testing").asInt());
            assertEquals(25, engineering.get("allocation_minutes").get("concept_or_course").asInt());
            assertEquals(10, engineering.get("allocation_minutes").get("evidence").asInt());
            assertTrue(dsa.get("allocation_minutes").isEmpty());
            assertTrue(Files.isRegularFile(Path.of(engineering.get("contract_path").asString())));
            assertTrue(Files.isRegularFile(Path.of(engineering.get("acceptance_reference").asString())));
            assertTrue(Files.isRegularFile(Path.of(engineering.get("evidence_reference").asString())));
            assertTrue(Files.isRegularFile(Path.of(dsa.get("guide_path").asString().split("#", 2)[0])));
            String title = story.get("title").asString();
            if (day <= 3) {
                int expectedProblem = new int[]{121, 1, 383}[day - 1];
                assertEquals("guided_first_sequence", dsa.get("mode").asString());
                assertEquals(expectedProblem, dsa.get("current_guided_encounter_problem_id").asInt());
                assertEquals("guided_first_sequence", manifestDay(day).get("dsa_role").asString());
                if (day == 3) {
                    assertEquals("383 Ransom Note", dsa.get("problem").asString());
                    assertTrue(dsa.get("problem_url").asString().endsWith("/ransom-note/"));
                    assertTrue(dsa.get("resource_references").get(0).asString().contains("/ransom-note/"));
                }
            } else {
                assertEquals("dynamic_actual_attempt_queue", dsa.get("mode").asString());
                assertTrue(dsa.get("current_guided_encounter_problem_id").isNull());
                assertEquals("dynamic_actual_attempt_queue", manifestDay(day).get("dsa_role").asString());
            }
        }
    }

    private static JsonNode manifestDay(int day) throws Exception {
        JsonNode days = new ObjectMapper().readTree(Files.readString(Path.of("docs/reboot/manifest.json"))).get("days");
        return days.get(day - 1);
    }

    private static long count(String source, String needle) {
        return Pattern.compile(Pattern.quote(needle)).matcher(source).results().count();
    }

    private static boolean isValidStatusAndEvidence(String status, String evidenceCell, Path base) {
        return Set.of("open", "blocked", "passed").contains(status)
                && (!status.equals("passed") || hasExistingEvidence(evidenceCell, base));
    }

    private static boolean hasExistingEvidence(String cell, Path base) {
        Matcher matcher = Pattern.compile("\\[[^]]+\\]\\(([^)]+)\\)").matcher(cell);
        boolean found = false;
        while (matcher.find()) {
            Path evidence = base.resolve(matcher.group(1)).normalize();
            if (Files.exists(evidence) && Files.isRegularFile(evidence)) found = true;
        }
        return found;
    }
}
