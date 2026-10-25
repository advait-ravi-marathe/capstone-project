package assignments.delivery;

import org.junit.jupiter.api.Test;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.sql.SQLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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
        LocalDate first = LocalDate.parse("2027-02-01");
        for (int index = 0; index < 14; index++) {
            JsonNode day = manifest.path("days").get(index);
            JsonNode card = export.path("cards").get(index);
            JsonNode evidence = mapper.readTree(Files.readString(Path.of(
                    day.path("engineering").path("evidence_reference").asString())));
            assertEquals(99 + index, day.path("day").asInt());
            assertEquals(first.plusDays(index).toString(), day.path("date").asString());
            assertEquals(day.path("title").asString(), card.path("title").asString());
            assertEquals(day.path("engineering"), card.path("engineering"),
                    "ticket export must preserve the canonical daily scope, selector, and course block");
            assertEquals("not_attempted", day.path("learner_status").asString());
            assertEquals(120, day.path("engineering").path("budget_minutes").asInt());
            JsonNode study = day.path("engineering").path("concept_course");
            assertEquals(1500, study.path("video_seconds").asInt()
                    + study.path("worked_reference_seconds").asInt()
                    + study.path("teachback_seconds").asInt());
            String contract = Files.readString(Path.of(day.path("contract_file").asString()));
            for (JsonNode command : day.path("engineering").path("acceptance_commands")) {
                assertTrue(contract.contains(command.asString()),
                        "dated contract must publish every canonical runnable acceptance command: " + command.asString());
            }
            for (JsonNode command : day.path("engineering").path("infrastructure_commands")) {
                assertTrue(contract.contains(command.asString()),
                        "dated contract must publish every canonical instructor infrastructure command: " + command.asString());
            }
            assertEquals(day.path("engineering").path("acceptance_commands"), evidence.path("acceptance_commands"),
                    "verification receipt must list the same acceptance commands as the canonical card");
            JsonNode canonicalInfrastructure = day.path("engineering").path("infrastructure_commands");
            if (evidence.has("infrastructure_commands")) {
                assertEquals(canonicalInfrastructure, evidence.path("infrastructure_commands"));
            } else {
                assertEquals(1, canonicalInfrastructure.size(), "legacy receipt form is only valid for one infrastructure command");
                assertEquals(canonicalInfrastructure.get(0), evidence.path("infrastructure_command"));
            }
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
                assertFalse(reference.path("reading_focus").asString().isBlank(),
                        "project-specific guidance is labeled as a reading focus, not an invented document heading");
                assertFalse(reference.has("section"), "do not label project reading guidance as an official source section");
            }
        }
    }

    @Test
    void assistantArchitectureAndInterviewAssignmentsMatchContractsAndExports() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode manifest = mapper.readTree(Files.readString(BASE.resolve("manifest.json")));
        JsonNode export = mapper.readTree(Files.readString(BASE.resolve("scrum-owner-manifest.json")));
        String notes = Files.readString(BASE.resolve("assistant-architecture-notes.md"));
        Path interview = Path.of("docs/reboot/interview");
        Set<String> mockDays = Set.of("101", "104", "108", "111");
        List<String> phases = List.of("Requirements", "Capacity and constraints",
                "Initial architecture and data model", "Bottlenecks, reliability, scaling",
                "Trade-offs, failures, follow-ups");

        assertEquals(14, manifest.path("days").size());
        for (int index = 0; index < 14; index++) {
            JsonNode day = manifest.path("days").get(index);
            JsonNode engineering = day.path("engineering");
            JsonNode owner = export.path("cards").get(index);
            int number = day.path("day").asInt();
            String dayId = Integer.toString(number);
            String anchor = "day-" + dayId;
            String noteRef = "docs/reboot/continuation-99-112/assistant-architecture-notes.md#" + anchor;
            String contract = Files.readString(Path.of(day.path("contract_file").asString()));
            assertEquals(noteRef, engineering.path("assistant_architecture_note").asString());
            assertTrue(notes.contains("<a id=\"" + anchor + "\"></a>"), "missing note anchor " + anchor);
            assertTrue(contract.contains("../assistant-architecture-notes.md#" + anchor));
            assertTrue(owner.path("engineering").path("assistant_architecture_note").asString().equals(noteRef));
            assertTrue(owner.path("course_learning_block").asText().contains("Assistant-provided architecture"));
            assertTrue(owner.path("course_learning_block").asText().contains("No capstone design proposal"));

            JsonNode assignment = engineering.path("interview_assignment");
            if (!mockDays.contains(dayId)) {
                assertTrue(assignment.isMissingNode() || assignment.isNull(), "unexpected mock assignment on Day " + dayId);
                continue;
            }
            assertEquals(assignment, owner.path("engineering").path("interview_assignment"),
                    "owner export must preserve the interview assignment exactly");
            assertEquals(number, assignment.path("day").asInt());
            assertTrue(assignment.path("proposal_before_hints_reference_or_ai").asBoolean());
            assertEquals("system_design_interview_question_only", assignment.path("proposal_scope").asString());
            assertEquals("assistant_supplied", assignment.path("capstone_architecture").asString());
            assertEquals(45, assignment.path("duration_minutes").asInt());
            assertEquals(85, assignment.path("practice_minutes_available").asInt());
            assertEquals(45, assignment.path("mock_minutes").asInt());
            assertEquals(85, assignment.path("practice_total_minutes").asInt());
            assertEquals(40, assignment.path("eai_slice_minutes").path("preconditions_and_prediction").asInt()
                    + assignment.path("eai_slice_minutes").path("implementation_or_observation").asInt()
                    + assignment.path("eai_slice_minutes").path("selector_and_blocker_record").asInt());
            assertEquals(phases.size(), assignment.path("mock_phases").size());
            int phaseTotal = 0;
            for (int phase = 0; phase < phases.size(); phase++) {
                JsonNode actual = assignment.path("mock_phases").get(phase);
                assertEquals(phases.get(phase), actual.path("phase").asString());
                phaseTotal += actual.path("minutes").asInt();
            }
            assertEquals(45, phaseTotal);

            String questionHref = assignment.path("question_href").asString();
            Path questionFile = interview.resolve(questionHref.substring("../interview/".length()).split("#", 2)[0]);
            String questionAnchor = questionHref.substring(questionHref.indexOf('#') + 1);
            String questionText = Files.readString(questionFile);
            assertTrue(questionText.contains("<a id=\"" + questionAnchor + "\"></a>"));
            assertTrue(contract.contains("../../interview/question-bank.md#" + questionAnchor));
            for (JsonNode pattern : assignment.path("pattern_refs")) {
                String href = pattern.asString();
                Path patternFile = interview.resolve(href.substring("../interview/".length()).split("#", 2)[0]);
                String patternAnchor = href.substring(href.indexOf('#') + 1);
                assertTrue(Files.readString(patternFile).contains("<a id=\"" + patternAnchor + "\"></a>"));
                assertTrue(contract.contains("../../interview/pattern-library.md#" + patternAnchor));
            }
            for (JsonNode command : assignment.path("acceptance_commands")) {
                assertTrue(contract.contains(command.asString()));
                assertTrue(engineering.path("acceptance_commands").toString().contains(command.asString()));
            }
            assertTrue(contract.contains("Separate system-design interview practice"));
            assertTrue(contract.contains("Deferred acceptance"));
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
        assertTrue(packaged.contains("Sprint8PackagedRuntimeProcess.start(postgres, \"2028-01-25T12:00:00Z\")"),
                "renewal preview uses a deterministic fixed clock inside the seeded policy's window");
        assertTrue(packaged.contains("/api/policies"));
        assertTrue(packaged.contains("--coverstory.delivery.run-once"));
        assertTrue(packaged.contains("issueReplay") && packaged.contains("policyRows()")
                && packaged.contains("eventRows()") && packaged.contains("idempotencyRows()")
                && packaged.contains("workPayloadRows()") && packaged.contains("receiptPayloadRows()"),
                "packaged replay snapshots compare full source, idempotency, work, receipt, and timestamp payloads");
        assertTrue(Files.isRegularFile(Path.of("src/test/java/assignments/delivery/LocalIssuanceDeliveryCommandTest.java")),
                "the supplied command has fixed invalid/empty/partial result checks");
        String commandChecks = Files.readString(Path.of("src/test/java/assignments/delivery/LocalIssuanceDeliveryCommandTest.java"));
        assertTrue(commandChecks.contains("\"0\"") && commandChecks.contains("\"-1\"")
                && commandChecks.contains("\"21\"") && commandChecks.contains("partialFailureReturnsNonzero"));
        assertTrue(restart.contains("terminateAndAwait(restarted") && restart.contains("terminateAndAwait(child"),
                "both child JVMs are forcibly terminated and waited on every exit path");
        assertTrue(Files.isRegularFile(Path.of("src/test/java/assignments/delivery/DurableIssuanceAcceptanceTest.java")));
        String durableAcceptance = Files.readString(Path.of("src/test/java/assignments/delivery/DurableIssuanceAcceptanceTest.java"));
        assertTrue(durableAcceptance.contains("day101RejectsInvalidCaptureLimitsBeforeChangingRows")
                        && durableAcceptance.contains("day102RejectsInvalidPendingLimitsWithoutChangingRows")
                        && durableAcceptance.contains("day106RejectsOutOfRangeBatchLimitBeforeAnyStoreCall"),
                "actual learner store and dispatcher seams have fixed invalid-limit boundary selectors");
        assertFalse(durableAcceptance
                        .contains("day100SchemaConstraintsRejectDuplicateAndInvalidRows"),
                "Day 100 is instructor-owned migration assessment, not a learner TODO selector");

        int day102Start = durableAcceptance.indexOf("void day102MapsCapturedPendingEnvelopeInStableBoundedOrder");
        int day102End = durableAcceptance.indexOf("\n    @Test", day102Start);
        String day102 = durableAcceptance.substring(day102Start, day102End);
        assertTrue(day102.contains("UPDATE training_issuance_work SET acknowledged_at"),
                "Day 102 fixture seeds pending-row state directly");
        assertFalse(day102.contains(".acceptReceipt("), "Day 102 cannot consume the Day 103 seam");
        assertFalse(day102.contains(".acknowledge("), "Day 102 cannot consume the Day 104 seam");
    }

    @Test
    void sprint8PackagedRuntimeSelectsRawDeliveryAndDay112BuildsEveryRequiredArtifact() throws Exception {
        String app = Files.readString(Path.of("src/main/java/assignments/sprint8/Sprint8LocalApplication.java"));
        String config = Files.readString(Path.of("src/main/java/assignments/sprint8/Sprint8LocalConfiguration.java"));
        String delivery = Files.readString(Path.of("src/main/java/assignments/sprint8/Sprint8IssuanceDeliveryConfiguration.java"));
        String mainLocal = Files.readString(Path.of("src/main/java/assignments/local/LocalRuntimeConfiguration.java"));
        String pom = Files.readString(Path.of("pom.xml"));
        String props = Files.readString(Path.of("src/main/resources/application-sprint8-local.properties"));
        String script = Files.readString(Path.of("scripts/package-sprint8-demo.sh"));
        String helper = Files.readString(Path.of("src/test/java/assignments/local/Sprint8PackagedRuntimeProcess.java"));
        String browser = Files.readString(Path.of("src/test/java/assignments/local/LocalPackagedIssuanceDeliveryAcceptanceTest.java"));
        String wiring = Files.readString(Path.of("src/test/java/assignments/delivery/Sprint8DeliveryRuntimeWiringTest.java"));

        assertTrue(app.contains("@Import(Sprint8LocalConfiguration.class)"));
        assertTrue(config.contains("Sprint8IssuanceDeliveryConfiguration.class"));
        assertTrue(config.contains("LocalPolicyRenewalReadStoreConfiguration.class"),
                "earlier renewal-read prerequisites remain explicit");
        assertFalse(config.contains("LocalRuntimeConfiguration.class"));
        assertFalse(config.contains("RecoveryOnlyRuntimeConfiguration.class"));
        assertFalse(config.contains("ManualRecoveryService.class"));
        assertTrue(delivery.contains("new JdbcIssuanceWorkStore(jdbc)"));
        assertFalse(delivery.contains("RecoveryAwareIssuanceWorkStore"));
        assertTrue(mainLocal.contains("new RecoveryAwareIssuanceWorkStore"),
                "default later runtime remains unchanged");
        assertTrue(props.contains("spring.flyway.target=4"), "Sprint 8 needs V004, but not Day 118 V005");
        assertTrue(pom.contains("<id>sprint8-local</id>")
                && pom.contains("assignments.sprint8.Sprint8LocalApplication")
                && pom.contains("<classifier>sprint8</classifier>"));
        assertTrue(script.contains("-Plocal-app,sprint8-local")
                && script.contains("build_mode=\"incremental\"")
                && script.contains("sourceContentSha256")
                && script.contains("-DskipTests package"),
                "the no-clean option repackages Sprint 8 without deleting the earlier classifier");
        assertTrue(helper.contains("capstone-starter-1.0-SNAPSHOT-sprint8.jar")
                && helper.contains("verifyArtifactReceipt")
                && helper.contains("--spring.profiles.active=local,sprint8-local"));
        assertTrue(browser.contains("Sprint8PackagedRuntimeProcess.start(postgres")
                && browser.contains("/api/session") && browser.contains("/api/policies")
                && browser.contains("/renewal-preview"),
                "Day 111 exercises the packaged session, issue, and preview API paths through Sprint 8");
        assertTrue(wiring.contains("getBeansOfType(RecoveryAwareIssuanceWorkStore.class).isEmpty()"));

        JsonNode manifest = new ObjectMapper().readTree(Files.readString(BASE.resolve("manifest.json")));
        JsonNode commands = manifest.path("days").get(13).path("engineering").path("acceptance_commands");
        int frontend = commandIndex(commands, "(cd frontend && npm ci && npm run build && npm run build:local)");
        int sprint6Package = commandIndex(commands, "scripts/package-sprint6-demo.sh");
        int sprint8Incremental = commandIndex(commands, "scripts/package-sprint8-demo.sh --no-clean");
        int broad = commandIndex(commands, "./mvnw -Plocal-app -DskipTests package");
        assertTrue(frontend < sprint6Package && sprint6Package < sprint8Incremental && sprint8Incremental < broad,
                "Day 112 keeps Sprint 6 and Sprint 8 receipt-bound artifacts after its only clean build");
        String day112Contract = Files.readString(Path.of(manifest.path("days").get(13)
                .path("contract_file").asString()));
        for (JsonNode command : commands) assertTrue(day112Contract.contains(command.asString()));
    }

    private static int commandIndex(JsonNode commands, String exact) {
        for (int index = 0; index < commands.size(); index++) {
            if (exact.equals(commands.get(index).asString())) return index;
        }
        fail("Missing exact package command: " + exact);
        return -1;
    }

    @Test
    void migrationEnforcesStableIdentityAndRequiredFields() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            var dataSource = postgres.getPostgresDatabase();
            Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").target("4").load().migrate();
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            assertEquals(3, countSeedPolicies(jdbc),
                    "the additive migration preserves the three versioned V003 synthetic policies");
            insertSource(jdbc, "schema-a");
            insertSource(jdbc, "schema-b");

            String insertWork = """
                    INSERT INTO training_issuance_work
                    (policy_id,source_version,event_type,effective_date,envelope_schema_version)
                    VALUES (?,1,'ISSUED',DATE '2027-01-30',1)
                    """;
            jdbc.update(insertWork, "schema-a");
            DuplicateKeyException duplicate = assertThrows(DuplicateKeyException.class,
                    () -> jdbc.update(insertWork, "schema-a"));
            assertEquals("23505", sqlState(duplicate), "duplicate source identity violates the primary key");

            DataIntegrityViolationException invalidType = assertThrows(DataIntegrityViolationException.class,
                    () -> jdbc.update("""
                            INSERT INTO training_issuance_work
                            (policy_id,source_version,event_type,effective_date,envelope_schema_version)
                            VALUES ('schema-b',1,'ENDORSED',DATE '2027-01-30',1)
                            """));
            assertEquals("23514", sqlState(invalidType), "a distinct key reaches the ISSUED-only check constraint");

            DataIntegrityViolationException missingRequiredValue = assertThrows(DataIntegrityViolationException.class,
                    () -> jdbc.update("""
                            INSERT INTO training_issuance_work
                            (policy_id,source_version,event_type,effective_date,envelope_schema_version)
                            VALUES ('schema-b',1,'ISSUED',NULL,1)
                            """));
            assertEquals("23502", sqlState(missingRequiredValue), "required event payload fields are not nullable");

            DataIntegrityViolationException missingWork = assertThrows(DataIntegrityViolationException.class,
                    () -> jdbc.update("""
                            INSERT INTO training_issuance_delivery_receipt
                            (policy_id,source_version,event_type,effective_date,envelope_schema_version)
                            VALUES ('missing-policy',1,'ISSUED',DATE '2027-01-30',1)
                            """));
            assertEquals("23503", sqlState(missingWork), "receipt requires its source work row");
            assertEquals(3, countSeedPolicies(jdbc), "constraint probes do not alter prior lifecycle rows");
        }
    }

    private static void insertSource(JdbcTemplate jdbc, String policyId) {
        jdbc.update("""
                INSERT INTO training_policy_lifecycle
                  (policy_id,owner_id,journey_id,quote_version_id,product_version,rate_version,
                   annual_premium_rupees,currency,effective_date,status,version)
                VALUES (?, 'owner-a', ?, ?, 'home-secure-v1', 'training-rate-v1',
                        125.00, 'INR', DATE '2027-01-30', 'ISSUED', 1)
                """, policyId, "journey-" + policyId, "quote-" + policyId);
        jdbc.update("INSERT INTO training_policy_lifecycle_event(policy_id,version,event_type,effective_date) VALUES (?,1,'ISSUED',DATE '2027-01-30')",
                policyId);
    }

    private static int countSeedPolicies(JdbcTemplate jdbc) {
        return jdbc.queryForObject("SELECT count(*) FROM training_policy_lifecycle WHERE policy_id LIKE 'policy-%'", Integer.class);
    }

    private static String sqlState(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql) return sql.getSQLState();
        }
        return null;
    }
}
