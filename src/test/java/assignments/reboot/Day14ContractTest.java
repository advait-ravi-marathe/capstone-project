package assignments.reboot;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Readiness evidence only: a green check here means the backend integration stays blocked. */
class Day14ContractTest {
    @Test
    void reportsAllUnfinishedPrerequisitesAndNeverCallsTheIntegrationReady() throws Exception {
        JsonNode manifest = new ObjectMapper().readTree(Files.readString(Path.of("docs/reboot/manifest.json")));
        List<Integer> blockers = new ArrayList<>();
        for (JsonNode day : manifest.get("days")) {
            int number = day.get("day").asInt();
            if (number > 13) break;
            if (!"contract_passed".equals(day.get("learner_status").asString())
                    || !"passed".equals(day.get("integration_status").asString())) {
                blockers.add(number);
            }
        }

        assertEquals(java.util.stream.IntStream.rangeClosed(1, 13).boxed().toList(), blockers);
        assertFalse(blockers.isEmpty());
        assertTrue(blockers.containsAll(List.of(1, 8, 12, 13)));
    }
}
