package assignments.reboot;

import assignments.insurance.*;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Infrastructure-only Chromium -> Spring MVC -> disposable PostgreSQL -> server restart smoke. */
class Day44BrowserTransportInfrastructureTest {
    private static volatile DataSource postgresDataSource;
    private static volatile DatabaseWire browserFailureDatabaseSnapshot;
    private static final HarnessClock lifecycleClock = new HarnessClock(Instant.parse("2026-12-01T12:00:00Z"));

    @BeforeEach void resetLifecycleClock() { lifecycleClock.set(Instant.parse("2026-12-01T12:00:00Z")); }

    @Test void realBrowserRoundTripSurvivesSpringServerRestart() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            postgresDataSource = postgres.getPostgresDatabase();
            try (Connection c = postgresDataSource.getConnection(); Statement s = c.createStatement()) {
                s.execute("CREATE TABLE harness_readback (witness_id TEXT PRIMARY KEY, value TEXT NOT NULL)");
            }
            Path base = Path.of("target/day44-harness").toAbsolutePath();
            Files.createDirectories(base);
            Path writeJson = base.resolve("write.json");
            Path readJson = base.resolve("read.json");
            int firstPort;
            try (ConfigurableApplicationContext first = startServer(HarnessConfiguration.class)) {
                firstPort = port(first);
                runBrowser("http://127.0.0.1:" + firstPort + "/__e2e/day44-lifecycle", "write", writeJson);
                assertTrue(Files.readString(writeJson).contains("lifecycle-smoke-v1:persisted-after-server-restart"));
            }
            try (ConfigurableApplicationContext restarted = startServer(HarnessConfiguration.class)) {
                assertNotEquals(firstPort, port(restarted), "test should start a fresh Spring server instance");
                runBrowser("http://127.0.0.1:" + port(restarted) + "/__e2e/day44-lifecycle", "read", readJson);
                assertTrue(Files.readString(readJson).contains("lifecycle-smoke-v1:persisted-after-server-restart"));
            }
            try (Connection c = postgresDataSource.getConnection(); Statement s = c.createStatement();
                 var rows = s.executeQuery("SELECT count(*) FROM harness_readback WHERE witness_id='lifecycle-smoke-v1'")) {
                assertTrue(rows.next()); assertEquals(1, rows.getInt(1));
            }
        }
    }

    @Test @Tag("learnerAcceptance")
    void realBrowserDrivesPolicyLifecycleApiThroughSpringAndPostgres() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            postgresDataSource = postgres.getPostgresDatabase();
            initializeLifecycleSchema(postgresDataSource);
            try (Connection c = postgresDataSource.getConnection(); Statement s = c.createStatement()) {
                s.execute("CREATE TABLE harness_readback (witness_id TEXT PRIMARY KEY, value TEXT NOT NULL)");
            }
            Path result = Path.of("target/day44-harness/lifecycle-flow.json").toAbsolutePath();
            try (ConfigurableApplicationContext app = startServer(LearnerHarnessConfiguration.class)) {
                runBrowser("http://127.0.0.1:" + port(app) + "/__e2e/day44-lifecycle", "flow", result);
            }
            var json = new tools.jackson.databind.ObjectMapper().readTree(Files.readString(result));
            var flow = json.get("flow").asString();
            assertTrue(json.get("browserOpened").asBoolean());
            Path restartResult = Path.of("target/day44-harness/lifecycle-restart.json").toAbsolutePath();
            try (ConfigurableApplicationContext restarted = startServer(LearnerHarnessConfiguration.class)) {
                runBrowser("http://127.0.0.1:" + port(restarted) + "/__e2e/day44-lifecycle", "lifecycle-read", restartResult);
            }
            String restart = new tools.jackson.databind.ObjectMapper().readTree(Files.readString(restartResult)).get("lifecycleReadback").asString();
            for (String route : List.of("issue", "replay", "endorsement", "cancellation", "history"))
                assertTrue(flow.contains("/test/day44/lifecycle/" + route + ":200"), "Expected successful browser/API call for " + route + "; got " + flow);
            assertEquals("/test/day44/lifecycle/history:200:CANCELLED:v4:events4", restart);
        }
    }

    @Test @Tag("learnerAcceptance")
    void realBrowserObservesServicingFailureRecoveryAndRestart() throws Throwable {
        var mapper = new tools.jackson.databind.ObjectMapper();
        browserFailureDatabaseSnapshot = null;
        var frozenExpectations = mapper.readTree(Files.readString(Path.of("docs/reboot/continuation-31-44/exercises/servicing-uat.expected.json")));
        assertFrozenExpectedCriteria(frozenExpectations);
        String runId = java.util.UUID.randomUUID().toString();
        Path runDir = Path.of("target/day44-harness/attempts", runId).toAbsolutePath();
        Files.createDirectories(runDir);
        String sourceSha = gitValue("rev-parse", "HEAD");
        String workingTreePatchSha = sha256(gitBytes("diff", "--binary", "HEAD"));
        boolean workingTreeDirty = !gitValue("status", "--porcelain").isBlank();
        String expectedHash = sha256(Path.of("docs/reboot/continuation-31-44/exercises/servicing-uat.expected.json"));
        String command = "./mvnw -Plearner-tests -Dtest=Day44BrowserTransportInfrastructureTest#realBrowserObservesServicingFailureRecoveryAndRestart test";
        String cwd = Path.of("").toAbsolutePath().toString();
        var records = new java.util.LinkedHashMap<String, tools.jackson.databind.node.ObjectNode>();
        for (String id : List.of("UAT-SVC-01", "UAT-SVC-02", "UAT-SVC-03"))
            records.put(id, newObservation(mapper, runId, id, sourceSha, workingTreePatchSha, workingTreeDirty, expectedHash, command, cwd));
        String activeScenario = null;
        Path activeBrowserResult = null;
        DatabaseWire lastObservedDatabase = null;
        String started03 = null;
        Path beforeRestartRun = runDir.resolve("uat-svc-03-before-restart.json");
        tools.jackson.databind.JsonNode beforeRestart = null;
        DatabaseWire beforeRestartDatabase = null;
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            postgresDataSource = postgres.getPostgresDatabase(); initializeLifecycleSchema(postgresDataSource);
            try (Connection c=postgresDataSource.getConnection(); Statement s=c.createStatement()) {
                s.execute("CREATE TABLE harness_readback (witness_id TEXT PRIMARY KEY, value TEXT NOT NULL)");
            }
            try (ConfigurableApplicationContext app=startServer(LearnerHarnessConfiguration.class)) {
                activeScenario = "UAT-SVC-01";
                var started01 = java.time.Instant.now().toString();
                Path issueRun = runDir.resolve("uat-svc-01-issued.json");
                activeBrowserResult = issueRun;
                runBrowser("http://127.0.0.1:"+port(app)+"/__e2e/day44-lifecycle","servicing",issueRun);
                var issueObservation = mapper.readTree(Files.readString(issueRun));
                assertIssueReplayAndEndorsementObservation(issueObservation);
                DatabaseWire afterIssueAndEndorsement = readDatabaseWire(postgresDataSource);
                lastObservedDatabase = afterIssueAndEndorsement;
                assertDatabase(afterIssueAndEndorsement, 1, 1, 2, "ISSUED", 2, "125.00", null,
                        List.of(new HistoryWire(2,"2026-12-02","REBUILD_AMOUNT")));
                finishObservation(records.get(activeScenario), mapper, started01, "passed", null, 0, issueObservation,
                        afterIssueAndEndorsement, List.of(issueRun));
                activeScenario = null;
                activeBrowserResult = null;

                activeScenario = "UAT-SVC-02";
                var started02 = java.time.Instant.now().toString();
                DatabaseWire beforeReadFailure = readDatabaseWire(postgresDataSource);
                lastObservedDatabase = beforeReadFailure;
                Path failureRun = runDir.resolve("uat-svc-02-failure-recovery.json");
                activeBrowserResult = failureRun;
                runBrowser("http://127.0.0.1:"+port(app)+"/__e2e/day44-lifecycle","servicing-failure",failureRun);
                var failureObservation = mapper.readTree(Files.readString(failureRun));
                assertFailureRecoveryObservation(failureObservation);
                DatabaseWire afterReadRecovery = readDatabaseWire(postgresDataSource);
                lastObservedDatabase = afterReadRecovery;
                assertEquals(beforeReadFailure, afterReadRecovery, "failed and recovered reads must not mutate persisted state");
                finishObservation(records.get(activeScenario), mapper, started02, "passed", null, 0, failureObservation,
                        Map.of("before", beforeReadFailure, "after", afterReadRecovery), List.of(failureRun));
                activeScenario = null;
                activeBrowserResult = null;

                activeScenario = "UAT-SVC-03";
                started03 = java.time.Instant.now().toString();
                activeBrowserResult = beforeRestartRun;
                runBrowser("http://127.0.0.1:"+port(app)+"/__e2e/day44-lifecycle","servicing-cancel",beforeRestartRun);
                beforeRestart = mapper.readTree(Files.readString(beforeRestartRun));
                assertCancelledSummaryObservation(beforeRestart);
                beforeRestartDatabase = readDatabaseWire(postgresDataSource);
                lastObservedDatabase = beforeRestartDatabase;
                assertDatabase(beforeRestartDatabase, 1, 1, 4, "CANCELLED", 4, "125.00", "2026-12-10",
                        List.of(new HistoryWire(2,"2026-12-02","REBUILD_AMOUNT")));
                records.get(activeScenario).put("actualStartedAt", started03);
                records.get(activeScenario).set("observedHttp", beforeRestart.get("http").deepCopy());
                records.get(activeScenario).set("observedUI", mapper.createObjectNode().set("beforeRestart", beforeRestart.get("summary").deepCopy()));
                records.get(activeScenario).set("independentlyObservedDatabase", mapper.createObjectNode().set("beforeRestart", mapper.valueToTree(beforeRestartDatabase)));
                records.get(activeScenario).putArray("screenshotPaths").add(beforeRestartRun.resolveSibling(beforeRestartRun.getFileName().toString().replace(".json", ".png")).toString());
                records.get(activeScenario).putArray("logPaths").add(browserLog(beforeRestartRun).toString());
                records.get(activeScenario).putArray("restartPhases").add("before restart: running Spring context and first Chromium process; persisted CANCELLED summary captured");
            }
            try (ConfigurableApplicationContext restarted=startServer(LearnerHarnessConfiguration.class)) {
                activeScenario = "UAT-SVC-03";
                Path afterRestartRun = runDir.resolve("uat-svc-03-after-restart.json");
                activeBrowserResult = afterRestartRun;
                runBrowser("http://127.0.0.1:"+port(restarted)+"/__e2e/day44-lifecycle","servicing-read",afterRestartRun);
                var afterRestart = mapper.readTree(Files.readString(afterRestartRun));
                assertCancelledSummaryObservation(afterRestart);
                assertEquals(mapper.readTree(Files.readString(runDir.resolve("uat-svc-03-before-restart.json"))).get("summary"), afterRestart.get("summary"),
                        "fresh Spring and browser must return every same servicing field");
                assertEquals("CANCELLED:v4:events4", historyString(afterRestart.get("history")));
                DatabaseWire afterRestartDatabase = readDatabaseWire(postgresDataSource);
                lastObservedDatabase = afterRestartDatabase;
                assertDatabase(afterRestartDatabase, 1, 1, 4, "CANCELLED", 4, "125.00", "2026-12-10",
                        List.of(new HistoryWire(2,"2026-12-02","REBUILD_AMOUNT")));
                assertEquals(beforeRestartDatabase, afterRestartDatabase,
                        "restart must preserve independently observed complete database state");
                var combined = mapper.createObjectNode();
                combined.set("beforeRestart", mapper.readTree(Files.readString(beforeRestartRun)));
                combined.set("afterRestart", afterRestart);
                var combinedHttp=mapper.createArrayNode();
                for(var response:beforeRestart.get("http"))combinedHttp.add(response.deepCopy());
                for(var response:afterRestart.get("http"))combinedHttp.add(response.deepCopy());
                combined.set("http",combinedHttp);
                combined.set("summary", mapper.createObjectNode().set("beforeRestart", beforeRestart.get("summary").deepCopy()).set("afterRestart", afterRestart.get("summary").deepCopy()));
                combined.set("history", afterRestart.get("history").deepCopy());
                combined.set("browserInvocations", mapper.createArrayNode()
                        .add(browserInvocation("before-restart", beforeRestart))
                        .add(browserInvocation("after-restart", afterRestart)));
                finishObservation(records.get(activeScenario), mapper, started03, "passed", null, 0,
                        combined, Map.of("beforeRestart", beforeRestartDatabase, "afterRestart", afterRestartDatabase), List.of(beforeRestartRun, afterRestartRun));
                records.get(activeScenario).putArray("restartPhases").add("fresh Spring context; fresh Playwright browser process; same disposable PostgreSQL; no reseed or writes");
                activeScenario = null;
                activeBrowserResult = null;
            }
        } catch (Throwable failure) {
            String blocker = failure.getClass().getSimpleName()+": "+String.valueOf(failure.getMessage());
            if (activeScenario != null) {
                var current = records.get(activeScenario);
                current.put("result", "failed"); current.put("blocker", blocker); current.put("actualFinishedAt", java.time.Instant.now().toString());
                int exitCode = failure instanceof BrowserRunFailure browserFailure ? browserFailure.exitCode() : 1;
                current.put("exitCode", exitCode);
                if (activeBrowserResult != null && Files.exists(activeBrowserResult)) {
                    var partial = mapper.readTree(Files.readString(activeBrowserResult));
                    copyPartialActual(current, mapper, partial);
                    if (activeScenario.equals("UAT-SVC-03") && beforeRestart != null)
                        current.set("observedUI", mapper.createObjectNode().set("beforeRestart", beforeRestart.get("summary").deepCopy()).set("partialAfterRestart", partial.get("panelText") == null ? mapper.nullNode() : partial.get("panelText").deepCopy()));
                }
                if (current.get("independentlyObservedDatabase").isNull() && lastObservedDatabase != null)
                    current.set("independentlyObservedDatabase", mapper.valueToTree(lastObservedDatabase));
                if (current.get("independentlyObservedDatabase").isNull() && postgresDataSource != null) {
                    try {
                        current.set("independentlyObservedDatabase", mapper.valueToTree(readDatabaseWire(postgresDataSource)));
                    } catch (Exception captureFailure) {
                        current.put("databaseCaptureFailure", captureFailure.getClass().getSimpleName()+": "+String.valueOf(captureFailure.getMessage()));
                    }
                }
                var failureDatabase = mapper.createObjectNode();
                if (current.get("independentlyObservedDatabase") != null && current.get("independentlyObservedDatabase").isObject())
                    failureDatabase.setAll((tools.jackson.databind.node.ObjectNode) current.get("independentlyObservedDatabase"));
                if (activeScenario.equals("UAT-SVC-03") && beforeRestartDatabase != null)
                    failureDatabase.set("beforeRestart", mapper.valueToTree(beforeRestartDatabase));
                if (postgresDataSource != null) {
                    try {
                        current.set("independentlyObservedDatabaseAtFailure", mapper.valueToTree(readDatabaseWire(postgresDataSource)));
                    } catch (Exception captureFailure) {
                        current.put("databaseCaptureFailure", captureFailure.getClass().getSimpleName()+": "+String.valueOf(captureFailure.getMessage()));
                    }
                }
                if (browserFailureDatabaseSnapshot != null)
                    current.set("independentlyObservedDatabaseAtBrowserFailure", mapper.valueToTree(browserFailureDatabaseSnapshot));
                if (!failureDatabase.isEmpty()) current.set("independentlyObservedDatabase", failureDatabase);
                current.putArray("logPaths").add(activeBrowserResult == null ? "" : browserLog(activeBrowserResult).toString());
            }
            for (var entry : records.entrySet()) if ("not_run".equals(entry.getValue().get("result").asString())) {
                entry.getValue().put("result", "blocked"); entry.getValue().put("blocker", blocker); entry.getValue().put("actualFinishedAt", java.time.Instant.now().toString());
            }
            throw failure;
        } finally {
            for (var entry : records.entrySet()) {
                appendExistingArtifacts(entry.getValue(), runDir);
                Path output = runDir.resolve(entry.getKey()+".json");
                Files.writeString(output, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(entry.getValue()));
            }
        }
    }

    private static void assertFrozenExpectedCriteria(tools.jackson.databind.JsonNode expected) {
        String canonical = """
                {
                  "schema_version": 1,
                  "criteria_version": "fde-coursework-v2",
                  "scenario_kind": "synthetic-local-only",
                  "cases": [
                    {
                      "scenarioId": "UAT-SVC-01",
                      "inputs": ["existing browser issue route", "same-instance replay", "REBUILD_AMOUNT endorsement"],
                      "steps": ["run browser flow", "GET persisted owner-scoped summary", "inspect PostgreSQL rows"],
                      "expectedUI": ["policy-a", "home-secure-v1", "125.00 INR", "ISSUED", "Version 2", "REBUILD_AMOUNT"],
                      "expectedPersistedState": {"policies": 1, "receipts": 1, "events": 2},
                      "failureInjection": null,
                      "recoveryStep": null
                    },
                    {
                      "scenarioId": "UAT-SVC-02",
                      "inputs": ["existing successful summary", "one deliberately aborted summary GET"],
                      "steps": ["observe summary", "abort exactly one GET", "confirm stale values clear", "remove interception", "retry the same GET"],
                      "expectedUI": ["unavailable alert", "old policy/premium/history absent", "recovered persisted values", "alert absent"],
                      "expectedHttp": null,
                      "expectedPersistedState": {"policyVersionUnchanged": true, "rowCountsUnchanged": true},
                      "failureInjection": "network abort; no HTTP status",
                      "recoveryStep": "real GET after removing interception"
                    },
                    {
                      "scenarioId": "UAT-SVC-03",
                      "inputs": ["existing endorsement", "same disposable PostgreSQL across Spring restart"],
                      "steps": ["apply existing cancellation route", "GET actual summary", "stop Spring", "start fresh Spring and browser", "GET summary/history only"],
                      "expectedUI": ["CANCELLED", "Version 4", "125.00 INR", "2026-12-10", "persisted endorsement"],
                      "expectedPersistedState": {"policies": 1, "receipts": 1, "events": 4},
                      "failureInjection": null,
                      "recoveryStep": null,
                      "limitation": "Spring-process restart persistence only; not a PostgreSQL crash/restart test"
                    }
                  ]
                }
                """;
        assertEquals(new tools.jackson.databind.ObjectMapper().readTree(canonical), expected,
                "frozen UAT criteria changed; update the browser/database assertions in the same reviewed change");
    }

    private static void copyPartialActual(tools.jackson.databind.node.ObjectNode record, tools.jackson.databind.ObjectMapper mapper,
            tools.jackson.databind.JsonNode partial) {
        for (String source : List.of("http", "observedNetworkFailures")) if (partial.has(source)) record.set(source.equals("http") ? "observedHttp" : "observedNetworkFailure", partial.get(source).deepCopy());
        var ui = mapper.createObjectNode();
        for (String field : List.of("panelText", "error", "phase", "actualArgv", "actualCwd", "failedPanelText", "summary", "recovered", "successfulReads", "history", "issueBody", "replayBody"))
            if (partial.has(field)) ui.set(field, partial.get(field).deepCopy());
        record.set("observedUI", ui);
        var database=mapper.createObjectNode();
        for(String field:List.of("afterIssue","afterReplay","replayCheckpoint","afterEndorsement","before","after","database"))
            if(partial.has(field))database.set(field,partial.get(field).deepCopy());
        if(!database.isEmpty())record.set("independentlyObservedDatabase",database);
        if (partial.has("screenshots")) for (var screenshot : partial.get("screenshots")) record.withArray("screenshotPaths").add(screenshot.asString());
        if (partial.has("actualArgv")) record.set("actualArgv", partial.get("actualArgv").deepCopy());
        if (partial.has("actualCwd")) record.set("actualCwd", partial.get("actualCwd").deepCopy());
    }

    private static tools.jackson.databind.node.ObjectNode newObservation(tools.jackson.databind.ObjectMapper mapper, String runId,
            String scenarioId, String sourceSha, String workingTreePatchSha, boolean workingTreeDirty, String expectedHash, String command, String cwd) {
        var record=mapper.createObjectNode();
        record.put("schema_version",1);record.put("result","not_run");record.put("runId",runId);record.put("scenarioId",scenarioId);
        record.put("expectedVersion",1);record.put("criteriaVersion","fde-coursework-v2");record.put("expectedHash",expectedHash);
        record.put("recordType","instructor-validation-only");record.put("workingTreeDirty",workingTreeDirty);record.put("workingTreePatchSha256",workingTreePatchSha);
        record.put("sourceSha",sourceSha);record.put("actualStartedAt",java.time.Instant.now().toString());record.putNull("actualFinishedAt");
        record.put("canonicalCommand",command);record.put("command",command);record.put("cwd",cwd);record.putNull("exitCode");record.putNull("actualArgv");record.putNull("actualCwd");record.putArray("observedHttp");
        record.putNull("observedNetworkFailure");record.putNull("observedUI");record.putArray("screenshotPaths");record.putArray("logPaths");
        record.putNull("independentlyObservedDatabase");record.putArray("restartPhases");record.putNull("blocker");record.putNull("reviewerNote");
        return record;
    }

    private static void finishObservation(tools.jackson.databind.node.ObjectNode record, tools.jackson.databind.ObjectMapper mapper,
            String startedAt, String result, String blocker, Integer exitCode, tools.jackson.databind.JsonNode actual,
            Object databaseObservation, List<Path> browserRuns) {
        record.put("actualStartedAt",startedAt);record.put("actualFinishedAt",java.time.Instant.now().toString());record.put("result",result);
        if(exitCode==null)record.putNull("exitCode");else record.put("exitCode",exitCode);
        if(blocker==null)record.putNull("blocker");else record.put("blocker",blocker);
        var http=actual.get("http");
        if(http==null)http=actual.get("successfulReads");
        if(http==null)record.putArray("observedHttp");else record.set("observedHttp",http.deepCopy());
        if(actual.has("actualArgv"))record.set("actualArgv",actual.get("actualArgv").deepCopy());
        if(actual.has("actualCwd"))record.set("actualCwd",actual.get("actualCwd").deepCopy());
        if(actual.has("browserInvocations"))record.set("browserInvocations",actual.get("browserInvocations").deepCopy());
        var network=actual.get("observedNetworkFailure");
        if(network==null)record.putNull("observedNetworkFailure");else record.set("observedNetworkFailure",network.deepCopy());
        var ui=mapper.createObjectNode();
        for(String field:List.of("summary","panelText","failedPanelText","recovered","history"))if(actual.has(field))ui.set(field,actual.get(field).deepCopy());
        if(actual.has("beforeRestart")&&actual.has("afterRestart")) {
            ui.set("beforeRestartSummary",actual.get("beforeRestart").get("summary").deepCopy());
            ui.set("afterRestartSummary",actual.get("afterRestart").get("summary").deepCopy());
        }
        record.set("observedUI",ui);
        record.set("independentlyObservedDatabase",mapper.valueToTree(databaseObservation));
        for(Path run:browserRuns) {
            Path screenshot=run.resolveSibling(run.getFileName().toString().replace(".json",".png"));
            if(Files.exists(screenshot))record.withArray("screenshotPaths").add(screenshot.toString());
            Path log=browserLog(run);if(Files.exists(log))record.withArray("logPaths").add(log.toString());
        }
    }

    private static tools.jackson.databind.node.ObjectNode browserInvocation(String phase, tools.jackson.databind.JsonNode actual) {
        var invocation = new tools.jackson.databind.ObjectMapper().createObjectNode();
        invocation.put("phase", phase);
        if (actual.has("actualArgv")) invocation.set("actualArgv", actual.get("actualArgv").deepCopy());
        if (actual.has("actualCwd")) invocation.set("actualCwd", actual.get("actualCwd").deepCopy());
        return invocation;
    }

    private static void appendExistingArtifacts(tools.jackson.databind.node.ObjectNode record, Path runDir) throws Exception {
        String prefix=record.get("scenarioId").asString().toLowerCase();
        try(var paths=Files.list(runDir)) {
            for(Path path:paths.toList()) {
                String name=path.getFileName().toString();
                if(!name.toLowerCase().startsWith(prefix))continue;
                if(name.endsWith(".png")&&!contains(record.withArray("screenshotPaths"),path.toString()))record.withArray("screenshotPaths").add(path.toString());
                if(name.endsWith(".log")&&!contains(record.withArray("logPaths"),path.toString()))record.withArray("logPaths").add(path.toString());
            }
        }
    }

    private static boolean contains(tools.jackson.databind.JsonNode array,String value) {
        for(var element:array)if(value.equals(element.asString()))return true;
        return false;
    }

    private static void assertIssueReplayAndEndorsementObservation(tools.jackson.databind.JsonNode observation) {
        var http=observation.get("http");
        var issue=findHttp(http,"/issue");var replay=findHttp(http,"/replay");var endorsement=findHttp(http,"/endorsement");var summary=findHttp(http,"/servicing/policy-a");
        assertEquals(200,issue.get("status").asInt());assertEquals(200,replay.get("status").asInt());assertEquals(issue.get("body"),replay.get("body"),"replay response must be the exact original snapshot");
        assertEquals(observation.get("issueBody"), observation.get("replayBody"), "captured command bodies must prove replay identity");
        assertEquals(observation.get("afterIssue"), observation.get("afterReplay"), "full policy/receipt/event rows must remain identical on replay");
        var body=issue.get("body");
        assertEquals("policy-a",body.get("policyId").asString());assertEquals("owner-a",body.get("ownerId").asString());
        assertEquals("journey-a",body.get("journeyId").asString());assertEquals("quote-a",body.get("quoteVersionId").asString());
        assertEquals("home-secure-v1",body.get("productVersion").asString());assertEquals("training-rate-v1",body.get("rateVersion").asString());
        assertEquals("125",body.get("annualPremiumRupees").asString());assertEquals("2026-12-01",body.get("effectiveDate").asString());
        assertEquals("ISSUED",body.get("status").asString());assertEquals(1,body.get("version").asInt());
        assertEquals(200,endorsement.get("status").asInt());
        assertDatabaseJson(observation.get("afterIssue"),1,1,1,"ISSUED",1,"125.00",null,List.of());
        assertDatabaseJson(observation.get("afterReplay"),1,1,1,"ISSUED",1,"125.00",null,List.of());
        assertDatabaseJson(observation.get("afterEndorsement"),1,1,2,"ISSUED",2,"125.00",null,List.of(new HistoryWire(2,"2026-12-02","REBUILD_AMOUNT")));
        assertSummaryJson(summary.get("body"),"ISSUED",2,null,List.of(new HistoryWire(2,"2026-12-02","REBUILD_AMOUNT")));
        String panel=observation.get("panelText").asString();
        for(String value:List.of("policy-a","home-secure-v1","125.00 INR","ISSUED","Version 2","REBUILD_AMOUNT","2026-12-02"))assertTrue(panel.contains(value),"panel lacks "+value);
        assertFalse(panel.contains("owner-a")||panel.contains("journey-a")||panel.contains("quote-a")||panel.contains("training-rate-v1"),"private source identifiers leaked into panel");
    }

    private static void assertFailureRecoveryObservation(tools.jackson.databind.JsonNode observation) {
        assertTrue(observation.get("abortedRequestStatus").isNull(),"aborted request has no HTTP status");
        assertTrue(observation.get("observedNetworkFailure").get("failure").asString().length()>0);
        assertEquals("/test/day44/lifecycle/servicing/policy-a",observation.get("observedNetworkFailure").get("path").asString());
        assertEquals(observation.get("before"),observation.get("after"),"database state is unchanged across failed GET and retry");
        var reads=observation.get("successfulReads");assertEquals(2,reads.size());
        for(var read:reads)assertEquals(200,read.get("status").asInt());
        assertSummaryJson(reads.get(1).get("body"),"ISSUED",2,null,List.of(new HistoryWire(2,"2026-12-02","REBUILD_AMOUNT")));
        String failed=observation.get("failedPanelText").asString();assertTrue(failed.contains("Policy unavailable"));
        assertFalse(failed.contains("policy-a")||failed.contains("125.00 INR")||failed.contains("REBUILD_AMOUNT"),"failed panel retained stale policy data");
        String recovered=observation.get("panelText").asString();
        for(String value:List.of("policy-a","125.00 INR","ISSUED","Version 2","REBUILD_AMOUNT","2026-12-02"))assertTrue(recovered.contains(value));
        assertFalse(recovered.contains("Policy unavailable"),"recovery retained stale error");
    }

    private static void assertCancelledSummaryObservation(tools.jackson.databind.JsonNode observation) {
        var summary=findHttp(observation.get("http"),"/servicing/policy-a");
        assertSummaryJson(summary.get("body"),"CANCELLED",4,"2026-12-10",List.of(new HistoryWire(2,"2026-12-02","REBUILD_AMOUNT")));
        assertDatabaseJson(observation.get("database"),1,1,4,"CANCELLED",4,"125.00","2026-12-10",List.of(new HistoryWire(2,"2026-12-02","REBUILD_AMOUNT")));
        String panel=observation.get("panelText").asString();
        for(String value:List.of("policy-a","home-secure-v1","125.00 INR","CANCELLED","Version 4","REBUILD_AMOUNT","2026-12-02","2026-12-10"))assertTrue(panel.contains(value));
        assertFalse(panel.contains("owner-a")||panel.contains("journey-a")||panel.contains("quote-a")||panel.contains("training-rate-v1"));
        assertEquals("CANCELLED:v4:events4",historyString(observation.get("history")));
    }

    private static tools.jackson.databind.JsonNode findHttp(tools.jackson.databind.JsonNode http,String suffix) {
        for(var item:http)if(item.get("path").asString().endsWith(suffix))return item;
        fail("missing HTTP observation for "+suffix);return null;
    }

    private static void assertSummaryJson(tools.jackson.databind.JsonNode summary,String status,int version,String cancellation,List<HistoryWire> history) {
        assertNotNull(summary);assertEquals("policy-a",summary.get("policyId").asString());assertEquals("home-secure-v1",summary.get("productVersion").asString());
        assertEquals("125.00",summary.get("annualPremiumRupees").asString());assertEquals("INR",summary.get("currency").asString());
        assertEquals(status,summary.get("status").asString());assertEquals(version,summary.get("version").asInt());
        if(cancellation==null)assertTrue(summary.get("cancellationEffectiveDate").isNull());else assertEquals(cancellation,summary.get("cancellationEffectiveDate").asString());
        assertEquals(history.size(),summary.get("history").size());
        for(int i=0;i<history.size();i++) {
            var actual=summary.get("history").get(i);var expected=history.get(i);
            assertEquals(expected.version(),actual.get("version").asLong());assertEquals(expected.effectiveDate(),actual.get("effectiveDate").asString());assertEquals(expected.changeCode(),actual.get("changeCode").asString());
        }
        assertEquals(8,summary.size(),"summary must contain exactly the eight allowlisted projection fields");
    }

    private static void assertDatabaseJson(tools.jackson.databind.JsonNode db,long policies,long receipts,long events,String status,long version,String premium,String cancellation,List<HistoryWire> history) {
        assertEquals(policies,db.get("policies").asLong());assertEquals(receipts,db.get("receipts").asLong());assertEquals(events,db.get("events").asLong());
        assertEquals(policies, db.get("policyRows").size(), "all policy rows must be captured");
        assertEquals(receipts, db.get("receiptRows").size(), "all receipt rows must be captured");
        assertEquals(events, db.get("eventRows").size(), "all event rows must be captured");
        if(status==null){assertTrue(db.get("version").isNull());return;}
        assertEquals("home-secure-v1",db.get("productVersion").asString());assertEquals(premium,db.get("annualPremiumRupees").asString());assertEquals("INR",db.get("currency").asString());
        assertEquals(status,db.get("status").asString());assertEquals(version,db.get("version").asLong());
        if(cancellation==null)assertTrue(db.get("cancellationEffectiveDate").isNull());else assertEquals(cancellation,db.get("cancellationEffectiveDate").asString());
        var policy=db.get("policyRows").get(0);
        assertEquals("policy-a",policy.get("policyId").asString());assertEquals("owner-a",policy.get("ownerId").asString());
        assertEquals("journey-a",policy.get("journeyId").asString());assertEquals("quote-a",policy.get("quoteVersionId").asString());
        assertEquals("home-secure-v1",policy.get("productVersion").asString());assertEquals("training-rate-v1",policy.get("rateVersion").asString());
        assertEquals(premium,policy.get("annualPremiumRupees").asString());assertEquals("INR",policy.get("currency").asString());
        assertEquals("2026-12-01",policy.get("effectiveDate").asString());assertEquals(status,policy.get("status").asString());assertEquals(version,policy.get("version").asLong());
        if(cancellation==null)assertTrue(policy.get("cancellationEffectiveDate").isNull());else assertEquals(cancellation,policy.get("cancellationEffectiveDate").asString());
        var receipt=db.get("receiptRows").get(0);
        assertEquals("owner-a",receipt.get("ownerId").asString());assertEquals("issue-e2e",receipt.get("clientRequestId").asString());
        assertFalse(receipt.get("requestFingerprint").asString().isBlank(),"receipt fingerprint must be present");assertEquals("policy-a",receipt.get("policyId").asString());
        var eventsActual=db.get("eventRows");
        assertEvent(eventsActual.get(0),1,"ISSUED","2026-12-01",null,null);
        if(events>=2)assertEvent(eventsActual.get(1),2,"ENDORSED","2026-12-02","REBUILD_AMOUNT","200000.00");
        if(events>=3)assertEvent(eventsActual.get(2),3,"CANCEL_SCHEDULED","2026-12-10","CUSTOMER_REQUEST",null);
        if(events>=4)assertEvent(eventsActual.get(3),4,"CANCELLED","2026-12-10",null,null);
        assertEquals(history.size(),db.get("endorsementHistory").size());
        for(int i=0;i<history.size();i++) {
            var actual=db.get("endorsementHistory").get(i);var expected=history.get(i);
            assertEquals(expected.version(),actual.get("version").asLong());assertEquals(expected.effectiveDate(),actual.get("effectiveDate").asString());assertEquals(expected.changeCode(),actual.get("changeCode").asString());
        }
    }

    private static void assertEvent(tools.jackson.databind.JsonNode event,long version,String type,String date,String changeCode,String rebuildAmount) {
        assertEquals("policy-a",event.get("policyId").asString());assertEquals(version,event.get("version").asLong());
        assertEquals(type,event.get("eventType").asString());assertEquals(date,event.get("effectiveDate").asString());
        if(changeCode==null)assertTrue(event.get("changeCode").isNull());else assertEquals(changeCode,event.get("changeCode").asString());
        if(rebuildAmount==null)assertTrue(event.get("trainingRebuildAmountRupees").isNull());else assertEquals(rebuildAmount,event.get("trainingRebuildAmountRupees").asString());
    }

    private static void assertDatabase(DatabaseWire db,long policies,long receipts,long events,String status,long version,String premium,String cancellation,List<HistoryWire> history) {
        assertDatabaseJson(new tools.jackson.databind.ObjectMapper().valueToTree(db),policies,receipts,events,status,version,premium,cancellation,history);
    }

    private static String historyString(tools.jackson.databind.JsonNode history) {
        return history.get("status").asString()+":v"+history.get("version").asInt()+":events"+history.get("eventCount").asInt();
    }

    private static DatabaseWire readDatabaseWire(DataSource ds) throws Exception {
        var policyRows=new java.util.ArrayList<PolicyRow>();
        var receiptRows=new java.util.ArrayList<ReceiptRow>();
        var eventRows=new java.util.ArrayList<EventRow>();
        try(Connection c=ds.getConnection();var q=c.createStatement();var r=q.executeQuery("SELECT policy_id,owner_id,journey_id,quote_version_id,product_version,rate_version,annual_premium_rupees,currency,effective_date,status,version,cancellation_effective_date FROM training_policy_lifecycle ORDER BY policy_id")) {
            while(r.next()) {
                var cancellation=r.getDate(12);
                policyRows.add(new PolicyRow(r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6),r.getBigDecimal(7).toPlainString(),r.getString(8).trim(),r.getDate(9).toLocalDate().toString(),r.getString(10),r.getLong(11),cancellation==null?null:cancellation.toLocalDate().toString()));
            }
        }
        try(Connection c=ds.getConnection();var q=c.createStatement();var r=q.executeQuery("SELECT owner_id,client_request_id,request_fingerprint,policy_id FROM training_policy_idempotency ORDER BY owner_id,client_request_id")) {
            while(r.next())receiptRows.add(new ReceiptRow(r.getString(1),r.getString(2),r.getString(3),r.getString(4)));
        }
        try(Connection c=ds.getConnection();var q=c.createStatement();var r=q.executeQuery("SELECT policy_id,version,event_type,effective_date,change_code,training_rebuild_amount_rupees FROM training_policy_lifecycle_event ORDER BY policy_id,version")) {
            while(r.next()) {
                var amount=r.getBigDecimal(6);
                eventRows.add(new EventRow(r.getString(1),r.getLong(2),r.getString(3),r.getDate(4).toLocalDate().toString(),r.getString(5),amount==null?null:amount.toPlainString()));
            }
        }
        PolicyRow policy=policyRows.stream().filter(p->p.policyId().equals("policy-a")&&p.ownerId().equals("owner-a")).findFirst().orElse(null);
        var endorsementHistory=eventRows.stream().filter(e->e.policyId().equals("policy-a")&&e.eventType().equals("ENDORSED"))
                .map(e->new HistoryWire(e.version(),e.effectiveDate(),e.changeCode())).toList();
        return new DatabaseWire(policyRows.size(),receiptRows.size(),eventRows.size(),policy==null?null:policy.productVersion(),policy==null?null:policy.annualPremiumRupees(),policy==null?null:policy.currency(),policy==null?null:policy.status(),policy==null?null:policy.version(),policy==null?null:policy.cancellationEffectiveDate(),endorsementHistory,List.copyOf(policyRows),List.copyOf(receiptRows),List.copyOf(eventRows));
    }

    private static String gitValue(String... args) throws Exception {
        return new String(gitBytes(args),java.nio.charset.StandardCharsets.UTF_8).trim();
    }

    private static byte[] gitBytes(String... args) throws Exception {
        var command=new java.util.ArrayList<String>();command.add("git");command.addAll(List.of(args));
        var process=new ProcessBuilder(command).redirectErrorStream(true).start();
        byte[] result=process.getInputStream().readAllBytes();
        if(process.waitFor()!=0)throw new IllegalStateException("git metadata failed: "+new String(result,java.nio.charset.StandardCharsets.UTF_8));return result;
    }

    private static String sha256(Path file) throws Exception {
        byte[] digest=java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file));
        return java.util.HexFormat.of().formatHex(digest);
    }

    private static String sha256(byte[] content) throws Exception {
        byte[] digest=java.security.MessageDigest.getInstance("SHA-256").digest(content);
        return java.util.HexFormat.of().formatHex(digest);
    }

    private static Path browserLog(Path result) {
        String name=result.getFileName().toString().replaceFirst("\\.json$","");
        return result.resolveSibling(name+".log");
    }

    private static ConfigurableApplicationContext startServer(Class<?> harnessConfiguration) {
        return new SpringApplicationBuilder(assignments.insurance.InsuranceDayOneApplication.class, harnessConfiguration)
                .web(WebApplicationType.SERVLET)
                .properties("server.port=0", "spring.main.banner-mode=off", "logging.level.root=ERROR")
                .run();
    }

    private static long count(DataSource ds,String table)throws Exception { try(Connection c=ds.getConnection();Statement s=c.createStatement();var r=s.executeQuery("SELECT count(*) FROM "+table)){r.next();return r.getLong(1);} }

    private static void initializeLifecycleSchema(DataSource dataSource) throws Exception {
        for (String sql : Files.readString(Path.of("src/test/resources/fixtures/continuation-31-44/policy-lifecycle.sql")).split(";"))
            if (!sql.isBlank()) try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) { s.execute(sql); }
    }

    private static int port(ConfigurableApplicationContext context) {
        return ((WebServerApplicationContext) context).getWebServer().getPort();
    }

    private static void runBrowser(String url, String phase, Path result) throws Exception {
        Files.createDirectories(result.toAbsolutePath().getParent());
        Path log=browserLog(result);Files.createDirectories(log.toAbsolutePath().getParent());
        ProcessBuilder process = new ProcessBuilder("node", "e2e/day44-browser.mjs", url, phase, result.toString())
                .directory(Path.of("frontend").toFile()).redirectErrorStream(true)
                .redirectOutput(log.toFile());
        String chromium = System.getenv("CHROMIUM_BIN");
        if (chromium != null && !chromium.isBlank()) process.environment().put("CHROMIUM_BIN", chromium);
        Process runner = process.start();
        if (!runner.waitFor(45, TimeUnit.SECONDS)) {
            runner.destroyForcibly(); runner.waitFor();
            throw new IllegalStateException("Playwright timed out: " + Files.readString(log));
        }
        if (runner.exitValue() != 0) {
            if (postgresDataSource != null) try { browserFailureDatabaseSnapshot = readDatabaseWire(postgresDataSource); }
            catch (Exception ignored) { browserFailureDatabaseSnapshot = null; }
            throw new BrowserRunFailure(runner.exitValue(), result, log, Files.readString(log));
        }
    }

    static final class BrowserRunFailure extends RuntimeException {
        private final int exitCode;
        BrowserRunFailure(int exitCode,Path resultPath,Path logPath,String output) {
            super("Playwright exited "+exitCode+"; result="+resultPath+"; log="+logPath+"\n"+output);
            this.exitCode=exitCode;
        }
        int exitCode(){return exitCode;}
    }

    @Configuration(proxyBeanMethods = false)
    static class HarnessConfiguration implements WebMvcConfigurer {
        @Override public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/assets/**")
                    .addResourceLocations(Path.of("frontend/dist/assets").toAbsolutePath().toUri().toString());
        }
        @Bean DataSource harnessDataSource() { return postgresDataSource; }
        @Bean PolicyLifecycleService policyLifecycleService() { return new PolicyLifecycleService(lifecycleClock); }
        @Bean Day44HarnessController day44HarnessController(DataSource dataSource, PolicyLifecycleService service) { return new Day44HarnessController(dataSource, service); }
        @Bean Day44SmokePageController day44SmokePageController(DataSource dataSource) { return new Day44SmokePageController(dataSource); }
    }

    @Configuration(proxyBeanMethods = false)
    static class LearnerHarnessConfiguration implements WebMvcConfigurer {
        @Override public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/assets/**")
                    .addResourceLocations(Path.of("frontend/dist/assets").toAbsolutePath().toUri().toString());
        }
        @Bean DataSource harnessDataSource() { return postgresDataSource; }
        @Bean PolicyLifecycleService policyLifecycleService() { return new PolicyLifecycleService(lifecycleClock); }
        @Bean Day44HarnessController day44HarnessController(DataSource dataSource, PolicyLifecycleService service) { return new Day44HarnessController(dataSource, service); }
        @Bean Day44LearnerPageController day44LearnerPageController() { return new Day44LearnerPageController(); }
    }

    @RestController
    static class Day44SmokePageController {
        private final DataSource dataSource;
        Day44SmokePageController(DataSource dataSource) { this.dataSource = dataSource; }

        @GetMapping(value = "/__e2e/day44-lifecycle", produces = MediaType.TEXT_HTML_VALUE)
        String page() { return """
                <!doctype html><html><body>
                <button type="button" id="write">Write synthetic witness</button>
                <button type="button" id="read">Read witness</button>
                <p role="status" id="status"></p><output data-testid="readback"></output>
                <script>
                  async function readback(){const r=await fetch('/test/day44/readback');const x=await r.json();document.querySelector('[data-testid=readback]').textContent=x.witnessId+':'+x.value;}
                  document.querySelector('#write').onclick=async()=>{await fetch('/test/day44/record',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({witnessId:'lifecycle-smoke-v1',value:'persisted-after-server-restart'})});document.querySelector('#status').textContent='saved';await readback();};
                  document.querySelector('#read').onclick=readback;
                </script></body></html>
                """; }
    }

    @RestController
    static class Day44LearnerPageController {
        @GetMapping(value = "/__e2e/day44-lifecycle", produces = MediaType.TEXT_HTML_VALUE)
        String page() throws Exception { return Files.readString(Path.of("frontend/dist/index.html")); }
    }

    @RestController
    static class Day44HarnessController {
        private final DataSource dataSource;
        private final PolicyLifecycleService service;
        Day44HarnessController(DataSource dataSource, PolicyLifecycleService service) { this.dataSource = dataSource; this.service = service; }

        @PostMapping(value = "/test/day44/record", consumes = MediaType.APPLICATION_JSON_VALUE)
        void write(@RequestBody Witness witness) throws Exception {
            try (Connection c = dataSource.getConnection(); var statement = c.prepareStatement(
                    "INSERT INTO harness_readback(witness_id,value) VALUES (?,?) ON CONFLICT(witness_id) DO UPDATE SET value=EXCLUDED.value")) {
                statement.setString(1, witness.witnessId()); statement.setString(2, witness.value()); statement.executeUpdate();
            }
        }

        @GetMapping(value = "/test/day44/readback", produces = MediaType.APPLICATION_JSON_VALUE)
        Witness read() throws Exception {
            try (Connection c = dataSource.getConnection(); var statement = c.prepareStatement(
                    "SELECT witness_id,value FROM harness_readback WHERE witness_id='lifecycle-smoke-v1'");
                 var rows = statement.executeQuery()) {
                return rows.next() ? new Witness(rows.getString(1), rows.getString(2)) : new Witness("", "");
            }
        }

        @GetMapping("/test/day44/lifecycle/servicing/{policyId}")
        ServicingWire servicing(@PathVariable("policyId") String policyId) throws Exception {
            Optional<PolicySnapshot> found = service.readForOwner(new JdbcLifecycleReadStore(dataSource), "owner-a", policyId);
            if (found.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
            PolicyServicingView view=service.toServicingView(found.get());
            return new ServicingWire(view.policyId(),view.productVersion(),view.annualPremiumRupees().toPlainString(),view.currency(),view.status(),view.version(),
                    view.history().stream().map(e->new HistoryWire(e.version(),e.effectiveDate().toString(),e.changeCode())).toList(),
                    view.cancellationEffectiveDate()==null?null:view.cancellationEffectiveDate().toString());
        }

        @GetMapping("/test/day44/lifecycle/diagnostics")
        DatabaseWire diagnostics() throws Exception { return readDatabaseWire(dataSource); }

        @PostMapping("/test/day44/lifecycle/issue")
        PolicySnapshot issue() {
            var request = request();
            var facts = new PolicyIssueFacts("owner-a", "owner-a", "journey-a", "quote-a", true, true, LocalDate.parse("2026-12-15"), true);
            if (!service.validateIssue(request, facts)) throw new IllegalArgumentException("quote failed fixed training facts");
            return service.persistIssueAtomically(dataSource, request, "owner-a");
        }

        @PostMapping("/test/day44/lifecycle/replay")
        PolicySnapshot replay() { return service.issue(request(), "owner-a"); }

        @PostMapping("/test/day44/lifecycle/endorsement")
        PolicySnapshot endorse() {
            var policy=issued();
            var request=new PolicyEndorsementRequest(1,LocalDate.parse("2026-12-02"),"REBUILD_AMOUNT",new BigDecimal("200000.00"));
            if(!service.validateEndorsement(policy,request))throw new IllegalArgumentException("fixed training endorsement failed validation");
            return service.endorseAtExpectedVersion(dataSource,policy,request);
        }

        @GetMapping("/test/day44/lifecycle/history")
        LifecycleReadback history() throws Exception {
            service.verifyLifecycleReadback(dataSource, "policy-a");
            try (Connection c=dataSource.getConnection(); var q=c.prepareStatement("SELECT p.status,p.version,(SELECT count(*) FROM training_policy_lifecycle_event e WHERE e.policy_id=p.policy_id) FROM training_policy_lifecycle p WHERE p.policy_id='policy-a'"); var rows=q.executeQuery()) {
                if (!rows.next()) throw new IllegalStateException("policy missing after lifecycle run");
                return new LifecycleReadback(rows.getString(1), rows.getLong(2), rows.getLong(3));
            }
        }

        @PostMapping("/test/day44/lifecycle/cancellation")
        PolicySnapshot cancellation() {
            lifecycleClock.set(Instant.parse("2026-12-10T12:00:00Z"));
            var request = new PolicyCancellationRequest(2, "cancel-e2e", LocalDate.parse("2026-12-10"), "CUSTOMER_REQUEST");
            PolicyCancellationReceiptStore empty = (owner, key) -> Optional.empty();
            var scheduled = service.scheduleCancellation(issuedAtVersionTwo(), request, empty);
            return service.applyDueCancellation(dataSource, scheduled);
        }

        private static PolicyIssueRequest request() { return new PolicyIssueRequest("journey-a", "quote-a", "issue-e2e", LocalDate.parse("2026-12-01")); }
        private static PolicySnapshot issued() {
            return new PolicySnapshot("policy-a", "owner-a", "journey-a", "quote-a", "home-secure-v1", "training-rate-v1",
                    new BigDecimal("125.00"), "INR", LocalDate.parse("2026-12-01"), PolicyStatus.ISSUED, 1, List.of(), null);
        }
        private static PolicySnapshot issuedAtVersionTwo() {
            var p=issued(); return new PolicySnapshot(p.policyId(),p.ownerId(),p.journeyId(),p.quoteVersionId(),p.productVersion(),p.rateVersion(),p.annualPremiumRupees(),p.currency(),p.effectiveDate(),p.status(),2,List.of(new PolicyEndorsement(2,LocalDate.parse("2026-12-02"),"REBUILD_AMOUNT",new BigDecimal("200000.00"))),null);
        }
    }
    record HistoryWire(long version,String effectiveDate,String changeCode) {}
    record ServicingWire(String policyId,String productVersion,String annualPremiumRupees,String currency,PolicyStatus status,long version,List<HistoryWire> history,String cancellationEffectiveDate) {}
    record PolicyRow(String policyId,String ownerId,String journeyId,String quoteVersionId,String productVersion,String rateVersion,String annualPremiumRupees,String currency,String effectiveDate,String status,long version,String cancellationEffectiveDate) {}
    record ReceiptRow(String ownerId,String clientRequestId,String requestFingerprint,String policyId) {}
    record EventRow(String policyId,long version,String eventType,String effectiveDate,String changeCode,String trainingRebuildAmountRupees) {}
    record DatabaseWire(long policies,long receipts,long events,String productVersion,String annualPremiumRupees,String currency,String status,Long version,String cancellationEffectiveDate,List<HistoryWire> endorsementHistory,List<PolicyRow> policyRows,List<ReceiptRow> receiptRows,List<EventRow> eventRows) {}
    static final class HarnessClock extends Clock {
        private final java.util.concurrent.atomic.AtomicReference<Instant> instant;
        HarnessClock(Instant initial) { instant=new java.util.concurrent.atomic.AtomicReference<>(initial); }
        void set(Instant value) { instant.set(value); }
        @Override public Instant instant() { return instant.get(); }
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { if(!ZoneOffset.UTC.equals(zone))throw new IllegalArgumentException("UTC fixture only");return this; }
    }
    static final class JdbcLifecycleReadStore implements PolicyReadStore {
        private final DataSource ds; JdbcLifecycleReadStore(DataSource ds){this.ds=ds;}
        public Optional<PolicySnapshot> findByPolicyIdAndOwnerId(String policyId,String ownerId) {
            try(Connection c=ds.getConnection();var q=c.prepareStatement("SELECT policy_id,owner_id,journey_id,quote_version_id,product_version,rate_version,annual_premium_rupees,currency,effective_date,status,version,cancellation_effective_date FROM training_policy_lifecycle WHERE policy_id=? AND owner_id=?")) {
                q.setString(1,policyId);q.setString(2,ownerId);try(var r=q.executeQuery()){if(!r.next())return Optional.empty();
                    var history=new java.util.ArrayList<PolicyEndorsement>();
                    try(var e=c.prepareStatement("SELECT version,effective_date,change_code,training_rebuild_amount_rupees FROM training_policy_lifecycle_event WHERE policy_id=? AND event_type='ENDORSED' ORDER BY effective_date ASC,version ASC")){e.setString(1,policyId);try(var x=e.executeQuery()){while(x.next())history.add(new PolicyEndorsement(x.getLong(1),x.getDate(2).toLocalDate(),x.getString(3),x.getBigDecimal(4)));}}
                    var cancellation=r.getDate(12)==null?null:r.getDate(12).toLocalDate();
                    return Optional.of(new PolicySnapshot(r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6),r.getBigDecimal(7),r.getString(8).trim(),r.getDate(9).toLocalDate(),PolicyStatus.valueOf(r.getString(10)),r.getLong(11),history,cancellation));
                }
            } catch(Exception e){throw new IllegalStateException("synthetic read failed",e);}
        }
    }

    record Witness(String witnessId, String value) { }
    record LifecycleReadback(String status, long version, long eventCount) { }
}
