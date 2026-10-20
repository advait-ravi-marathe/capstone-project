package assignments.reboot;

import assignments.insurance.*;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Infrastructure-only Chromium -> Spring MVC -> disposable PostgreSQL -> server restart smoke. */
class Day44BrowserTransportInfrastructureTest {
    private static volatile DataSource postgresDataSource;
    private static final Clock lifecycleClock = Clock.fixed(Instant.parse("2026-12-10T12:00:00Z"), ZoneOffset.UTC);

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
            try (ConfigurableApplicationContext first = startServer()) {
                firstPort = port(first);
                runBrowser("http://127.0.0.1:" + firstPort + "/__e2e/day44-lifecycle", "write", writeJson);
                assertTrue(Files.readString(writeJson).contains("lifecycle-smoke-v1:persisted-after-server-restart"));
            }
            try (ConfigurableApplicationContext restarted = startServer()) {
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
            try (ConfigurableApplicationContext app = startServer()) {
                runBrowser("http://127.0.0.1:" + port(app) + "/__e2e/day44-lifecycle", "flow", result);
            }
            var json = new tools.jackson.databind.ObjectMapper().readTree(Files.readString(result));
            var flow = json.get("flow").asString();
            assertTrue(json.get("browserOpened").asBoolean());
            Path restartResult = Path.of("target/day44-harness/lifecycle-restart.json").toAbsolutePath();
            try (ConfigurableApplicationContext restarted = startServer()) {
                runBrowser("http://127.0.0.1:" + port(restarted) + "/__e2e/day44-lifecycle", "lifecycle-read", restartResult);
            }
            String restart = new tools.jackson.databind.ObjectMapper().readTree(Files.readString(restartResult)).get("lifecycleReadback").asString();
            for (String route : List.of("issue", "replay", "endorsement", "cancellation", "history"))
                assertTrue(flow.contains("/test/day44/lifecycle/" + route + ":200"), "Expected successful browser/API call for " + route + "; got " + flow);
            assertEquals("/test/day44/lifecycle/history:200:CANCELLED:v4:events4", restart);
        }
    }

    private static ConfigurableApplicationContext startServer() {
        return new SpringApplicationBuilder(assignments.insurance.InsuranceDayOneApplication.class, HarnessConfiguration.class)
                .web(WebApplicationType.SERVLET)
                .properties("server.port=0", "spring.main.banner-mode=off", "logging.level.root=ERROR")
                .run();
    }

    private static void initializeLifecycleSchema(DataSource dataSource) throws Exception {
        for (String sql : Files.readString(Path.of("src/test/resources/fixtures/continuation-31-44/policy-lifecycle.sql")).split(";"))
            if (!sql.isBlank()) try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) { s.execute(sql); }
    }

    private static int port(ConfigurableApplicationContext context) {
        return ((WebServerApplicationContext) context).getWebServer().getPort();
    }

    private static void runBrowser(String url, String phase, Path result) throws Exception {
        ProcessBuilder process = new ProcessBuilder("node", "e2e/day44-browser.mjs", url, phase, result.toString())
                .directory(Path.of("frontend").toFile()).redirectErrorStream(true)
                .redirectOutput(Path.of("target/day44-harness", phase + ".log").toFile());
        String chromium = System.getenv("CHROMIUM_BIN");
        if (chromium != null && !chromium.isBlank()) process.environment().put("CHROMIUM_BIN", chromium);
        Process runner = process.start();
        if (!runner.waitFor(45, TimeUnit.SECONDS)) {
            runner.destroyForcibly(); runner.waitFor();
            throw new IllegalStateException("Playwright timed out: " + Files.readString(Path.of("target/day44-harness", phase + ".log")));
        }
        assertEquals(0, runner.exitValue(), Files.readString(Path.of("target/day44-harness", phase + ".log")));
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
    }

    @RestController
    static class Day44HarnessController {
        private final DataSource dataSource;
        private final PolicyLifecycleService service;
        Day44HarnessController(DataSource dataSource, PolicyLifecycleService service) { this.dataSource = dataSource; this.service = service; }

        @GetMapping(value = "/__e2e/day44-lifecycle", produces = MediaType.TEXT_HTML_VALUE)
        String page() throws Exception { return Files.readString(Path.of("frontend/dist/index.html")); }

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

        @PostMapping("/test/day44/lifecycle/issue")
        PolicySnapshot issue() {
            var request = request();
            var facts = new PolicyIssueFacts("owner-a", "owner-a", "journey-a", "quote-a", true, true, LocalDate.parse("2026-12-01"), true);
            if (!service.validateIssue(request, facts)) throw new IllegalArgumentException("quote failed fixed training facts");
            return service.persistIssueAtomically(dataSource, request, "owner-a");
        }

        @PostMapping("/test/day44/lifecycle/replay")
        PolicySnapshot replay() { return service.issue(request(), "owner-a"); }

        @PostMapping("/test/day44/lifecycle/endorsement")
        PolicySnapshot endorse() {
            return service.endorseAtExpectedVersion(dataSource, issued(),
                    new PolicyEndorsementRequest(1, LocalDate.parse("2026-12-02"), "REBUILD_AMOUNT", new BigDecimal("200000.00")));
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
    record Witness(String witnessId, String value) { }
    record LifecycleReadback(String status, long version, long eventCount) { }
}
