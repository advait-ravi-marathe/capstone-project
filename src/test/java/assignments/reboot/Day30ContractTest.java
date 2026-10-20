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

/** Instructor integrity check: statuses are evidence-driven and valid completion is allowed. */
class Day30ContractTest {
    @TempDir Path temp;

    @Test
    void currentOpenChecklistHasThirtyContractsAndLocalEvidenceLinks() throws Exception {
        JsonNode manifest = new ObjectMapper().readTree(Files.readString(Path.of("docs/reboot/manifest.json")));
        assertTrue(manifest.get("days").size() == 30);
        String checklist = Files.readString(Path.of("docs/reboot/acceptance-checklist.md"));
        assertTrue(checklist.contains("| Browser-to-database acceptance |"));
        assertTrue(checklist.contains("| blocked | prerequisites incomplete |"));
        assertTrue(checklist.contains("| DSA attempts and due reviews |"));
        assertTrue(checklist.contains("| open | missing |"));
        for (String row : checklist.lines().filter(line -> line.startsWith("| ") && !line.startsWith("| Area") && !line.startsWith("| Dimension") && !line.startsWith("|---"))
                .toList()) {
            String[] cells = row.split("\\|", -1);
            if (cells.length < 5) continue;
            String status = cells[3].trim();
            assertTrue(Set.of("open", "blocked", "passed").contains(status), "Unknown checklist status: " + status);
            if (status.equals("passed")) assertTrue(hasExistingEvidence(cells[2], Path.of("docs/reboot")),
                    "A passed row must link to existing evidence: " + row);
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
    void passedStatusIsAllowedWhenLocalEvidenceExists() throws Exception {
        Path receipt = temp.resolve("passed-receipt.json");
        Files.writeString(receipt, """
                {"command":"focused-test","exit_code":0}
                """);
        String row = "| example | [receipt](passed-receipt.json) | passed | focused test exit 0 |";
        assertTrue(hasExistingEvidence(row.split("\\|", -1)[2], temp));
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
