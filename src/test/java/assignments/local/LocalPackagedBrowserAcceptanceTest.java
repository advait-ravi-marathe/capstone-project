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
             Sprint5PackagedRuntimeProcess app = Sprint5PackagedRuntimeProcess.start(postgres, PREVIEW_TEST_CLOCK)) {
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
                    () -> assertEquals(0, result.path("prohibitedControls").path("buttons").asInt()),
                    () -> assertEquals(0, result.path("prohibitedControls").path("links").asInt()),
                    () -> {
                        if (!previewControlsPresent) return;
                        JsonNode response = result.path("previewAsOwnerA");
                        assertEquals(200, response.path("status").asInt());
                        JsonNode body = new ObjectMapper().readTree(response.path("body").asString());
                        assertEquals(java.util.Set.of("policyId", "sourcePolicyVersion", "productVersion", "rateVersion",
                                        "currentAnnualPremiumRupees", "currency", "nextTermEffectiveDate", "notice"), jsonKeys(body));
                        assertEquals("policy-renewal-a", body.path("policyId").asString());
                        assertTrue(body.path("sourcePolicyVersion").isIntegralNumber());
                        assertEquals(3, body.path("sourcePolicyVersion").asInt());
                        assertEquals("home-secure-v1", body.path("productVersion").asString());
                        assertEquals("home-secure-rate-v1", body.path("rateVersion").asString());
                        assertTrue(body.path("currentAnnualPremiumRupees").isNumber());
                        assertEquals(new java.math.BigDecimal("125.00"), body.path("currentAnnualPremiumRupees").decimalValue());
                        assertEquals("INR", body.path("currency").asString());
                        assertEquals("2027-01-15", body.path("nextTermEffectiveDate").asString());
                        assertEquals("Reference only; not a renewal offer or coverage commitment.", body.path("notice").asString());
                        assertTrue(result.path("visibleAfterOwnerA").asString().contains("125.00"));
                        assertTrue(result.path("visibleAfterOwnerA").asString().contains("2027-01-15"));
                        assertTrue(result.path("visibleAfterOwnerA").asString().contains(body.path("notice").asString()));
                        assertFalse(result.path("visibleAfterOwnerA").asString().toLowerCase().matches("(?s).*(purchase|renew now|buy coverage).*"));
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
             Sprint5PackagedRuntimeProcess app = Sprint5PackagedRuntimeProcess.start(postgres, "2026-12-19T12:00:00Z")) {
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
                        assertEquals(issues.path(0).path("requestBody").asString(), issues.path(1).path("requestBody").asString(),
                                "replay reuses the exact frozen request body");
                        assertTrue(reads.isArray() && reads.size() >= 1, "capture owner-scoped GET readback");
                        assertEquals(200, reads.path(0).path("status").asInt());
                        String issuedPolicyId = bodyField(issues.path(0), "policyId");
                        assertEquals(issuedPolicyId, bodyField(issues.path(1), "policyId"),
                                "same-key replay must return the issued policy ID");
                        assertEquals(issuedPolicyId, bodyField(reads.path(0), "policyId"),
                                "owner-scoped readback must return the issued policy ID");
                        JsonNode request = new ObjectMapper().readTree(issues.path(0).path("requestBody").asString());
                        assertEquals("journey-a", request.path("journeyId").asString());
                        assertEquals("quote-a", request.path("quoteVersionId").asString());
                        assertFalse(request.path("clientRequestId").asString().isBlank());
                        assertPolicyProjection(new ObjectMapper().readTree(issues.path(0).path("body").asString()), request);
                        assertPolicyProjection(new ObjectMapper().readTree(issues.path(1).path("body").asString()), request);
                        assertPolicyProjection(new ObjectMapper().readTree(reads.path(0).path("body").asString()), request);
                        assertEquals(1, rowCount(afterRows.policies()) - rowCount(beforeRows.policies()));
                        JsonNode createdRows = new ObjectMapper().readTree(afterRows.policies());
                        JsonNode createdRow = findRow(createdRows, "policy_id", issuedPolicyId);
                        assertNotNull(createdRow);
                        assertEquals("owner-a", createdRow.path("owner_id").asString());
                        assertEquals("journey-a", createdRow.path("journey_id").asString());
                        assertEquals("quote-a", createdRow.path("quote_version_id").asString());
                        assertEquals("home-secure-v1", createdRow.path("product_version").asString());
                        assertEquals("training-rate-v1", createdRow.path("rate_version").asString());
                        assertEquals(new java.math.BigDecimal("125.00"), createdRow.path("annual_premium_rupees").decimalValue());
                        assertEquals("INR", createdRow.path("currency").asString().trim());
                        assertEquals(request.path("effectiveDate").asString(), createdRow.path("effective_date").asString());
                        assertEquals("ISSUED", createdRow.path("status").asString());
                        assertEquals(1, createdRow.path("version").asInt());
                        assertTrue(createdRow.path("cancellation_effective_date").isNull());
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
            JsonNode logoutJourney;
            JsonNode first;
            PolicyDatabaseSnapshot beforeLogoutJourney;
            PolicyDatabaseSnapshot afterLogoutJourney;
            PolicyDatabaseSnapshot beforeActiveSession;
            PolicyDatabaseSnapshot afterActiveSession;
            Path sessionCookieFile = privateSessionCookieFile();
            JsonNode restarted;
            PolicyDatabaseSnapshot beforeRestartPreview;
            PolicyDatabaseSnapshot afterRestartPreview;
            long firstServerPid;
            long restartedServerPid;
            try {
                try (Sprint5PackagedRuntimeProcess app = Sprint5PackagedRuntimeProcess.start(postgres, PREVIEW_TEST_CLOCK)) {
                    firstServerPid = app.pid();
                    // Start the packaged app first so Flyway creates and seeds the database before queries run.
                    beforeLogoutJourney = policySnapshot(dataSource);
                    logoutJourney = runBrowser(app.port(), "day69", OUTPUT.resolve("day69-logout.json"),
                            null, null);
                    afterLogoutJourney = policySnapshot(dataSource);
                    beforeActiveSession = policySnapshot(dataSource);
                    first = runBrowser(app.port(), "day69-restart-seed", OUTPUT.resolve("day69-before-restart.json"),
                            null, sessionCookieFile);
                    afterActiveSession = policySnapshot(dataSource);
                }
                try (Sprint5PackagedRuntimeProcess app = Sprint5PackagedRuntimeProcess.start(postgres, PREVIEW_TEST_CLOCK)) {
                    restartedServerPid = app.pid();
                    beforeRestartPreview = policySnapshot(dataSource);
                    restarted = runBrowser(app.port(), "day69-restart-check",
                            OUTPUT.resolve("day69-after-restart.json"), sessionCookieFile, null);
                    afterRestartPreview = policySnapshot(dataSource);
                }
            } finally {
                Files.deleteIfExists(sessionCookieFile);
            }
            assertNotEquals(firstServerPid, restartedServerPid, "the second JAR launch is a distinct server process");
            boolean firstJourneyControlsPresent = logoutJourney.path("controls").path("selectOwnerA").asBoolean()
                    && logoutJourney.path("controls").path("viewPreviewAsOwnerA").asBoolean()
                    && logoutJourney.path("controls").path("selectOwnerB").asBoolean()
                    && logoutJourney.path("controls").path("viewForeignPreview").asBoolean()
                    && logoutJourney.path("controls").path("logout").asBoolean();
            boolean restartControlsPresent = restarted.path("controls").path("reselectOwnerA").asBoolean()
                    && restarted.path("controls").path("viewPreviewAfterRestart").asBoolean();
            assertAll(
                    () -> assertTrue(logoutJourney.path("controls").path("selectOwnerA").asBoolean(), "owner-a must be selectable in the UI"),
                    () -> assertTrue(logoutJourney.path("controls").path("viewPreviewAsOwnerA").asBoolean(), "preview must be a UI action"),
                    () -> assertTrue(logoutJourney.path("controls").path("selectOwnerB").asBoolean(), "owner-b must be selectable in the UI"),
                    () -> assertTrue(logoutJourney.path("controls").path("viewForeignPreview").asBoolean(), "foreign read must use the UI"),
                    () -> assertTrue(logoutJourney.path("controls").path("logout").asBoolean(), "logout must be a UI action"),
                    () -> assertTrue(restarted.path("controls").path("reselectOwnerA").asBoolean(), "owner re-selection must use the UI after restart"),
                    () -> assertTrue(restarted.path("controls").path("viewPreviewAfterRestart").asBoolean(), "restarted preview must use the UI"),
                    () -> {
                        if (!firstJourneyControlsPresent) return;
                        assertEquals(200, logoutJourney.path("previewAsOwnerA").path("status").asInt());
                        assertEquals(200, logoutJourney.path("ownerBSessionSelection").path("status").asInt());
                        assertTrue(logoutJourney.path("ownerSessionRotated").asBoolean(),
                                "switching synthetic owners rotates the session ID");
                        assertEquals(404, logoutJourney.path("previewAsOwnerB").path("status").asInt(), "foreign owner remains concealed");
                        assertFalse(logoutJourney.path("visibleAfterOwnerB").asString()
                                .contains("Reference only; not a renewal offer or coverage commitment."),
                                "owner-b view must remove owner-a preview text");
                        assertEquals(204, responseStatus(logoutJourney.path("apiResponses"), "POST", "/api/session/logout"));
                        assertEquals(401, logoutJourney.path("sessionAfterLogout").path("status").asInt());
                        assertFalse(logoutJourney.path("visibleAfterLogout").asString()
                                .contains("Reference only; not a renewal offer or coverage commitment."),
                                "logout must remove authenticated preview text");
                    },
                    () -> {
                        assertTrue(first.path("controls").path("viewPreviewBeforeRestart").asBoolean());
                        assertTrue(first.path("sessionCookieActiveBeforeRestart").asBoolean(),
                                "restart cookie must be demonstrably active before process stop");
                        assertEquals(200, first.path("sessionBeforeRestart").path("status").asInt());
                        if (!restartControlsPresent) return;
                        assertEquals(401, restarted.path("afterRestartSession").path("status").asInt());
                        assertEquals(200, restarted.path("previewAfterRestart").path("status").asInt());
                        assertEquals(first.path("previewBeforeRestart").path("body").asString(),
                                restarted.path("previewAfterRestart").path("body").asString(), "preview response is stable across restart");
                        assertTrue(restarted.path("visibleAfterRestart").asString().contains("125.00"));
                        assertTrue(restarted.path("visibleAfterRestart").asString().contains("2027-01-15"));
                    },
                    () -> assertEquals(beforeLogoutJourney, afterLogoutJourney,
                            "preview, owner switch and logout must not change policy values, versions, history, or idempotency records"),
                    () -> assertEquals(beforeActiveSession, afterActiveSession,
                            "the active restart sample and preview must not change persisted rows"),
                    () -> assertEquals(afterActiveSession, beforeRestartPreview,
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
        Files.deleteIfExists(output);
        Files.deleteIfExists(log);
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

    private static java.util.Set<String> jsonKeys(JsonNode object) {
        java.util.Set<String> keys = new java.util.HashSet<>();
        keys.addAll(object.propertyNames());
        return keys;
    }

    private static void assertPolicyProjection(JsonNode actual, JsonNode request) {
        assertEquals(java.util.Set.of("policyId", "ownerId", "journeyId", "quoteVersionId", "productVersion",
                "rateVersion", "annualPremiumRupees", "currency", "effectiveDate", "status", "version",
                "endorsements", "cancellationEffectiveDate"), jsonKeys(actual));
        assertEquals("policy-a", actual.path("policyId").asString());
        assertEquals("owner-a", actual.path("ownerId").asString());
        assertEquals(request.path("journeyId").asString(), actual.path("journeyId").asString());
        assertEquals(request.path("quoteVersionId").asString(), actual.path("quoteVersionId").asString());
        assertEquals("home-secure-v1", actual.path("productVersion").asString());
        assertEquals("training-rate-v1", actual.path("rateVersion").asString());
        assertTrue(actual.path("annualPremiumRupees").isNumber());
        assertEquals(new java.math.BigDecimal("125.00"), actual.path("annualPremiumRupees").decimalValue());
        assertEquals("INR", actual.path("currency").asString());
        assertEquals(request.path("effectiveDate").asString(), actual.path("effectiveDate").asString());
        assertEquals("ISSUED", actual.path("status").asString());
        assertTrue(actual.path("version").isIntegralNumber());
        assertEquals(1, actual.path("version").asInt());
        assertTrue(actual.path("endorsements").isArray());
        assertEquals(0, actual.path("endorsements").size());
        assertTrue(actual.path("cancellationEffectiveDate").isNull());
    }

    private static JsonNode findRow(JsonNode rows, String key, String value) {
        for (JsonNode row : rows) if (value.equals(row.path(key).asString())) return row;
        return null;
    }
}
