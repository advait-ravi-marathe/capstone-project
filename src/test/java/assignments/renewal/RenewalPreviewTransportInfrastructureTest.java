package assignments.renewal;

import assignments.insurance.*;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Instructor transport smoke uses a test-only read endpoint and browser route; it does not call learner rules.
 * The two tagged learner flows use the real controller and intentionally stop at unfinished policy/repository seams.
 */
class RenewalPreviewTransportInfrastructureTest {
    private static volatile DataSource postgresDataSource;
    private static final Path OUTPUT = Path.of("target/day45-renewal-harness").toAbsolutePath();
    private static final String FIXTURE_JSON = """
            {"policyId":"renewal-demo","sourcePolicyVersion":3,"productVersion":"home-secure-v1","rateVersion":"home-secure-rate-v1","currentAnnualPremiumRupees":"125.00","currency":"INR","nextTermEffectiveDate":"2027-12-01","notice":"Reference only; not a renewal offer or coverage commitment."}
            """;

    @Test
    void testOnlyReadOnlyBrowserTransportSurvivesSpringRestart() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            postgresDataSource = postgres.getPostgresDatabase();
            createWitnessTable();
            insertWitness();
            BrowserResult first;
            Path firstResult = OUTPUT.resolve("transport-first.json");
            try (ConfigurableApplicationContext context = startServer(TransportApplication.class, TransportConfiguration.class)) {
                first = runBrowser(port(context), firstResult, "smoke", "renewal-demo");
            }
            assertSuccessfulSmoke(first);
            assertEquals(200, first.status());
            assertTrue(first.body().contains("renewal-demo"));
            assertTrue(first.body().contains("not a renewal offer"));

