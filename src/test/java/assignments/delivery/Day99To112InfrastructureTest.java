package assignments.delivery;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Instructor-owned contract, migration, and continuation wiring checks. */
class Day99To112InfrastructureTest {
    private static final Path BASE = Path.of("docs/reboot/continuation-99-112");

    @Test
    void fourteenDatedContractsAndCourseCardsMatchTheVerifiedPlan() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode manifest = mapper.readTree(Files.readString(BASE.resolve("manifest.json")));
        JsonNode export = mapper.readTree(Files.readString(BASE.resolve("scrum-owner-manifest.json")));
        assertEquals(14, manifest.path("days").size());
        assertEquals(14, export.path("cards").size());
        LocalDate first = LocalDate.parse("2027-01-26");
        for (int index = 0; index < 14; index++) {
            JsonNode day = manifest.path("days").get(index);
            JsonNode card = export.path("cards").get(index);
            assertEquals(99 + index, day.path("day").asInt());
            assertEquals(first.plusDays(index).toString(), day.path("date").asString());
            assertEquals(day.path("title").asString(), card.path("title").asString());
            assertEquals(day.path("engineering").path("concept_course"),
                    card.path("engineering").path("concept_course"));
            assertEquals(60, day.path("java_dsa").path("budget_minutes").asInt());
            String queue = day.path("java_dsa").path("review_queue_command").asString();
            assertTrue(queue.endsWith(first.plusDays(index).toString()));
            assertTrue(day.path("java_dsa").path("problem_id").isNull(), "no new DSA anchor is assigned");
            assertEquals("not_attempted", day.path("learner_status").asString());
            assertEquals(180, day.path("engineering").path("budget_minutes").asInt()
                    + day.path("java_dsa").path("budget_minutes").asInt());
            JsonNode study = day.path("engineering").path("concept_course");
            assertEquals(1500, study.path("video_seconds").asInt()
                    + study.path("worked_reference_seconds").asInt()
                    + study.path("teachback_seconds").asInt());
            String contract = Files.readString(Path.of(day.path("contract_file").asString()));
            for (JsonNode lesson : study.path("lessons")) {
                assertEquals("1341538", lesson.path("course_id").asString());
                assertFalse(lesson.path("source_item_id").asString().isBlank());
                assertFalse(lesson.path("section_id").asString().isBlank());
                assertTrue(contract.contains(lesson.path("title").asString()));
                assertTrue(contract.contains(lesson.path("section_title").asString()));
            }
            for (JsonNode course : study.path("courses")) {
                assertTrue(contract.contains(course.path("title").asString()));
                assertTrue(contract.contains(course.path("url").asString()));
            }
            for (JsonNode reference : study.path("resources")) {
                assertEquals("https", URI.create(reference.path("url").asString()).getScheme());
                assertTrue(contract.contains(reference.path("url").asString()));
            }
        }
    }

    @Test
    void migrationIsAdditiveAndDoesNotTurnPreviewIntoAWriter() throws Exception {
        String migration = Files.readString(Path.of("src/main/resources/db/migration/V004__event_log_delivery_work.sql"));
        assertTrue(migration.contains("REFERENCES training_policy_lifecycle_event"));
        assertTrue(migration.contains("PRIMARY KEY (policy_id, source_version)"));
        assertTrue(migration.contains("event_type = 'ISSUED'"));
        assertFalse(migration.toLowerCase().contains("truncate"));
        String preview = Files.readString(Path.of("src/main/java/assignments/insurance/JdbcPolicyRenewalReadStore.java"));
        assertFalse(preview.contains("training_issuance_work"));
        assertFalse(preview.contains("training_issuance_delivery_receipt"));
        String workflow = Files.readString(Path.of(".github/workflows/coverstory-starter.yml"));
        assertTrue(workflow.contains("learning/coverstory-days-99-112"));
        String app = Files.readString(Path.of("src/main/java/assignments/local/LocalCoverStoryApplication.java"));
        assertTrue(app.contains("coverstory.delivery.run-once"));
        assertTrue(app.contains("WebApplicationType.NONE"), "manual command mode must not bind a web server");
        String command = Files.readString(Path.of("src/main/java/assignments/delivery/LocalIssuanceDeliveryCommand.java"));
        assertTrue(command.contains("containsOption(\"coverstory.delivery.run-once\")"));
        assertTrue(command.contains("BatchDeliveryFailureException") && command.contains("committedProgress()"),
                "one-shot command reports the already committed portion of a stopped batch");
        assertTrue(Files.isRegularFile(Path.of("src/test/java/assignments/delivery/DurableWorkRestartAcceptanceTest.java")));
        assertTrue(Files.isRegularFile(Path.of("src/test/java/assignments/local/LocalPackagedIssuanceDeliveryAcceptanceTest.java")));
        String restart = Files.readString(Path.of("src/test/java/assignments/delivery/DurableWorkRestartAcceptanceTest.java"));
        assertTrue(restart.contains("destroyForcibly()") && restart.contains("DurableDeliveryChildMain.class.getName()")
                && restart.contains("awaitSignal"), "fresh-JVM harness has an explicit boundary and confirms termination");
        String packaged = Files.readString(Path.of("src/test/java/assignments/local/LocalPackagedIssuanceDeliveryAcceptanceTest.java"));
        assertTrue(packaged.contains("LocalPackagedRuntimeProcess.start(postgres, \"2028-01-25T12:00:00Z\")"),
                "renewal preview uses a deterministic fixed clock inside the seeded policy's window");
        assertTrue(packaged.contains("/api/policies"));
        assertTrue(packaged.contains("--coverstory.delivery.run-once"));
    }
}
