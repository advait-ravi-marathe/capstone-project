package coverstory.modules.catalog;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import coverstory.CoverStoryApplication;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Actual embedded-PostgreSQL -> Catalog JDBC port -> Spring HTTP evidence for CAP-166 setup. */
class CAP166CatalogRuntimeIntegrationTest {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test void persistedVersionHistorySurvivesSpringRestartAndExposesRetiredSourceSafely() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            String jdbcUrl;
            String username;
            try (Connection c = postgres.getPostgresDatabase().getConnection()) {
                jdbcUrl = c.getMetaData().getURL();
                username = c.getMetaData().getUserName();
                try (Statement s = c.createStatement()) {
                    executeResource(s, "/db/schema/ownership.sql");
                    executeResource(s, "/db/schema/catalog_publication.sql");
                    executeResource(s, "/db/schema/catalog_discovery.sql");
                    executeResource(s, "/coverstory/catalog/cap_166_seed.sql");
                }
            }
            String first;
            try (ConfigurableApplicationContext app = startApp(jdbcUrl, username)) {
                first = fetch(app);
                assertContract(first);
                assertEquals(400, get(app, "/api/catalog/versions?category=motor").statusCode(),
                        "the health-only comparison must not combine other scenarios/categories");
            }
            try (ConfigurableApplicationContext restarted = startApp(jdbcUrl, username)) {
                String afterRestart = fetch(restarted);
                assertEquals(JSON.readTree(first), JSON.readTree(afterRestart));
                assertContract(afterRestart);
                try (Connection c = postgres.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
                    s.execute("DROP VIEW catalog.health_offer_discovery_export");
                }
                HttpResponse<String> unavailable = get(restarted, "/api/catalog/versions");
                assertEquals(503, unavailable.statusCode());
                assertEquals("CATALOG_PERSISTENCE_UNAVAILABLE", JSON.readTree(unavailable.body()).path("code").asText());
                assertFalse(unavailable.body().contains("health_offer_discovery"), "database errors are not exposed");
            }
        }
    }

    @Test void starterWithoutConfiguredDatabaseFailsReadOnlyCatalogRouteClosed() throws Exception {
        try (ConfigurableApplicationContext app = new SpringApplicationBuilder(CoverStoryApplication.class)
                .web(WebApplicationType.SERVLET)
                .properties("server.port=0", "spring.main.banner-mode=off", "logging.level.root=ERROR")
                .run()) {
            HttpResponse<String> response = get(app, "/api/catalog/versions");
            assertEquals(503, response.statusCode());
            assertEquals("CATALOG_PERSISTENCE_UNAVAILABLE", JSON.readTree(response.body()).path("code").asText());
        }
    }

    @Test void chromiumExercisesPersistedReactSpringPostgresFlowAndOutageRecovery() throws Exception {
        Assumptions.assumeTrue(Boolean.getBoolean("coverstory.cap166.browser"),
                "run with -Dcoverstory.cap166.browser=true after npm ci on a host with Chromium");
        EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
        ConfigurableApplicationContext app = null;
        Process browserFlow = null;
        OutputStreamWriter browserCommands = null;
        try {
            String jdbcUrl; String username;
            try (Connection c = postgres.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
                jdbcUrl = c.getMetaData().getURL(); username = c.getMetaData().getUserName();
                executeResource(s, "/db/schema/ownership.sql");
                executeResource(s, "/db/schema/catalog_publication.sql");
                executeResource(s, "/db/schema/catalog_discovery.sql");
                executeResource(s, "/coverstory/catalog/cap_166_seed.sql");
            }
            app = startApp(jdbcUrl, username);
            int apiPort = ((WebServerApplicationContext) app).getWebServer().getPort();
            String decoderMode = System.getProperty("coverstory.cap166.decoder", "instructor");
            assertTrue(List.of("instructor", "learner").contains(decoderMode), "decoder mode must be instructor or learner");
            String runMode = System.getProperty("coverstory.cap166.browser.mode", "full");
            assertTrue(List.of("full", "todo-smoke").contains(runMode), "browser mode must be full or todo-smoke");
            ProcessBuilder browserBuilder = new ProcessBuilder("node", "web/scripts/cap166-browser-flow.mjs", "http://127.0.0.1:" + apiPort, decoderMode, runMode)
                    .redirectErrorStream(true);
            String configuredChromium = System.getProperty("coverstory.cap166.chromium");
            if (configuredChromium != null && !configuredChromium.isBlank()) {
                browserBuilder.environment().put("COVERSTORY_CHROMIUM", configuredChromium);
            }
            browserFlow = browserBuilder.start();
            browserCommands = new OutputStreamWriter(browserFlow.getOutputStream());
            try (BufferedReader output = new BufferedReader(new InputStreamReader(browserFlow.getInputStream()))) {
                if (decoderMode.equals("learner") && runMode.equals("todo-smoke")) {
                    awaitLine(output, "CAP166_LEARNER_TODO_SMOKE", Duration.ofSeconds(70));
                    assertEquals(0, browserFlow.waitFor(10, TimeUnit.SECONDS) ? browserFlow.exitValue() : -1);
                    return;
                }
                awaitLine(output, "BROWSER_READY", Duration.ofSeconds(70));
                // Stop only the Catalog HTTP service. PostgreSQL remains durable and available
                // for the restarted service; the browser must first fail closed with no stale data.
                app.close();
                app = null;
                browserCommands.write("OUTAGE\n"); browserCommands.flush();
                awaitLine(output, "OUTAGE_RECOVERY_VALIDATED", Duration.ofSeconds(30));
                app = startApp(jdbcUrl, username, apiPort);
                browserCommands.write("RECOVER\n"); browserCommands.flush();
                awaitLine(output, "CAP166_BROWSER_RECOVERED", Duration.ofSeconds(30));
            }
            assertEquals(0, browserFlow.waitFor(10, TimeUnit.SECONDS) ? browserFlow.exitValue() : -1);
        } finally {
            if (browserCommands != null) try { browserCommands.close(); } catch (Exception ignored) { }
            if (browserFlow != null && browserFlow.isAlive()) browserFlow.destroyForcibly();
            if (app != null) app.close();
            try { postgres.close(); } catch (Exception ignored) { }
        }
    }

    private static void awaitLine(BufferedReader output, String expected, Duration timeout) throws Exception {
        CompletableFuture<String> next = CompletableFuture.supplyAsync(() -> {
            try { return output.readLine(); } catch (Exception error) { throw new RuntimeException(error); }
        });
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            String line = next.get(Math.max(1, deadline - System.nanoTime()), TimeUnit.NANOSECONDS);
            if (line == null) throw new AssertionError("browser harness exited before " + expected);
            if (line.contains("CAP166_LEARNER_ACCEPTANCE_TODO"))
                throw new AssertionError("learner browser acceptance reached the intentionally unimplemented decodeCatalogEnvelope TODO");
            if (line.startsWith("Error:") || line.startsWith("AssertionError") || line.startsWith("Error "))
                throw new AssertionError("browser harness failure: " + line);
            System.out.println("CAP166 browser evidence: " + line);
            if (line.contains(expected)) return;
            next = CompletableFuture.supplyAsync(() -> {
                try { return output.readLine(); } catch (Exception error) { throw new RuntimeException(error); }
            });
        }
        throw new AssertionError("Timed out waiting for browser harness marker " + expected);
    }

    static ConfigurableApplicationContext startApp(String url, String username) {
        return startApp(url, username, 0);
    }

    static ConfigurableApplicationContext startApp(String url, String username, int port) {
        return new SpringApplicationBuilder(CoverStoryApplication.class).web(WebApplicationType.SERVLET)
                .properties("server.port=" + port, "spring.main.banner-mode=off", "logging.level.root=ERROR",
                        "coverstory.catalog.jdbc.url=" + url,
                        "coverstory.catalog.jdbc.username=" + username,
                        "coverstory.catalog.jdbc.password=")
                .run();
    }

    private static String fetch(ConfigurableApplicationContext app) throws Exception {
        HttpResponse<String> response = get(app, "/api/catalog/versions");
        assertEquals(200, response.statusCode());
        return response.body();
    }

    private static void assertContract(String body) throws Exception {
        JsonNode root = JSON.readTree(body);
        assertEquals(1, root.path("schemaMajor").asInt());
        JsonNode versions = root.path("versions");
        assertEquals(3, versions.size());
        assertRow(versions.get(0), "16600000-0000-4000-8000-000000000001", "health-example-a", "v1", "RETIRED");
        assertRow(versions.get(1), "16600000-0000-4000-8000-000000000002", "health-example-a", "v2", "PUBLISHED");
        assertRow(versions.get(2), "16600000-0000-4000-8000-000000000003", "health-example-b", "v1", "PUBLISHED");
        for (JsonNode row : versions) {
            assertEquals("health", row.path("category").asText());
            assertEquals("individual", row.path("scenarioId").asText());
            assertEquals("SYNTHETIC", row.path("evidenceStatus").asText());
            assertFalse(row.has("price")); assertFalse(row.has("amountMinor"));
            assertFalse(row.has("restrictions")); assertFalse(row.has("terms"));
        }
    }

    private static void assertRow(JsonNode row, String id, String source, String sourceVersion, String status) {
        assertEquals(id, row.path("versionId").asText());
        assertEquals(source, row.path("sourceId").asText());
        assertEquals(sourceVersion, row.path("sourceVersion").asText());
        assertEquals(status, row.path("status").asText());
    }

    private static HttpResponse<String> get(ConfigurableApplicationContext app, String path) throws Exception {
        int port = ((WebServerApplicationContext) app).getWebServer().getPort();
        return HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Accept", "application/json").GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    static void executeResource(Statement statement, String resource) throws Exception {
        try (var in = CAP166CatalogRuntimeIntegrationTest.class.getResourceAsStream(resource)) {
            assertNotNull(in, "missing SQL resource " + resource);
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            boolean dollar = false, single = false, lineComment = false;
            StringBuilder current = new StringBuilder();
            for (int i = 0; i < sql.length(); i++) {
                if (!single && !dollar && sql.startsWith("--", i)) lineComment = true;
                if (lineComment) {
                    char c = sql.charAt(i);
                    if (c == '\n') { lineComment = false; current.append(c); }
                    continue;
                }
                if (!single && sql.startsWith("$$", i)) { dollar = !dollar; current.append("$$"); i++; continue; }
                char c = sql.charAt(i);
                if (!dollar && c == '\'') single = !single;
                if (!dollar && !single && c == ';') {
                    if (!current.toString().isBlank()) statement.execute(current.toString());
                    current.setLength(0);
                } else current.append(c);
            }
            if (!current.toString().isBlank()) statement.execute(current.toString());
        }
    }
}
