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
        var stories = mapper.readTree(Files.readString(Path.of("docs/reboot/continuation-71-84/scrum-owner-manifest.json")));
        assertEquals(14, manifest.path("days").size());
        assertEquals(14, stories.path("cards").size());
        for (int i = 0; i < 14; i++) {
            assertEquals(manifest.path("days").get(i).path("day").asInt(),
                    stories.path("cards").get(i).path("day_number").asInt(), "day index " + i);
            assertEquals(manifest.path("days").get(i).path("title").asText(),
                    stories.path("cards").get(i).path("title").asText(), "title for day "
                            + manifest.path("days").get(i).path("day").asInt());
        }
        assertTrue(manifest.path("scope").asText().contains("Reuse the completed Day48 owner-scoped SQL"));
        assertTrue(manifest.path("capacity_decision").path("pool_availability_guarantee").asText().startsWith("none;"));
        assertEquals(83, manifest.path("days").get(12).path("day").asInt());
        assertEquals("src/test/java/assignments/local/Day83PackagedLockBrowserAcceptanceTest.java",
                manifest.path("days").get(12).path("engineering").path("acceptance_test_path").asText());
        assertTrue(Files.exists(Path.of("src/test/java/assignments/local/Day83PackagedLockBrowserAcceptanceTest.java")));
        assertTrue(Files.exists(Path.of("frontend/e2e/day83-lock-recovery.mjs")));
        String workflow = Files.readString(Path.of(".github/workflows/coverstory-starter.yml"));
        assertTrue(workflow.contains("learning/coverstory-days-71-84"), "pushes to this continuation must run CI");
        String pom = Files.readString(Path.of("pom.xml"));
        assertTrue(pom.contains("<excludeLearnerAcceptance>learnerAcceptance</excludeLearnerAcceptance>"));
        assertTrue(Files.exists(Path.of("src/test/java/assignments/local/LocalPackagedRuntimeBoundaryAcceptanceTest.java")));
        String learnerRuntime = Files.readString(
                Path.of("src/test/java/assignments/local/LocalPackagedRuntimeBoundaryAcceptanceTest.java"));
        assertTrue(learnerRuntime.contains("@Tag(\"learnerAcceptance\")"));
        assertEquals("frontend/src/__tests__/LocalCoverStoryApp.test.tsx",
                manifest.path("days").get(10).path("engineering").path("acceptance_test_path").asText());
        assertEquals("frontend/src/components/RenewalPreviewBusyState.test.tsx",
                manifest.path("days").get(10).path("engineering").path("focused_acceptance_test_path").asText());
        assertEquals("cd frontend && RUN_LEARNER_ACCEPTANCE=true npx vitest run src/components/RenewalPreviewBusyState.test.tsx",
                manifest.path("days").get(10).path("engineering").path("focused_acceptance_command").asText());
        assertEquals("src/test/java/assignments/local/Day83PackagedLockBrowserAcceptanceTest.java",
                manifest.path("days").get(12).path("engineering").path("acceptance_reference").asText());
        assertEquals("instructor_scaffold_complete", manifest.path("days").get(12).path("scaffold_status").asText());
        assertTrue(manifest.path("days").get(12).path("integration_status").asText().startsWith("installed;"));
        assertEquals("src/test/java/assignments/local/Day83PackagedLockBrowserAcceptanceTest.java",
                stories.path("cards").get(12).path("engineering").path("acceptance_reference").asText());
        assertEquals("frontend/src/__tests__/LocalCoverStoryApp.test.tsx",
                stories.path("cards").get(10).path("engineering").path("acceptance_test_path").asText());
        assertEquals("frontend/src/components/RenewalPreviewBusyState.test.tsx",
                stories.path("cards").get(10).path("engineering").path("focused_acceptance_test_path").asText());
        assertEquals(manifest.path("days").get(10).path("engineering").path("focused_acceptance_command").asText(),
                stories.path("cards").get(10).path("engineering").path("focused_acceptance_command").asText());
        assertEquals(manifest.path("days").get(1).path("title").asText(),
                stories.path("cards").get(1).path("title").asText());
        assertEquals("src/test/java/assignments/local/LocalMetricsExposureInfrastructureTest.java",
                manifest.path("days").get(9).path("engineering").path("infrastructure_test_path").asText());
        assertEquals("https://docs.micrometer.io/micrometer/reference/concepts/timers.html",
                manifest.path("days").get(9).path("engineering").path("concept_course")
                        .path("resources").get(0).path("url").asText());
        for (int dayIndex : new int[]{0, 13}) {
            assertTrue(manifest.path("days").get(dayIndex).path("engineering").path("acceptance_test_path").isNull());
            assertTrue(stories.path("cards").get(dayIndex).path("engineering").path("acceptance_test_path").isNull());
        }
    }
}