            BrowserResult second;
            Path secondResult = OUTPUT.resolve("transport-restart.json");
            try (ConfigurableApplicationContext restarted = startServer(TransportApplication.class, TransportConfiguration.class)) {
                second = runBrowser(port(restarted), secondResult, "smoke", "renewal-demo");
            }
            assertSuccessfulSmoke(second);
            assertEquals(200, second.status(), "second smoke response must independently be HTTP 200");
            assertEquals(first.body(), second.body(), "readback remains the same after a new Spring server instance");
            assertEquals(1, witnessCount(), "browser GETs do not mutate the fixture");
        } finally {
            postgresDataSource = null;
        }
    }

    @Test @Tag("learnerAcceptance")
    void day55RealBrowserLoadsOwnerScopedPreviewFromSpringAndPostgres() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            postgresDataSource = postgres.getPostgresDatabase();
            initializePolicySchema();
            seedPolicy();
            var beforeFirst = PolicyRenewalPostgresAcceptanceTest.allRows(postgresDataSource);
            BrowserResult first;
            try (ConfigurableApplicationContext app = startServer(LearnerApplication.class, LearnerConfiguration.class)) {
                first = runBrowser(port(app), OUTPUT.resolve("learner-day55-original.json"), "learner", "renewal-demo");
                assertBrowserResponse(first, "renewal-demo", "home-secure-v1", "home-secure-rate-v1", "125.00", 3, "2027-12-01");
                assertEquals(beforeFirst, PolicyRenewalPostgresAcceptanceTest.allRows(postgresDataSource),
                        "the original browser GET preserves every lifecycle row");
                insertChangedPolicy();
                var beforeChanged = PolicyRenewalPostgresAcceptanceTest.allRows(postgresDataSource);
                BrowserResult changed = runBrowser(port(app), OUTPUT.resolve("learner-day55-changed.json"), "learner", "renewal-demo-v2");
                assertBrowserResponse(changed, "renewal-demo-v2", "home-secure-v9", "home-secure-rate-v4", "987.40", 9, "2027-12-10");
                assertEquals(beforeChanged, PolicyRenewalPostgresAcceptanceTest.allRows(postgresDataSource),
                        "the changed browser GET preserves every lifecycle row");
            }
        } finally {
            postgresDataSource = null;
        }
    }

    @Test @Tag("learnerAcceptance")
    void day56PreviewReadIsStableAcrossRestartAndDoesNotWrite() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            postgresDataSource = postgres.getPostgresDatabase();
            initializePolicySchema();
            seedPolicy();
            var before = PolicyRenewalPostgresAcceptanceTest.allRows(postgresDataSource);
            BrowserResult first;
            try (ConfigurableApplicationContext app = startServer(LearnerApplication.class, LearnerConfiguration.class)) {
                first = runBrowser(port(app), OUTPUT.resolve("learner-day56-first.json"), "learner", "renewal-demo");
                assertBrowserResponse(first, "renewal-demo", "home-secure-v1", "home-secure-rate-v1", "125.00", 3, "2027-12-01");
                assertEquals(before, PolicyRenewalPostgresAcceptanceTest.allRows(postgresDataSource),
                        "full database rows match baseline immediately after first GET while server is alive");
            }
            assertEquals(before, PolicyRenewalPostgresAcceptanceTest.allRows(postgresDataSource),
                    "full database rows match the baseline after the first read");
            BrowserResult second;
            try (ConfigurableApplicationContext app = startServer(LearnerApplication.class, LearnerConfiguration.class)) {
                assertEquals(before, PolicyRenewalPostgresAcceptanceTest.allRows(postgresDataSource),
                        "starting the second Spring server context does not change database rows");
                second = runBrowser(port(app), OUTPUT.resolve("learner-day56-restart.json"), "learner", "renewal-demo");
                assertBrowserResponse(second, "renewal-demo", "home-secure-v1", "home-secure-rate-v1", "125.00", 3, "2027-12-01");
                assertEquals(before, PolicyRenewalPostgresAcceptanceTest.allRows(postgresDataSource),
                        "full database rows match baseline immediately after second GET while server is alive");
            }
            assertEquals(200, first.status()); assertEquals(200, second.status(), "second response independently remains HTTP 200");
            assertEquals(new tools.jackson.databind.ObjectMapper().readTree(first.body()),
                    new tools.jackson.databind.ObjectMapper().readTree(second.body()), "semantic wire fields match after restart");
            assertEquals(before, PolicyRenewalPostgresAcceptanceTest.allRows(postgresDataSource),
                    "all policy, event and idempotency rows match the baseline after restart and second read");
        } finally {
            postgresDataSource = null;
        }
    }

    private static ConfigurableApplicationContext startServer(Class<?> application, Class<?> configuration) {
        return new SpringApplicationBuilder(application, configuration).web(WebApplicationType.SERVLET)
                .properties("server.port=0", "spring.main.banner-mode=off", "logging.level.root=ERROR").run();
    }

    private static int port(ConfigurableApplicationContext context) {
        return ((WebServerApplicationContext) context).getWebServer().getPort();
    }

    private static BrowserResult runBrowser(int port, Path output, String mode, String policyId) throws Exception {
        Files.createDirectories(output.toAbsolutePath().getParent());
        Path log = output.resolveSibling(output.getFileName() + ".log");
        Files.deleteIfExists(output);
        Files.deleteIfExists(log);
        var process = new ProcessBuilder("node", "e2e/day45-renewal-preview-browser.mjs",
                "http://127.0.0.1:" + port + "/__e2e/day45-renewal-preview?policyId=" + policyId,
                output.toString(), mode)
                .directory(Path.of("frontend").toFile()).redirectErrorStream(true).redirectOutput(log.toFile());
        String chromium = System.getenv("CHROMIUM_BIN");
        if (chromium != null && !chromium.isBlank()) process.environment().put("CHROMIUM_BIN", chromium);
        Process runner = process.start();
        if (!runner.waitFor(45, java.util.concurrent.TimeUnit.SECONDS)) {
            runner.destroyForcibly(); runner.waitFor();
            throw new IllegalStateException("Playwright timed out: " + Files.readString(log));
        }
        if (!Files.exists(output)) throw new IllegalStateException("browser runner produced no fresh evidence; " + Files.readString(log));
        var json = new tools.jackson.databind.ObjectMapper().readTree(Files.readString(output));
        return new BrowserResult(json.get("browserOpened").asBoolean(), json.get("status").isNumber() ? json.get("status").asInt() : null,
                json.get("body").isTextual() ? json.get("body").asString() : json.get("body").toString(),
                json.get("visibleText").asString(), json.get("apiRequests").toString(), runner.exitValue(),
                json.hasNonNull("failure") ? json.get("failure").asString() : null);
    }

    private static void createWitnessTable() throws Exception {
        try (Connection c = postgresDataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE renewal_preview_transport (id TEXT PRIMARY KEY, preview_json JSONB NOT NULL)");
        }
    }

    private static void insertWitness() throws Exception {
        try (Connection c = postgresDataSource.getConnection(); var s = c.prepareStatement("INSERT INTO renewal_preview_transport VALUES ('renewal-demo', ?::jsonb)")) {
            s.setString(1, FIXTURE_JSON); s.executeUpdate();
        }
    }

    private static long witnessCount() throws Exception {
        try (Connection c = postgresDataSource.getConnection(); Statement s = c.createStatement(); var rows = s.executeQuery("SELECT count(*) FROM renewal_preview_transport")) {
            assertTrue(rows.next()); return rows.getLong(1);
        }
    }

    private static void initializePolicySchema() throws Exception {
        try (Connection c = postgresDataSource.getConnection(); Statement s = c.createStatement()) {
            for (String sql : Files.readString(Path.of("src/test/resources/fixtures/continuation-31-44/policy-lifecycle.sql")).split(";"))
                if (!sql.isBlank()) s.execute(sql);
        }
    }

    private static void seedPolicy() throws Exception {
        try (Connection c = postgresDataSource.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("INSERT INTO training_policy_lifecycle VALUES ('renewal-demo','owner-a','journey-hidden','quote-hidden','home-secure-v1','home-secure-rate-v1',125.00,'INR',DATE '2026-12-01','ISSUED',3,NULL)");
            s.executeUpdate("INSERT INTO training_policy_lifecycle_event VALUES ('renewal-demo',3,'ISSUED',DATE '2026-12-01',NULL,NULL)");
            s.executeUpdate("INSERT INTO training_policy_idempotency VALUES ('owner-a','renewal-demo-request','opaque-original','renewal-demo')");
        }
    }

    private static void insertChangedPolicy() throws Exception {
        try (Connection c = postgresDataSource.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("INSERT INTO training_policy_lifecycle VALUES ('renewal-demo-v2','owner-a','journey-v2','quote-v2','home-secure-v9','home-secure-rate-v4',987.40,'INR',DATE '2026-12-10','ISSUED',9,NULL)");
            s.executeUpdate("INSERT INTO training_policy_lifecycle_event VALUES ('renewal-demo-v2',9,'ISSUED',DATE '2026-12-10',NULL,NULL)");
            s.executeUpdate("INSERT INTO training_policy_idempotency VALUES ('owner-a','renewal-demo-v2-request','opaque-second','renewal-demo-v2')");
        }
    }

    private static void assertBrowserResponse(BrowserResult response, String id, String product, String rate,
                                              String premium, int version, String nextDate) throws Exception {
        assertTrue(response.browserOpened(), "browser opened; " + response.failure());
        assertEquals(0, response.exitCode(), "browser assertions pass; " + response.failure());
        assertEquals(200, response.status());
        var json = new tools.jackson.databind.ObjectMapper().readTree(response.body());
        assertEquals(java.util.Set.of("policyId", "sourcePolicyVersion", "productVersion", "rateVersion",
                "currentAnnualPremiumRupees", "currency", "nextTermEffectiveDate", "notice"), jsonFieldNames(json));
        assertEquals(id, json.get("policyId").asString()); assertEquals(version, json.get("sourcePolicyVersion").asInt());
        assertTrue(json.get("sourcePolicyVersion").isIntegralNumber(), "wire version must be an integral JSON number");
        assertEquals((long) version, json.get("sourcePolicyVersion").asLong());
        assertEquals(product, json.get("productVersion").asString()); assertEquals(rate, json.get("rateVersion").asString());
        assertTrue(json.get("currentAnnualPremiumRupees").isNumber());
        assertEquals(0, new BigDecimal(premium).compareTo(json.get("currentAnnualPremiumRupees").decimalValue()),
                "wire JSON preserves numeric value; display scale is applied by the client adapter");
        assertEquals("INR", json.get("currency").asString()); assertEquals(nextDate, json.get("nextTermEffectiveDate").asString());
        assertEquals("Reference only; not a renewal offer or coverage commitment.", json.get("notice").asString());
        assertTrue(response.visibleText().contains(id)); assertTrue(response.visibleText().contains(product));
        assertTrue(response.visibleText().contains(rate)); assertTrue(response.visibleText().contains(premium + " INR"));
        assertTrue(response.visibleText().contains(nextDate)); assertTrue(response.visibleText().contains("Version " + version));
        assertTrue(response.visibleText().contains("Reference only; not a renewal offer or coverage commitment."));
        assertEquals("[{\"method\":\"GET\",\"path\":\"/api/policies/" + id + "/renewal-preview\"}]", response.apiRequests());
    }

    private static java.util.Set<String> jsonFieldNames(tools.jackson.databind.JsonNode node) {
        var result = new java.util.HashSet<String>(); result.addAll(node.propertyNames()); return result;
    }

    private static void assertSuccessfulSmoke(BrowserResult result) {
        assertTrue(result.browserOpened(), "smoke browser opened");
        assertEquals(0, result.exitCode(), "smoke runner exited successfully");
        assertNull(result.failure(), "smoke runner reported no failure");
        assertEquals(200, result.status(), "smoke HTTP status is successful");
    }

    record BrowserResult(boolean browserOpened, Integer status, String body, String visibleText,
                         String apiRequests, int exitCode, String failure) { }

    @SpringBootConfiguration @EnableAutoConfiguration
    static class TransportApplication { }

    @Configuration(proxyBeanMethods = false)
    @EnableWebSecurity
    @Import(TransportController.class)
    static class TransportConfiguration implements WebMvcConfigurer {
        @Bean SecurityFilterChain transportHarnessSecurity(HttpSecurity http) throws Exception {
            return http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).csrf(csrf -> csrf.disable()).build();
        }
        @Override public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/assets/**")
                    .addResourceLocations(Path.of("frontend/dist/assets").toAbsolutePath().toUri().toString());
        }
    }

    @RestController
    static class TransportController {
        @GetMapping(value = "/__e2e/day45-renewal-preview", produces = MediaType.TEXT_HTML_VALUE)
        String page() { return """
                <!doctype html><html><body><main id="root">
                <button type="button" id="preview">Check preview</button>
                <output id="result" aria-live="polite"></output>
                <script>document.querySelector('#preview').onclick=async()=>{const r=await fetch('/api/policies/renewal-demo/renewal-preview');const body=await r.json();document.querySelector('#result').textContent=JSON.stringify(body);};</script>
                </main></body></html>
                """; }

        @GetMapping(value = "/api/policies/renewal-demo/renewal-preview", produces = MediaType.APPLICATION_JSON_VALUE)
        Map<String, Object> preview() throws Exception {
            try (Connection c = postgresDataSource.getConnection(); var s = c.prepareStatement("SELECT preview_json::text FROM renewal_preview_transport WHERE id='renewal-demo'"); var rows = s.executeQuery()) {
                if (!rows.next()) throw new IllegalStateException("renewal preview witness missing");
                return new tools.jackson.databind.ObjectMapper().readValue(rows.getString(1),
                        new tools.jackson.core.type.TypeReference<Map<String, Object>>() { });
            }
        }
    }

    @SpringBootConfiguration @EnableAutoConfiguration
    static class LearnerApplication { }

    @Configuration(proxyBeanMethods = false)
    @EnableWebSecurity
    @Import(LearnerPageController.class)
    static class LearnerConfiguration implements WebMvcConfigurer {
        @Bean SecurityFilterChain learnerBrowserHarnessSecurity(HttpSecurity http) throws Exception {
            return http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).csrf(csrf -> csrf.disable()).build();
        }
        @Override public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/assets/**")
                    .addResourceLocations(Path.of("frontend/dist/assets").toAbsolutePath().toUri().toString());
        }
        @Bean PolicyRenewalReadStore policyRenewalReadStore() { return new JdbcPolicyRenewalReadStore(postgresDataSource); }
        @Bean PolicyRenewalPreviewPolicy policyRenewalPreviewPolicy() { return new PolicyRenewalPreviewPolicy(); }
        @Bean Clock policyRenewalClock() { return Clock.fixed(Instant.parse("2027-11-15T12:00:00Z"), ZoneOffset.UTC); }
        @Bean PolicyRenewalPreviewController policyRenewalPreviewController(PolicyRenewalReadStore store,
                PolicyRenewalPreviewPolicy policy, Clock clock) { return new PolicyRenewalPreviewController(store, policy, clock); }
        @Bean FilterRegistrationBean<OwnerPrincipalFilter> ownerPrincipalFilter() {
            var registration = new FilterRegistrationBean<>(new OwnerPrincipalFilter());
            registration.addUrlPatterns("/api/policies/*");
            return registration;
        }
    }

    @RestController
    static class LearnerPageController {
        @GetMapping(value = "/__e2e/day45-renewal-preview", produces = MediaType.TEXT_HTML_VALUE)
        String page() throws IOException { return Files.readString(Path.of("frontend/dist/index.html")); }
    }

    static class OwnerPrincipalFilter implements jakarta.servlet.Filter {
        @Override public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response,
                                       FilterChain chain) throws IOException, ServletException {
            HttpServletRequest http = (HttpServletRequest) request;
            var wrapped = new HttpServletRequestWrapper(http) {
                @Override public java.security.Principal getUserPrincipal() { return () -> "owner-a"; }
            };
            chain.doFilter(wrapped, response);
        }
    }
}
