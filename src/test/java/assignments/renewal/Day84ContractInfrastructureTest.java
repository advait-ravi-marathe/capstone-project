package assignments.renewal;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class Day84ContractInfrastructureTest {
    @Test
    void assessmentSeparatesInstructorEvidenceFromLearnerIntegration() throws Exception {
        var mapper = new ObjectMapper();
        var manifest = mapper.readTree(Files.readString(Path.of("docs/reboot/continuation-71-84/manifest.json")));
        assertFalse(manifest.path("dsa_revision_policy").path("actual_attempt_dates_supplied").asBoolean());
        assertTrue(manifest.path("scope").asText().contains("Reuse the completed Day48 owner-scoped SQL"));
        assertTrue(manifest.path("capacity_decision").path("pool_availability_guarantee").asText().startsWith("none;"));
        assertEquals(83, manifest.path("days").get(12).path("day").asInt());
        assertEquals("src/test/java/assignments/local/Day83PackagedLockBrowserAcceptanceTest.java",
                manifest.path("days").get(12).path("engineering").path("acceptance_test_path").asText());
        assertTrue(Files.exists(Path.of("src/test/java/assignments/local/Day83PackagedLockBrowserAcceptanceTest.java")));
        assertTrue(Files.exists(Path.of("frontend/e2e/day83-lock-recovery.mjs")));
    }
}
