package assignments.reboot;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Instructor integrity check: incomplete evidence stays open and never auto-passes. */
class Day30ContractTest {
    @Test
    void checklistContainsThirtyDatesAndKeepsUnverifiedWorkOpen() throws Exception {
        JsonNode manifest = new ObjectMapper().readTree(Files.readString(Path.of("docs/reboot/manifest.json")));
        assertTrue(manifest.get("days").size() == 30);
        String checklist = Files.readString(Path.of("docs/reboot/acceptance-checklist.md"));
        assertTrue(checklist.contains("| Browser-to-database acceptance |"));
        assertTrue(checklist.contains("| blocked | prerequisites incomplete |"));
        assertTrue(checklist.contains("| DSA attempts and due reviews |"));
        assertTrue(checklist.contains("| open | missing |"));
        assertFalse(checklist.contains("| passed |"));
        Matcher links = Pattern.compile("\\[[^]]+\\]\\(([^)]+)\\)").matcher(checklist);
        while (links.find()) {
            String target = links.group(1);
            assertFalse(target.startsWith("http"), "Checklist evidence links must be repository-local");
            assertTrue(Files.exists(Path.of("docs/reboot").resolve(target).normalize()),
                    "Broken checklist evidence/template link: " + target);
        }
        for (JsonNode day : manifest.get("days")) {
            assertTrue(java.util.Set.of("not_attempted", "attempted", "contract_passed").contains(day.get("learner_status").asString()),
                    "Manifest learner status is outside the allowed state machine");
        }
    }
}
