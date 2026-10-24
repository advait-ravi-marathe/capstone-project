package assignments.renewal;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Instructor-owned integrity check; this does not implement or grade learner TODOs. */
class Day85To98InfrastructureTest {
    private static final Path BASE = Path.of("docs/reboot/continuation-85-98");

    @Test
    void datedContractsAndIndependentStatusAxesAreInstalled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode manifest = mapper.readTree(Files.readString(BASE.resolve("manifest.json")));
        JsonNode export = mapper.readTree(Files.readString(BASE.resolve("scrum-owner-manifest.json")));
        assertEquals(14, manifest.path("days").size());
        assertEquals(14, export.path("cards").size());
        LocalDate first = LocalDate.parse("2027-01-16");
        for (int i = 0; i < 14; i++) {
            JsonNode day = manifest.path("days").get(i);
            JsonNode card = export.path("cards").get(i);
            String contract = Files.readString(Path.of(day.path("contract_file").asString()));
            assertEquals(85 + i, day.path("day").asInt());
            assertEquals(first.plusDays(i).toString(), day.path("date").asString());
            assertEquals("not_attempted", day.path("learner_status").asString());
            assertEquals(120, day.path("engineering").path("budget_minutes").asInt());
            assertTrue(day.path("java_dsa").path("budget_minutes").isNull());
            assertEquals(60, day.path("java_dsa").path("budget_minutes_by_exercise_count").path("1_or_2_substantive_exercises").asInt());
            assertEquals(75, day.path("java_dsa").path("budget_minutes_by_exercise_count").path("3_substantive_exercises").asInt());
            assertEquals(90, day.path("java_dsa").path("budget_minutes_by_exercise_count").path("4_substantive_exercises").asInt());
            assertEquals(180, manifest.path("daily_budget_minutes").path("total_by_dsa_tier_minutes").path("1_or_2").asInt());
            assertEquals(195, manifest.path("daily_budget_minutes").path("total_by_dsa_tier_minutes").path("3").asInt());
            assertEquals(210, manifest.path("daily_budget_minutes").path("total_by_dsa_tier_minutes").path("4").asInt());
            assertEquals(85, day.path("engineering").path("allocation_minutes").path("implementation_and_testing").asInt());
            assertEquals(25, day.path("engineering").path("allocation_minutes").path("concept_or_course").asInt());
            assertEquals(10, day.path("engineering").path("allocation_minutes").path("evidence").asInt());
            assertEquals(day.path("title").asString(), card.path("title").asString());
            assertEquals(day.path("date").asString(), card.path("date").asString());
            assertEquals(day.path("engineering").path("concept_course"), card.path("engineering").path("concept_course"));
            assertEquals(25, day.path("engineering").path("concept_course").path("minutes").asInt());
            JsonNode learning = day.path("engineering").path("concept_course");
            assertEquals(1500, learning.path("video_seconds").asInt()
                    + learning.path("worked_reference_seconds").asInt()
                    + learning.path("teachback_seconds").asInt());
            int assignedVideoSeconds = 0;
            for (JsonNode lesson : learning.path("lessons")) {
                assignedVideoSeconds += lesson.path("duration_seconds").asInt();
                assertTrue(contract.contains(lesson.path("title").asString()));
                assertTrue(contract.contains(lesson.path("section_title").asString()));
            }
            assertEquals(learning.path("video_seconds").asInt(), assignedVideoSeconds,
                    "assigned lesson duration must match the study budget");
            for (JsonNode course : learning.path("courses")) {
                assertTrue(contract.contains(course.path("title").asString()));
                assertTrue(contract.contains(course.path("url").asString()));
            }
            for (JsonNode ref : learning.path("resources")) assertTrue(contract.contains(ref.path("url").asString()));
            assertTrue(Files.isRegularFile(Path.of(day.path("contract_file").asString())));
            assertTrue(Files.isRegularFile(Path.of(day.path("engineering").path("infrastructure_test_path").asString())));
            for (JsonNode ref : day.path("engineering").path("concept_course").path("resources")) {
                assertEquals("https", URI.create(ref.path("url").asString()).getScheme());
                assertFalse(ref.path("section").asText().isBlank());
            }
            if (!day.path("engineering").path("acceptance_test_path").isNull()) {
                assertTrue(Files.isRegularFile(Path.of(day.path("engineering").path("acceptance_test_path").asString())));
            }
        }
    }

    @Test
    void runtimeLimitsAndBrowserBootstrapArePreserved() throws Exception {
        JsonNode manifest = new ObjectMapper().readTree(Files.readString(BASE.resolve("manifest.json")));
        assertEquals(2, manifest.path("capacity_decision").path("application_permits").asInt());
        assertEquals(4, manifest.path("capacity_decision").path("hikari_maximum_pool_size").asInt());
        assertEquals(1000, manifest.path("capacity_decision").path("connection_timeout_millis").asInt());
        String workflow = Files.readString(Path.of(".github/workflows/coverstory-starter.yml"));
        assertTrue(workflow.contains("learning/coverstory-days-85-98"));
        String readme = Files.readString(Path.of("README.md"));
        assertTrue(readme.contains("npm ci"));
        assertTrue(readme.contains("CHROMIUM_BIN=/usr/bin/chromium"));
        assertTrue(Files.readString(Path.of("frontend/package.json")).contains("\"playwright\": \"1.64.1\""));
        String day83 = Files.readString(Path.of("src/test/java/assignments/local/Day83PackagedLockBrowserAcceptanceTest.java"));
        assertTrue(day83.contains("120000") && day83.contains("180000"),
                "the historical success-under-lock test needs its explicit finite test-only budget");
        assertTrue(Files.isRegularFile(Path.of("src/test/java/assignments/renewal/PreviewTimeoutPostgresAcceptanceTest.java")),
                "short-budget lock/statement cases remain separate learner acceptance checks");
        String day97 = Files.readString(Path.of("src/test/java/assignments/local/Day97PackagedTimeoutBrowserAcceptanceTest.java"));
        assertTrue(day97.contains("LocalPackagedRuntimeProcess.start(postgres, \"2027-01-01T12:00:00Z\")"),
                "the seeded 2026-01-15 policy must be preview-eligible in the packaged retry test");
    }

    @Test
    void day96RecoverySelectorCoversLockRollbackAndIdleBackend() throws Exception {
        String test = Files.readString(Path.of("src/test/java/assignments/renewal/PreviewTimeoutPostgresAcceptanceTest.java"));
        String contract = Files.readString(BASE.resolve("contracts/day-96.md"));
        String scope = "success, then hold a real ACCESS EXCLUSIVE relation lock while the one-slot reader fails; " +
                "prove rollback and idle state before releasing the holder";
        assertTrue(contract.contains(scope));
        assertTrue(test.contains("void repeatedRecoveryRestoresSessionBaselineBeforePermitReuse()"));
        assertTrue(test.contains("LOCK TABLE training_policy_lifecycle IN ACCESS EXCLUSIVE MODE"));
        assertTrue(test.contains("fixture.awaitWait("));
        assertTrue(test.contains("assertEquals(\"55P03\", sqlState(lockFailure))"));
        assertTrue(test.contains("assertEquals(\"idle\", lockCleaned.state())"));
        assertTrue(test.contains("assertFalse(lockCleaned.transactionOpen())"));
        assertTrue(test.contains("assertEquals(\"idle\", activity.state()"));
        assertTrue(test.contains("assertFalse(activity.transactionOpen()"));
    }
}
