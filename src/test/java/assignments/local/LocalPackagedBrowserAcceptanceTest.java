package assignments.local;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Browser contract for the dedicated MAIN application and real synthetic PostgreSQL migration. */
class LocalPackagedBrowserAcceptanceTest {
    private static final Path OUTPUT = Path.of("target/local-coverstory-browser").toAbsolutePath();

    @Test
    @Tag("learnerAcceptance")
    void day68BrowserUsesPackagedMainAppAndPostgres() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            long before;
            BrowserResult result;
            try (ConfigurableApplicationContext app = startServer(postgres)) {
                before = policyCount(dataSource);
                result = runBrowser(port(app), "day68", OUTPUT.resolve("day68.json"), null);
                assertEquals(before, policyCount(dataSource), "browser preview must not insert or update policies");
            }
            assertAll(
                    () -> assertTrue(result.browserOpened()),
                    () -> assertEquals(200, result.pageStatus()),
                    () -> assertEquals("CoverStory local training", result.heading()),
                    () -> assertEquals(200, result.healthStatus()),
                    () -> assertEquals(401, result.anonymousStatus()),
                    () -> assertEquals(200, result.sessionSelectionStatus()),
                    () -> assertEquals(200, result.authenticatedSessionStatus()),
                    () -> assertEquals(200, result.previewStatus(), result.previewBody()),
                    () -> assertTrue(result.previewBody().contains("nextTermEffectiveDate"))
            );
        }
    }

    @Test
    @Tag("learnerAcceptance")
    void day69RestartLogoutForeignOwnerAndPreviewNoWrite() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            DataSource dataSource = postgres.getPostgresDatabase();
            long before;
            BrowserResult first;
            try (ConfigurableApplicationContext app = startServer(postgres)) {
                before = policyCount(dataSource);
                first = runBrowser(port(app), "day69", OUTPUT.resolve("day69-before-restart.json"), null);
            }
            BrowserResult afterRestart;
            try (ConfigurableApplicationContext restarted = startServer(postgres)) {
                afterRestart = runBrowser(port(restarted), "day69-restart-check",
                        OUTPUT.resolve("day69-after-restart.json"), first.sessionCookie());
            }
            assertAll(
                    () -> assertEquals(200, first.sessionSelectionStatus()),
                    () -> assertEquals(200, first.ownerBSelectionStatus()),
                    () -> assertEquals(404, first.foreignOwnerPreviewStatus(), first.foreignOwnerPreviewBody()),
                    () -> assertEquals(204, first.logoutStatus()),
                    () -> assertEquals(401, first.sessionAfterLogoutStatus()),
                    () -> assertNotNull(first.sessionCookie()),
                    () -> assertEquals(401, afterRestart.sessionAfterRestartStatus())
            );
            assertEquals(before, policyCount(dataSource), "sessions, restart and preview reads cannot write policies");
        }
    }

    private static ConfigurableApplicationContext startServer(EmbeddedPostgres postgres) {
        return new SpringApplicationBuilder(LocalCoverStoryApplication.class)
                .web(WebApplicationType.SERVLET)
                .profiles("local")
                .run("--server.address=127.0.0.1", "--server.port=0", "--logging.level.root=ERROR",
                        "--spring.datasource.url=" + postgres.getJdbcUrl("postgres", "postgres"),
                        "--spring.datasource.username=postgres", "--spring.datasource.password=");
    }

    private static int port(ConfigurableApplicationContext context) {
        return ((WebServerApplicationContext) context).getWebServer().getPort();
    }

    private static BrowserResult runBrowser(int port, String scenario, Path output, String sessionCookie) throws Exception {
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
            throw new IllegalStateException("Local Playwright browser timed out: " + Files.readString(log));
        }
        assertEquals(0, runner.exitValue(), Files.readString(log));
        var json = new ObjectMapper().readTree(Files.readString(output));
        return BrowserResult.from(json);
    }

    private static long policyCount(DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             var rows = statement.executeQuery("SELECT count(*) FROM training_policy_lifecycle")) {
            assertTrue(rows.next());
            return rows.getLong(1);
        }
    }

    private record BrowserResult(boolean browserOpened, int pageStatus, String heading, int healthStatus,
                                 int anonymousStatus, int sessionSelectionStatus, int authenticatedSessionStatus,
                                 int previewStatus, String previewBody, int ownerBSelectionStatus,
                                 int foreignOwnerPreviewStatus, String foreignOwnerPreviewBody,
                                 int logoutStatus, int sessionAfterLogoutStatus, String sessionCookie,
                                 int sessionAfterRestartStatus) {
        static BrowserResult from(tools.jackson.databind.JsonNode json) {
            var selection = json.get("selectedOwnerA");
            var session = json.get("authenticatedOwnerA");
            var preview = json.get("previewAsOwnerA");
            var foreignPreview = json.get("previewAsOwnerB");
            var afterLogout = json.get("afterLogout");
            var afterRestart = json.get("afterRestart");
            return new BrowserResult(json.path("browserOpened").asBoolean(), json.path("pageStatus").asInt(),
                    json.path("heading").isNull() ? null : json.path("heading").asString(),
                    json.path("healthStatus").asInt(), json.path("anonymousBeforeSession").path("status").asInt(),
                    intStatus(selection), intStatus(session), intStatus(preview), text(preview),
                    intStatus(json.get("selectedOwnerB")), intStatus(foreignPreview), text(foreignPreview),
                    json.path("logoutStatus").asInt(), intStatus(afterLogout),
                    json.path("sessionCookie").isNull() ? null : json.path("sessionCookie").asString(),
                    intStatus(afterRestart));
        }

        private static int intStatus(tools.jackson.databind.JsonNode node) {
            return node == null || node.isNull() ? 0 : node.path("status").asInt();
        }

        private static String text(tools.jackson.databind.JsonNode node) {
            return node == null || node.isNull() ? "" : node.path("body").asString();
        }
    }
}
