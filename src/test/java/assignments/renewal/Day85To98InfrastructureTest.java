package assignments.renewal;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Set;

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
        assertTrue(day83.contains("Sprint6PackagedRuntimeProcess.start(postgres, CLOCK)"),
                "Day 83 uses the bounded Sprint 6 archive, which excludes later timed-read budgets");
        assertFalse(day83.contains("120000") || day83.contains("180000"),
                "Day 83 must not depend on Day 86+ read-budget configuration");
        assertTrue(Files.isRegularFile(Path.of("src/test/java/assignments/renewal/PreviewTimeoutPostgresAcceptanceTest.java")),
                "short-budget lock/statement cases remain separate learner acceptance checks");
        String day97 = Files.readString(Path.of("src/test/java/assignments/local/Day97PackagedTimeoutBrowserAcceptanceTest.java"));
        assertTrue(day97.contains("LocalPackagedRuntimeProcess.start(postgres, \"2027-01-01T12:00:00Z\")"),
                "the seeded 2026-01-15 policy must be preview-eligible in the packaged retry test");
        String processHelper = Files.readString(Path.of("src/test/java/assignments/local/LocalPackagedRuntimeProcess.java"));
        String packageScript = Files.readString(Path.of("scripts/package-local-demo.sh"));
        assertTrue(processHelper.contains("List.of(java, \"-jar\", jar.toString()"),
                "Day 97 launches an executable archive in a separate process");
        assertTrue(packageScript.contains("-Plocal-app -DskipTests clean package"),
                "Day 97 packaging starts from a clean frontend-bearing local archive");
        assertTrue(day97.contains("snapshot(holder)"), "the real browser journey snapshots all three lifecycle tables");
        assertTrue(day97.contains("holder.commit()"), "the lock is released only after the blocked result is observed");
        assertFalse(day97.contains("@SpringBootTest"), "packaged behavior is not inferred from an in-process context");
        String sharedStore = Files.readString(Path.of("src/main/java/assignments/local/LocalPolicyRenewalReadStoreConfiguration.java"));
        String sprint6Config = Files.readString(Path.of("src/main/java/assignments/sprint6/Sprint6LocalConfiguration.java"));
        String localApp = Files.readString(Path.of("src/main/java/assignments/local/LocalCoverStoryApplication.java"));
        assertEquals(1, occurrences(sharedStore, "LEARNER TODO Day 79"));
        assertTrue(sprint6Config.contains("LocalPolicyRenewalReadStoreConfiguration.class"));
        assertTrue(localApp.contains("scanBasePackages = \"assignments.local\""));
        assertFalse(packageScript.contains("sprint6-local"), "Day 97 transitions to the full local runtime");
    }

    @Test
    void architectureNotesInterviewOnlySessionsAndRenderedGuidanceStaySynchronized() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode manifest = mapper.readTree(Files.readString(BASE.resolve("manifest.json")));
        JsonNode export = mapper.readTree(Files.readString(BASE.resolve("scrum-owner-manifest.json")));
        String notes = Files.readString(BASE.resolve("assistant-architecture-notes.md"));
        String questionBank = Files.readString(Path.of("docs/reboot/interview/question-bank.md"));
        Set<Integer> mockDays = Set.of(87, 90, 94, 97);

        assertEquals(14, manifest.path("days").size());
        assertEquals(14, export.path("cards").size());
        for (int i = 0; i < 14; i++) {
            JsonNode day = manifest.path("days").get(i);
            JsonNode card = export.path("cards").get(i);
            int number = 85 + i;
            String notePath = day.path("engineering").path("assistant_architecture_note").asText();
            assertEquals(notePath, card.path("engineering").path("assistant_architecture_note").asText());
            assertEquals("docs/reboot/continuation-85-98/assistant-architecture-notes.md#day-" + number, notePath);
            assertTrue(notes.contains("<a id=\"day-" + number + "\"></a>"), "missing supplied architecture note for Day " + number);

            String contract = Files.readString(Path.of(day.path("contract_file").asText()));
            assertTrue(contract.contains("Assistant-provided architecture (what and why)"), "Day " + number);
            assertFalse(contract.contains("**Sketch prompt:**"), "capstone design proposals are not assigned on Day " + number);
            assertTrue(contract.contains("no capstone design proposal or post-hoc justification is assigned"));
            String learningBlock = card.path("course_learning_block").asText();
            assertTrue(learningBlock.contains("assistant-architecture-notes.md#day-" + number));
            assertTrue(learningBlock.contains("no capstone design proposal or post-hoc justification is assigned"));
            assertFalse(learningBlock.contains("Sketch retained policy context"));
            assertFalse(learningBlockContainsCapstoneDesignPrompt(learningBlock),
                    "rendered guidance must not ask the learner to design the capstone on Day " + number);
            assertTrue(contract.contains("Assistant-provided architecture (what and why)"));

            JsonNode canonicalAssignment = day.path("engineering").path("interview_assignment");
            JsonNode ownerAssignment = card.path("engineering").path("interview_assignment");
            if (mockDays.contains(number)) {
                assertEquals(canonicalAssignment, ownerAssignment, "mock assignment export differs on Day " + number);
                assertEquals("45_minute_mock", canonicalAssignment.path("mode").asText());
                assertEquals(45, canonicalAssignment.path("duration_minutes").asInt());
                assertEquals(85, canonicalAssignment.path("practice_total_minutes").asInt());
                assertEquals(45, canonicalAssignment.path("mock_minutes").asInt());
                assertEquals("system_design_interview_question_only", canonicalAssignment.path("proposal_scope").asText());
                assertEquals("assistant_supplied", canonicalAssignment.path("capstone_architecture").asText());
                assertEquals(notePath, canonicalAssignment.path("assistant_architecture_note").asText());
                assertTrue(questionBank.contains("<a id=\"" + canonicalAssignment.path("question_id").asText().toLowerCase() + "\"></a>"));
                assertTrue(canonicalAssignment.path("deferred_work_id").asText().startsWith("S7-D" + number + "-"));
                assertFalse(canonicalAssignment.path("deferred_work").asText().isBlank());
                assertInterviewAssignmentMatchesContract(contract, canonicalAssignment, number);
                int phases = 0;
                for (JsonNode phase : canonicalAssignment.path("mock_phases")) phases += phase.path("minutes").asInt();
                assertEquals(45, phases);
                JsonNode slice = canonicalAssignment.path("eai_slice_minutes");
                assertEquals(40, slice.path("preconditions_and_prediction").asInt()
                        + slice.path("implementation_or_observation").asInt()
                        + slice.path("selector_and_blocker_record").asInt());
                assertEquals(day.path("engineering").path("acceptance_commands"),
                        canonicalAssignment.path("acceptance_commands"));
            } else {
                assertTrue(canonicalAssignment.isMissingNode(), "unexpected interview homework on Day " + number);
                assertTrue(ownerAssignment.isMissingNode(), "unexpected owner export interview homework on Day " + number);
            }
        }
        assertTrue(mockDays.contains(87) && mockDays.contains(90) && mockDays.contains(94) && mockDays.contains(97));
    }

    private static boolean learningBlockContainsCapstoneDesignPrompt(String block) {
        return block.contains("Sketch prompt:") || block.contains("Draw admission")
                || block.contains("Propose the capstone") || block.contains("design the capstone");
    }

    private static void assertInterviewAssignmentMatchesContract(String contract, JsonNode assignment, int day) {
        String heading = "### Separate system-design interview practice — 45 minutes of the 85-minute practice block";
        int start = contract.indexOf(heading);
        assertTrue(start >= 0, "missing separate interview assignment in Day " + day + " contract");
        int end = contract.indexOf("### Scope and safety limits", start);
        assertTrue(end > start, "interview assignment must finish before implementation safety scope");
        String section = contract.substring(start, end);
        assertEquals(-1, contract.indexOf(heading, start + heading.length()),
                "duplicate interview/practice schedule on Day " + day);
        assertTrue(section.contains("separate interview exercise, not a capstone design assignment"));
        assertTrue(section.contains("before using hints, references, or AI feedback"));
        String questionId = assignment.path("question_id").asText();
        String questionTitle = assignment.path("question_title").asText();
        String questionHref = assignment.path("question_href").asText().replace("../interview/", "../../interview/");
        assertTrue(section.contains("[" + questionId + " — " + questionTitle + "](" + questionHref + ")"));
        assertTrue(section.contains(assignment.path("proposal_prompt").asText()));
        assertTrue(section.contains("These 45 minutes replace part of the existing 85-minute practice block"));
        assertTrue(section.contains("This replaces, and does not supplement, the full practice allocation"));
        assertTrue(section.contains("Mock phases (45 minutes total)"));
        assertTrue(section.contains("Remaining engineering slice (40 minutes total)"));
        assertFalse(section.contains("55 minutes") || section.contains("60 minutes"),
                "an earlier full implementation allocation must not stack with the mock on Day " + day);
        assertTrue(section.contains(assignment.path("bounded_task").asText()));
        assertTrue(section.contains("`" + assignment.path("deferred_work_id").asText() + "`"));
        assertTrue(section.contains(assignment.path("deferred_work").asText()));
        for (JsonNode command : assignment.path("acceptance_commands")) {
            assertTrue(section.contains("`" + command.asText() + "`"), "missing runnable command on Day " + day);
        }
        for (JsonNode phase : assignment.path("mock_phases")) {
            assertTrue(section.contains(phase.path("minutes").asText() + " minutes "
                    + phase.path("phase").asText()), "mock phase mismatch on Day " + day);
        }
        for (JsonNode pattern : assignment.path("pattern_refs")) {
            String href = pattern.asText().replace("../interview/", "../../interview/");
            assertTrue(section.contains("(" + href + ")"), "missing interview pattern on Day " + day);
        }
    }

    @Test
    void day92HttpAcceptanceAndDay97FreshArchiveCommandsMatchInstalledHarnesses() throws Exception {
        JsonNode manifest = new ObjectMapper().readTree(Files.readString(BASE.resolve("manifest.json")));
        JsonNode day92 = manifest.path("days").get(7).path("engineering");
        String mapperTests = Files.readString(Path.of("src/test/java/assignments/renewal/TimedRenewalReadAcceptanceTest.java"));
        assertTrue(day92.path("acceptance_commands").toString()
                .contains("translatedStateMapsToSafe503WithoutLeakingSqlDetails"));
        assertTrue(mapperTests.contains("void translatedStateMapsToSafe503WithoutLeakingSqlDetails()"));
        assertTrue(mapperTests.contains("PREVIEW_UNAVAILABLE"));
        assertTrue(mapperTests.contains("containsString(\"57014\")"));

        JsonNode day97 = manifest.path("days").get(12).path("engineering");
        assertTrue(day97.path("acceptance_commands").get(0).asText().contains("scripts/package-local-demo.sh"));
        assertTrue(day97.path("acceptance_commands").get(1).asText().contains("Day97PackagedTimeoutBrowserAcceptanceTest"));
        assertFalse(day97.path("acceptance_commands").toString().contains("-DskipTests package"),
                "a stale archive must not satisfy the packaged browser selector");
    }

    @Test
    void day88DirectSettingsSelectorAndDay79RuntimeSeamAreIsolated() throws Exception {
        JsonNode canonical = new ObjectMapper().readTree(Files.readString(BASE.resolve("manifest.json")));
        JsonNode owner = new ObjectMapper().readTree(Files.readString(BASE.resolve("scrum-owner-manifest.json")));
        JsonNode day88 = canonical.path("days").get(3).path("engineering");
        JsonNode owner88 = owner.path("cards").get(3).path("engineering");
        assertEquals(day88.path("acceptance_test_path"), owner88.path("acceptance_test_path"));
        assertEquals(day88.path("acceptance_commands"), owner88.path("acceptance_commands"));
        assertTrue(day88.path("acceptance_commands").get(0).asText()
                .contains("PgLocalReadBudgetsAcceptanceTest#appliesSettingsInsideOwnedTransactionAndRejectsCallsOutsideIt"));
        String directTest = Files.readString(Path.of(day88.path("acceptance_test_path").asText()));
        assertTrue(directTest.contains("PgLocalReadBudgets.apply"));
        assertTrue(directTest.contains("assertThrows(IllegalStateException.class"));
        assertFalse(directTest.contains("TimedPolicyRenewalReadStore"),
                "Day 88 must not need Day 89 orchestration to test the direct settings seam");

        String shared = Files.readString(Path.of("src/main/java/assignments/local/LocalPolicyRenewalReadStoreConfiguration.java"));
        String sprint6 = Files.readString(Path.of("src/main/java/assignments/sprint6/Sprint6LocalConfiguration.java"));
        String localRuntime = Files.readString(Path.of("src/main/java/assignments/local/LocalRuntimeConfiguration.java"));
        String application = Files.readString(Path.of("src/main/java/assignments/local/LocalCoverStoryApplication.java"));
        assertEquals(1, occurrences(shared, "LEARNER TODO Day 79"));
        assertTrue(sprint6.contains("LocalPolicyRenewalReadStoreConfiguration.class"));
        assertFalse(sprint6.contains("LEARNER TODO Day 79"));
        assertFalse(localRuntime.contains("LEARNER TODO Day 79"));
        assertTrue(application.contains("scanBasePackages = \"assignments.local\""),
                "the normal local package discovers the one shared Day 79 configuration");
    }

    @Test
    void day96PackagesAndChecksTheDay97RuntimeBeforeTheBrowserAcceptanceDay() throws Exception {
        JsonNode manifest = new ObjectMapper().readTree(Files.readString(BASE.resolve("manifest.json")));
        JsonNode owner = new ObjectMapper().readTree(Files.readString(BASE.resolve("scrum-owner-manifest.json")));
        JsonNode day96 = manifest.path("days").get(11);
        JsonNode owner96 = owner.path("cards").get(11);
        String packageCheck = "./scripts/package-local-demo.sh && unzip -p target/capstone-starter-1.0-SNAPSHOT.jar META-INF/MANIFEST.MF | tr -d '\\r' | grep -Fx 'Start-Class: assignments.local.LocalCoverStoryApplication'";
        assertEquals(day96.path("engineering").path("acceptance_commands"),
                owner96.path("engineering").path("acceptance_commands"));
        assertTrue(day96.path("engineering").path("acceptance_commands").toString().contains(packageCheck));
        String contract = Files.readString(Path.of(day96.path("contract_file").asText()));
        assertTrue(contract.contains("25 minutes inside today's practice block"));
        assertTrue(contract.contains("60 minutes for the one-slot outcome test"));
        assertTrue(contract.contains("This is part of the existing 85-minute practice allocation"));
        assertTrue(contract.contains(packageCheck));
        assertTrue(packageCheck.contains(" | tr -d '\\r' | grep -Fx 'Start-Class: assignments.local.LocalCoverStoryApplication'"),
                "manifest matching removes CRLF carriage returns before exact Start-Class comparison");
        Process crlfCheck = new ProcessBuilder("bash", "-c",
                "printf 'Start-Class: assignments.local.LocalCoverStoryApplication\\r\\n' " +
                        "| tr -d '\\r' | grep -Fx 'Start-Class: assignments.local.LocalCoverStoryApplication'")
                .redirectErrorStream(true).start();
        assertEquals(0, crlfCheck.waitFor(), "the documented exact match must accept a CRLF manifest line");
        assertTrue(contract.contains("single Day 79 seam"));
        assertTrue(Files.readString(Path.of("scripts/package-local-demo.sh"))
                .contains("-Plocal-app -DskipTests clean package"));
    }

    private static int occurrences(String text, String needle) {
        int count = 0;
        for (int at = 0; (at = text.indexOf(needle, at)) >= 0; at += needle.length()) count++;
        return count;
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
