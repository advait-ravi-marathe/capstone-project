package assignments.local;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Learner feature acceptance driven through UI controls against the built executable JAR. */
class LocalPackagedBrowserAcceptanceTest {
    private static final Path OUTPUT = Path.of("target/local-coverstory-browser").toAbsolutePath();
    private static final String PREVIEW_TEST_CLOCK = "2027-01-01T12:00:00Z";

    @Test
    @Tag("learnerAcceptance")
    void day68PackagedUiPreviewIsReadOnly() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
             LocalPackagedRuntimeProcess app = LocalPackagedRuntimeProcess.start(postgres, PREVIEW_TEST_CLOCK)) {
            PolicyDatabaseSnapshot before = policySnapshot(postgres.getPostgresDatabase());
            JsonNode result = runBrowser(app.port(), "day68-preview", OUTPUT.resolve("day68-preview.json"), null, null);
            PolicyDatabaseSnapshot after = policySnapshot(postgres.getPostgresDatabase());
            boolean previewControlsPresent = result.path("controls").path("selectOwnerA").asBoolean()
                    && result.path("controls").path("viewPreviewAsOwnerA").asBoolean();
            assertAll(
                    () -> assertEquals(200, result.path("pageStatus").asInt()),
                    () -> assertEquals("CoverStory local training", result.path("heading").asString()),
                    () -> assertTrue(result.path("controls").path("selectOwnerA").asBoolean(), "real UI must offer owner-a"),
                    () -> assertTrue(result.path("controls").path("viewPreviewAsOwnerA").asBoolean(), "preview must be a UI action"),
                    () -> {
                        if (!previewControlsPresent) return;
                        assertEquals(200, result.path("previewAsOwnerA").path("status").asInt());
                        assertTrue(result.path("previewAsOwnerA").path("body").asString().contains("nextTermEffectiveDate"));
                    },
                    () -> assertEquals(before, after,
                            "preview must not change policy values, versions, lifecycle history, or idempotency records")
            );
        }
    }

    @Test
    @Tag("learnerAcceptance")
    void day68PackagedUiIssuesReadsBackAndReplaysExactlyOnce() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
             LocalPackagedRuntimeProcess app = LocalPackagedRuntimeProcess.start(postgres)) {
            DataSource dataSource = postgres.getPostgresDatabase();
            long before = policyCount(dataSource);
            PolicyDatabaseSnapshot beforeRows = policySnapshot(dataSource);
            JsonNode result = runBrowser(app.port(), "day68-issue", OUTPUT.resolve("day68-issue.json"), null, null);
            long after = policyCount(dataSource);
            PolicyDatabaseSnapshot afterRows = policySnapshot(dataSource);
            JsonNode issues = result.path("issueResponses");
            JsonNode reads = result.path("readbackResponses");
            boolean issueFlowControlsPresent = result.path("controls").path("selectOwnerA").asBoolean()
                    && result.path("controls").path("issueSelectedQuote").asBoolean()
                    && result.path("controls").path("readIssuedPolicy").asBoolean()
                    && result.path("controls").path("replayIssue").asBoolean();
            assertAll(
                    () -> assertTrue(result.path("controls").path("selectOwnerA").asBoolean(), "owner selection must be a UI action"),
                    () -> assertTrue(result.path("controls").path("issueSelectedQuote").asBoolean(), "issue must be a UI action"),
                    () -> assertTrue(result.path("controls").path("readIssuedPolicy").asBoolean(), "readback must be a UI action"),
                    () -> assertTrue(result.path("controls").path("replayIssue").asBoolean(), "same-key replay must be a UI action"),
                    () -> {
                        if (!issueFlowControlsPresent) return;
                        assertTrue(issues.isArray() && issues.size() == 2, "capture original and replay POSTs");
                        assertEquals(201, issues.path(0).path("status").asInt());
                        assertEquals(200, issues.path(1).path("status").asInt());
                        assertTrue(reads.isArray() && reads.size() >= 1, "capture owner-scoped GET readback");
                        assertEquals(200, reads.path(0).path("status").asInt());
                        String issuedPolicyId = bodyField(issues.path(0), "policyId");
                        assertEquals(issuedPolicyId, bodyField(issues.path(1), "policyId"),
                                "same-key replay must return the issued policy ID");
                        assertEquals(issuedPolicyId, bodyField(reads.path(0), "policyId"),
                                "owner-scoped readback must return the issued policy ID");
                        assertEquals(before + 1, after, "replay must not create another policy row");
                        assertEquals(rowCount(beforeRows.lifecycleEvents()) + 1, rowCount(afterRows.lifecycleEvents()),
                                "one new issued policy should add exactly one lifecycle event");
                        assertEquals(rowCount(beforeRows.idempotency()) + 1, rowCount(afterRows.idempotency()),
                                "same-key replay must add only the original idempotency record");
                    },
                    () -> assertTrue(preservesEveryRow(beforeRows.policies(), afterRows.policies()),
                            "issue/readback/replay must not update any preexisting policy values or versions"),
                    () -> assertTrue(preservesEveryRow(beforeRows.lifecycleEvents(), afterRows.lifecycleEvents()),
                            "issue/readback/replay must preserve all preexisting lifecycle history"),
                    () -> assertTrue(preservesEveryRow(beforeRows.idempotency(), afterRows.idempotency()),
                            "issue/readback/replay must preserve preexisting idempotency records")
            );
        }
    }

    @Test
    @Tag("learnerAcceptance")
    void day69PreviewIsStableAcrossPackagedRestartAndLogout() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            JsonNode first;
            PolicyDatabaseSnapshot beforeFirstPreview;
            PolicyDatabaseSnapshot afterFirstPreview;
            Path sessionCookieFile = privateSessionCookieFile();
            JsonNode restarted;
            PolicyDatabaseSnapshot beforeRestartPreview;
            PolicyDatabaseSnapshot afterRestartPreview;
            try {
                try (LocalPackagedRuntimeProcess app = LocalPackagedRuntimeProcess.start(postgres, PREVIEW_TEST_CLOCK)) {
                    // Start the packaged app first so Flyway creates and seeds the database before queries run.
                    beforeFirstPreview = policySnapshot(dataSource);
                    first = runBrowser(app.port(), "day69", OUTPUT.resolve("day69-before-restart.json"),
                            null, sessionCookieFile);
                    afterFirstPreview = policySnapshot(dataSource);
                }
                try (LocalPackagedRuntimeProcess app = LocalPackagedRuntimeProcess.start(postgres, PREVIEW_TEST_CLOCK)) {
                    beforeRestartPreview = policySnapshot(dataSource);
                    restarted = runBrowser(app.port(), "day69-restart-check",
                            OUTPUT.resolve("day69-after-restart.json"), sessionCookieFile, null);
                    afterRestartPreview = policySnapshot(dataSource);
                }
            } finally {
                Files.deleteIfExists(sessionCookieFile);
            }
            boolean firstJourneyControlsPresent = first.path("controls").path("selectOwnerA").asBoolean()
                    && first.path("controls").path("viewPreviewAsOwnerA").asBoolean()
                    && first.path("controls").path("selectOwnerB").asBoolean()
                    && first.path("controls").path("viewForeignPreview").asBoolean()
                    && first.path("controls").path("logout").asBoolean();
            boolean restartControlsPresent = restarted.path("controls").path("reselectOwnerA").asBoolean()
                    && restarted.path("controls").path("viewPreviewAfterRestart").asBoolean();
            assertAll(
                    () -> assertTrue(first.path("controls").path("selectOwnerA").asBoolean(), "owner-a must be selectable in the UI"),
                    () -> assertTrue(first.path("controls").path("viewPreviewAsOwnerA").asBoolean(), "preview must be a UI action"),
                    () -> assertTrue(first.path("controls").path("selectOwnerB").asBoolean(), "owner-b must be selectable in the UI"),
                    () -> assertTrue(first.path("controls").path("viewForeignPreview").asBoolean(), "foreign read must use the UI"),
                    () -> assertTrue(first.path("controls").path("logout").asBoolean(), "logout must be a UI action"),
                    () -> assertTrue(restarted.path("controls").path("reselectOwnerA").asBoolean(), "owner re-selection must use the UI after restart"),
                    () -> assertTrue(restarted.path("controls").path("viewPreviewAfterRestart").asBoolean(), "restarted preview must use the UI"),
                    () -> {
                        if (!firstJourneyControlsPresent) return;
                        assertEquals(200, first.path("previewAsOwnerA").path("status").asInt());
                        assertEquals(200, first.path("ownerBSessionSelection").path("status").asInt());
                        assertTrue(first.path("ownerSessionRotated").asBoolean(),
                                "switching synthetic owners rotates the session ID");
                        assertEquals(404, first.path("previewAsOwnerB").path("status").asInt(), "foreign owner remains concealed");
                        assertEquals(204, responseStatus(first.path("apiResponses"), "POST", "/api/session/logout"));
                        assertEquals(401, first.path("sessionAfterLogout").path("status").asInt());
                    },
                    () -> {
                        if (!restartControlsPresent) return;
                        assertEquals(401, restarted.path("afterRestartSession").path("status").asInt());
                        assertEquals(200, restarted.path("previewAfterRestart").path("status").asInt());
                        assertEquals(first.path("previewAsOwnerA").path("body").asString(),
                                restarted.path("previewAfterRestart").path("body").asString(), "preview response is stable across restart");
                    },
                    () -> assertEquals(beforeFirstPreview, afterFirstPreview,
                            "preview, owner switch and logout must not change policy values, versions, history, or idempotency records"),
                    () -> assertEquals(afterFirstPreview, beforeRestartPreview,
                            "Flyway startup on restart must preserve all policy and history rows"),
                    () -> assertEquals(beforeRestartPreview, afterRestartPreview,
                            "preview after restart must not change policy values, versions, history, or idempotency records")
            );
        }
    }

    private static JsonNode runBrowser(int port, String scenario, Path output,
                                       Path sessionCookieInput, Path sessionCookieOutput) throws Exception {
        Files.createDirectories(output.getParent());
        Path log = output.resolveSibling(output.getFileName() + ".log");
        var command = new java.util.ArrayList<>(java.util.List.of("node", "e2e/local-coverstory-browser.mjs",
                "http://127.0.0.1:" + port, output.toString(), scenario));
        ProcessBuilder builder = new ProcessBuilder(command).directory(Path.of("frontend").toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile());
        String chromium = System.getenv("CHROMIUM_BIN");
        if (chromium != null && !chromium.isBlank()) builder.environment().put("CHROMIUM_BIN", chromium);
        if (sessionCookieInput != null) {
            builder.environment().put("COVERSTORY_SESSION_COOKIE_INPUT", sessionCookieInput.toString());
        }
        if (sessionCookieOutput != null) {
            builder.environment().put("COVERSTORY_SESSION_COOKIE_OUTPUT", sessionCookieOutput.toString());
        }
        Process runner = builder.start();
        if (!runner.waitFor(45, TimeUnit.SECONDS)) {
            runner.destroyForcibly();
            runner.waitFor();
            throw new IllegalStateException("Packaged-JAR Playwright flow timed out: " + Files.readString(log));
        }
        assertEquals(0, runner.exitValue(), Files.readString(log));
        return new ObjectMapper().readTree(Files.readString(output));
    }

    private static Path privateSessionCookieFile() throws Exception {
        Files.createDirectories(OUTPUT);
        Path path = Files.createTempFile(OUTPUT, "day69-session-", ".cookie");
        try {
            Files.setPosixFilePermissions(path, java.nio.file.attribute.PosixFilePermissions.fromString("rw-------"));
        } catch (UnsupportedOperationException ignored) {
            // The generated temp file is always deleted in a finally block.
        }
        return path;
    }

    private record PolicyDatabaseSnapshot(String policies, String lifecycleEvents, String idempotency) { }

    private static PolicyDatabaseSnapshot policySnapshot(DataSource dataSource) throws Exception {
        return new PolicyDatabaseSnapshot(
                snapshotTable(dataSource, "training_policy_lifecycle", "policy_id"),
                snapshotTable(dataSource, "training_policy_lifecycle_event", "policy_id, version"),
                snapshotTable(dataSource, "training_policy_idempotency", "owner_id, client_request_id"));
    }

    private static long policyCount(DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT count(*) FROM training_policy_lifecycle")) {
            assertTrue(rows.next());
            return rows.getLong(1);
        }
    }

    private static String snapshotTable(DataSource dataSource, String table, String orderBy) throws Exception {
        String sql = "SELECT COALESCE(jsonb_agg(to_jsonb(row_data) ORDER BY " + orderBy + "), '[]'::jsonb)::text " +
                "FROM " + table + " AS row_data";
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            assertTrue(rows.next());
            return rows.getString(1);
        }
    }

    private static boolean preservesEveryRow(String beforeJson, String afterJson) throws Exception {
        JsonNode before = new ObjectMapper().readTree(beforeJson);
        JsonNode after = new ObjectMapper().readTree(afterJson);
        for (JsonNode previous : before) {
            boolean found = false;
            for (JsonNode current : after) {
                if (previous.equals(current)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private static int rowCount(String rowsJson) throws Exception {
        return new ObjectMapper().readTree(rowsJson).size();
    }

    private static int responseStatus(JsonNode responses, String method, String path) {
        for (JsonNode response : responses) {
            if (method.equals(response.path("method").asString()) && path.equals(response.path("path").asString())) {
                return response.path("status").asInt();
            }
        }
        return 0;
    }

    private static String bodyField(JsonNode response, String field) throws Exception {
        return new ObjectMapper().readTree(response.path("body").asString()).path(field).asString();
    }
}
