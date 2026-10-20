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
        assertEquals("2026-10-20", cards.get(0).get("date").asString());
        assertEquals("2026-11-18", cards.get(29).get("date").asString());
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
            assertEquals(180, story.get("budget_minutes").asInt());
            assertEquals(120, engineering.get("budget_minutes").asInt());
            assertEquals(60, dsa.get("budget_minutes").asInt());
            assertEquals(85, engineering.get("allocation_minutes").get("implementation_and_testing").asInt());
            assertEquals(25, engineering.get("allocation_minutes").get("concept_or_course").asInt());
            assertEquals(10, engineering.get("allocation_minutes").get("evidence").asInt());
            assertEquals(25, dsa.get("allocation_minutes").get("pattern_learning").asInt());
            assertEquals(20, dsa.get("allocation_minutes").get("independent_solve_or_cold_review").asInt());
            assertEquals(15, dsa.get("allocation_minutes").get("active_retrieval_and_error_log").asInt());
            assertTrue(Files.isRegularFile(Path.of(engineering.get("contract_path").asString())));
            assertTrue(Files.isRegularFile(Path.of(engineering.get("acceptance_reference").asString())));
            assertTrue(Files.isRegularFile(Path.of(engineering.get("evidence_reference").asString())));
            assertTrue(Files.isRegularFile(Path.of(dsa.get("guide_path").asString().split("#", 2)[0])));
            String title = story.get("title").asString();
            if (dsa.get("mode").asString().equals("new_pattern_anchor")) {
                assertTrue(title.contains(dsa.get("concept").asString()));
                assertEquals(1, count(title, "LC "));
                assertTrue(title.contains("LC " + dsa.get("problem_id").asInt()));
                assertEquals("new_pattern_anchor", manifestDay(day).get("dsa_role").asString());
            } else {
                assertEquals(dsa.get("review_anchors").size(), count(title, "LC "));
                for (JsonNode review : dsa.get("review_anchors"))
                    assertTrue(title.contains("LC " + review.get("problem_id").asInt() + " D+" + review.get("review_interval_days").asInt()));
                if (dsa.get("review_anchors").isEmpty()) assertTrue(title.contains("no due anchors"));
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
