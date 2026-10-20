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
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Learner feature acceptance driven through UI controls against the built executable JAR. */
class LocalPackagedBrowserAcceptanceTest {
    private static final Path OUTPUT = Path.of("target/local-coverstory-browser").toAbsolutePath();

    @Test
    @Tag("learnerAcceptance")
    void day68PackagedUiPreviewIsReadOnly() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
             LocalPackagedRuntimeProcess app = LocalPackagedRuntimeProcess.start(postgres)) {
            long before = policyCount(postgres.getPostgresDatabase());
            JsonNode result = runBrowser(app.port(), "day68-preview", OUTPUT.resolve("day68-preview.json"), null);
            long after = policyCount(postgres.getPostgresDatabase());
            assertAll(
                    () -> assertEquals(200, result.path("pageStatus").asInt()),
                    () -> assertEquals("CoverStory local training", result.path("heading").asString()),
                    () -> assertTrue(result.path("controls").path("selectOwnerA").asBoolean(), "real UI must offer owner-a"),
                    () -> assertTrue(result.path("controls").path("viewPreviewAsOwnerA").asBoolean(), "preview must be a UI action"),
                    () -> assertEquals(200, result.path("previewAsOwnerA").path("status").asInt()),
                    () -> assertTrue(result.path("previewAsOwnerA").path("body").asString().contains("nextTermEffectiveDate")),
                    () -> assertEquals(before, after, "preview must not insert or update policies")
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
            JsonNode result = runBrowser(app.port(), "day68-issue", OUTPUT.resolve("day68-issue.json"), null);
            long after = policyCount(dataSource);
            JsonNode issues = result.path("issueResponses");
            JsonNode reads = result.path("readbackResponses");
            assertAll(
                    () -> assertTrue(result.path("controls").path("issueSelectedQuote").asBoolean(), "issue must be a UI action"),
                    () -> assertTrue(result.path("controls").path("readIssuedPolicy").asBoolean(), "readback must be a UI action"),
                    () -> assertTrue(result.path("controls").path("replayIssue").asBoolean(), "same-key replay must be a UI action"),
                    () -> assertTrue(issues.isArray() && issues.size() == 2, "capture original and replay POSTs"),
                    () -> assertEquals(201, issues.path(0).path("status").asInt()),
                    () -> assertEquals(200, issues.path(1).path("status").asInt()),
                    () -> assertTrue(reads.isArray() && reads.size() >= 1, "capture owner-scoped GET readback"),
                    () -> assertEquals(200, reads.path(0).path("status").asInt()),
                    () -> assertEquals(bodyField(issues.path(0), "policyId"), bodyField(issues.path(1), "policyId")),
                    () -> assertEquals(before + 1, after, "replay must not create another policy row")
            );
        }
    }

    @Test
    @Tag("learnerAcceptance")
    void day69PreviewIsStableAcrossPackagedRestartAndLogout() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            long before = policyCount(dataSource);
            JsonNode first;
            try (LocalPackagedRuntimeProcess app = LocalPackagedRuntimeProcess.start(postgres)) {
                first = runBrowser(app.port(), "day69", OUTPUT.resolve("day69-before-restart.json"), null);
            }
            JsonNode restarted;
            try (LocalPackagedRuntimeProcess app = LocalPackagedRuntimeProcess.start(postgres)) {
                restarted = runBrowser(app.port(), "day69-restart-check",
                        OUTPUT.resolve("day69-after-restart.json"), first.path("sessionCookie").asString());
            }
            long after = policyCount(dataSource);
            assertAll(
                    () -> assertEquals(200, first.path("previewAsOwnerA").path("status").asInt()),
                    () -> assertEquals(200, first.path("ownerBSessionSelection").path("status").asInt()),
                    () -> assertNotEquals(first.path("ownerASessionCookie").asString(),
                            first.path("ownerBSessionCookie").asString(), "switching synthetic owners rotates the session ID"),
                    () -> assertEquals(404, first.path("previewAsOwnerB").path("status").asInt(), "foreign owner remains concealed"),
                    () -> assertEquals(204, responseStatus(first.path("apiResponses"), "POST", "/api/session/logout")),
                    () -> assertEquals(401, first.path("sessionAfterLogout").path("status").asInt()),
                    () -> assertEquals(401, restarted.path("afterRestartSession").path("status").asInt()),
                    () -> assertTrue(restarted.path("controls").path("reselectOwnerA").asBoolean()),
                    () -> assertEquals(200, restarted.path("previewAfterRestart").path("status").asInt()),
                    () -> assertEquals(first.path("previewAsOwnerA").path("body").asString(),
                            restarted.path("previewAfterRestart").path("body").asString(), "preview response is stable across restart"),
                    () -> assertEquals(before, after, "preview and session changes must not write policies")
            );
        }
    }

    private static JsonNode runBrowser(int port, String scenario, Path output, String sessionCookie) throws Exception {
        Files.createDirectories(output.getParent());
        Path log = output.resolveSibling(output.getFileName() + ".log");
        var command = new java.util.ArrayList<>(java.util.List.of("node", "e2e/local-coverstory-browser.mjs",
                "http://127.0.0.1:" + port, output.toString(), scenario));
        if (sessionCookie != null) command.add(sessionCookie);
        ProcessBuilder builder = new ProcessBuilder(command).directory(Path.of("frontend").toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile());
        String chromium = System.getenv("CHROMIUM_BIN");
        if (chromium != null && !chromium.isBlank()) builder.environment().put("CHROMIUM_BIN", chromium);
        Process runner = builder.start();
        if (!runner.waitFor(45, TimeUnit.SECONDS)) {
            runner.destroyForcibly();
            runner.waitFor();
            throw new IllegalStateException("Packaged-JAR Playwright flow timed out: " + Files.readString(log));
        }
        assertEquals(0, runner.exitValue(), Files.readString(log));
        return new ObjectMapper().readTree(Files.readString(output));
    }

    private static long policyCount(DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             var rows = statement.executeQuery("SELECT count(*) FROM training_policy_lifecycle")) {
            assertTrue(rows.next());
            return rows.getLong(1);
        }
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
