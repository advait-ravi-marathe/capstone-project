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
    void scrumManifestHasThirtyConsolidatedEngineeringStoriesAndTwoWeekSprintCounts() throws Exception {
        JsonNode owner = new ObjectMapper().readTree(Files.readString(Path.of("docs/reboot/scrum-owner-manifest.json")));
        JsonNode cards = owner.get("cards");
        assertEquals(30, owner.get("story_count").asInt());
        assertEquals(30, cards.size());
        assertEquals("2026-10-26", cards.get(0).get("date").asString());
        assertEquals("2026-11-24", cards.get(29).get("date").asString());
        JsonNode canonical = new ObjectMapper().readTree(Files.readString(Path.of("docs/reboot/manifest.json")));
        assertEquals(30, canonical.get("board_story_count").asInt());
        assertEquals(30, canonical.get("assignments_by_track").get("engineering").asInt());
        assertEquals(canonical.get("board_story_count").asInt(), owner.get("story_count").asInt());
        assertEquals("https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0",
                canonical.get("separate_learning_repository").get("algorithm_practice").asString());
        for (int i = 0; i < 30; i++) {
            JsonNode story = cards.get(i);
            JsonNode engineering = story.get("engineering");
            int day = i + 1;
            assertEquals(day, story.get("sequence").asInt());
            assertEquals(day, story.get("day_number").asInt());
            assertTrue(story.get("title").asString().startsWith("Day%02d:".formatted(day)));
            assertEquals(120, engineering.get("budget_minutes").asInt());
            assertEquals(85, engineering.get("allocation_minutes").get("implementation_and_testing").asInt());
            assertEquals(25, engineering.get("allocation_minutes").get("concept_or_course").asInt());
            assertEquals(10, engineering.get("allocation_minutes").get("evidence").asInt());
            assertTrue(Files.isRegularFile(Path.of(engineering.get("contract_path").asString())));
            assertTrue(Files.isRegularFile(Path.of(engineering.get("acceptance_reference").asString())));
            assertTrue(Files.isRegularFile(Path.of(engineering.get("evidence_reference").asString())));
            assertFalse(story.has("java_dsa"), "DSA schedule must live outside capstone");
        }
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
