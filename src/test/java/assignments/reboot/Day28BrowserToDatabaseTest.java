package assignments.reboot;

import assignments.insurance.*;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;

/** Real React/Chromium -> Spring MVC -> learner components -> disposable PostgreSQL acceptance. */
@Tag("learnerAcceptance")
class Day28BrowserToDatabaseTest {
    private static volatile DataSource browserDataSource;
    private static final AtomicInteger quoteRequests = new AtomicInteger();
    private static final AtomicInteger selectionRequests = new AtomicInteger();

    @Test
    void browserCreatesSelectsAndReloadsHomeSecureQuoteFromPostgres() throws Exception {
        quoteRequests.set(0);
        selectionRequests.set(0);
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            browserDataSource = postgres.getPostgresDatabase();
            initializeFixture(browserDataSource);
            try (ConfigurableApplicationContext backend = new SpringApplicationBuilder(
                    InsuranceDayOneApplication.class, BrowserHarnessConfiguration.class)
                    .web(WebApplicationType.SERVLET)
                    .properties("server.port=0", "spring.main.banner-mode=off", "logging.level.root=ERROR")
                    .run()) {
                int backendPort = ((WebServerApplicationContext) backend).getWebServer().getPort();
                Path browserResult = Path.of("target/day28-browser-result.json").toAbsolutePath();
                Files.createDirectories(browserResult.getParent());
                int browserExit;
                Process runner = new ProcessBuilder("node", "e2e/day28-browser.mjs",
                        "http://127.0.0.1:" + backendPort + "/__e2e/home-secure", browserResult.toString())
                        .directory(Path.of("frontend").toFile())
                        .redirectErrorStream(true).redirectOutput(Path.of("target/day28-playwright.log").toFile())
                        .start();
                if (!runner.waitFor(45, TimeUnit.SECONDS)) {
                    runner.destroyForcibly();
                    runner.waitFor();
                    throw new IllegalStateException("Playwright did not finish: " + Files.readString(Path.of("target/day28-playwright.log")));
                }
                browserExit = runner.exitValue();

                var result = new tools.jackson.databind.ObjectMapper().readTree(Files.readString(browserResult));
                int quoteCount;
                int selectionCount;
                String selectionVersion = null;
                int selectionOrder = -1;
                try (Connection connection = postgres.getPostgresDatabase().getConnection(); Statement statement = connection.createStatement()) {
                    try (var rows = statement.executeQuery("SELECT count(*) FROM quote_versions")) { rows.next(); quoteCount = rows.getInt(1); }
                    try (var rows = statement.executeQuery("SELECT quote_version_id, selection_order FROM selections")) {
                        selectionCount = 0;
                        while (rows.next()) { selectionVersion = rows.getString(1); selectionOrder = rows.getInt(2); selectionCount++; }
                    }
                }

                final int persistedSelectionCount = selectionCount;
                final String persistedSelectionVersion = selectionVersion;
                final int persistedSelectionOrder = selectionOrder;
                List<org.junit.jupiter.api.function.Executable> assertions = new ArrayList<>();
                assertions.add(() -> assertTrue(result.get("browserOpened").asBoolean(), "real browser must load the React route"));
                assertions.add(() -> assertTrue(result.get("formInputsSubmitted").asBoolean(), "browser enters valid HomeSecure fields"));
                assertions.add(() -> assertTrue(result.get("quoteCreated").asBoolean(), "React form creates the quote through Spring"));
                assertions.add(() -> assertTrue(result.get("quoteSnapshotRendered").asBoolean(), "React renders the server snapshot and premium"));
                assertions.add(() -> assertTrue(result.get("selectionSubmitted").asBoolean(), "React selection action is exercised"));
                assertions.add(() -> assertTrue(result.get("summaryUpdated").asBoolean(), "browser summary reflects selected quote"));
                assertions.add(() -> assertTrue(result.get("reloadReadback").asBoolean(), "browser reload reads persisted selection"));
                assertions.add(() -> assertEquals(0, browserExit, "Playwright diagnostics: " + result));
                assertions.add(() -> assertEquals(1, quoteRequests.get(), "quote POST reached real component adapter"));
                assertions.add(() -> assertEquals(1, selectionRequests.get(), "selection POST reached real component adapter"));
                assertions.add(() -> assertEquals(2, quoteCount, "seed and newly created quote are stored in PostgreSQL"));
                assertions.add(() -> assertEquals(1, persistedSelectionCount, "one persisted selection survives browser reload"));
                assertions.add(() -> assertEquals("e2e-quote-v1", persistedSelectionVersion));
                assertions.add(() -> assertEquals(0, persistedSelectionOrder));
                assertAll("complete browser-to-database HomeSecure flow", assertions);
            }
        }
    }

    private static void initializeFixture(DataSource dataSource) throws Exception {
        String ddl = Files.readString(Path.of("src/main/resources/db/migration/V001__homesecure_quotes.sql"));
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            for (String sql : ddl.split(";")) if (!sql.isBlank()) statement.execute(sql);
            statement.execute("ALTER TABLE journeys ADD COLUMN owner_id TEXT");
            statement.execute("INSERT INTO journeys (journey_id, owner_id) VALUES ('journey-day28', 'owner-a')");
            statement.execute("""
                    INSERT INTO quote_versions(journey_id, quote_version_id, product_code, product_version,
                      rate_version, rebuild_amount_rupees, coverage_code, currency, annual_premium_rupees,
                      created_at, expires_at)
                    VALUES ('journey-day28','seed-quote-v1','homesecure','home-secure-v1',
                      'training-rate-v1',100000.00,'BUILDING','INR',125.00,
                      TIMESTAMPTZ '2026-10-20 12:00:00+00',TIMESTAMPTZ '2026-10-20 12:15:00+00')
                    """);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class BrowserHarnessConfiguration implements WebMvcConfigurer {
        @Override public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/assets/**")
                    .addResourceLocations(Path.of("frontend/dist/assets").toAbsolutePath().toUri().toString());
        }

        @Bean DataSource day28DataSource() { return browserDataSource; }
        @Bean BrowserHarnessController browserHarnessController(DataSource dataSource,
                QuoteSelectionRequestValidator validator, QuoteSelectionPolicy policy) {
            return new BrowserHarnessController(dataSource, validator, policy);
        }
    }

    @RestController
    static class BrowserHarnessController {
        private final DataSource dataSource;
        private final QuoteSelectionRequestValidator validator;
        private final QuoteSelectionPolicy policy;
        private final QuoteVersionService quoteService = new QuoteVersionService("homesecure", "home-secure-v1",
                "training-rate-v1", "INR", Duration.ofMinutes(15),
                new HomeSecureEligibilityPolicy("OWNER_OCCUPIED", new java.math.BigDecimal("100000.00"), new java.math.BigDecimal("10000000.00")),
                new PremiumCalculator(new java.math.BigDecimal("0.00125"), Map.of("BUILDING", java.math.BigDecimal.ZERO)),
                Clock.systemUTC(), () -> "e2e-quote-v1");

        BrowserHarnessController(DataSource dataSource, QuoteSelectionRequestValidator validator, QuoteSelectionPolicy policy) {
            this.dataSource = dataSource; this.validator = validator; this.policy = policy;
        }

        @GetMapping(value = "/__e2e/home-secure", produces = MediaType.TEXT_HTML_VALUE)
        String browserPage() throws Exception { return Files.readString(Path.of("frontend/dist/index.html")); }

        @PostMapping(value = "/test/day28/quotes", consumes = MediaType.APPLICATION_JSON_VALUE)
        QuoteView createQuote(@RequestBody QuoteCreationInput input) throws Exception {
            quoteRequests.incrementAndGet();
            QuoteSnapshot quote = quoteService.createQuote(input);
            new QuoteRepositoryAdapter(dataSource).insert("journey-day28", quote);
            return view(quote);
        }

        @GetMapping("/test/day28/quotes/{id}")
        ResponseEntity<?> quote(@PathVariable("id") String id) throws Exception {
            return new QuoteRepositoryAdapter(dataSource).find("journey-day28", id)
                    .<ResponseEntity<?>>map(q -> ResponseEntity.ok(view(q)))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }

        @GetMapping("/test/day28/summary")
        JourneySummary summary() throws Exception {
            try {
                return new JourneySummaryQuery(dataSource).find("journey-day28", "owner-a")
                        .orElseGet(BrowserHarnessController::seedSummary);
            } catch (UnsupportedOperationException learnerTodo) {
                // Keep the seeded downstream path exercisable while the learner-owned
                // summary query is intentionally incomplete.
                return seedSummary();
            }
        }

        private static JourneySummary seedSummary() {
            return new JourneySummary("journey-day28", new JourneyQuoteSummary("seed-quote-v1", "homesecure",
                    "home-secure-v1", "training-rate-v1", new java.math.BigDecimal("100000.00"),
                    "BUILDING", "INR", new java.math.BigDecimal("125.00"),
                    java.time.Instant.parse("2026-10-20T12:00:00Z"),
                    java.time.Instant.parse("2026-10-20T12:15:00Z")), List.of());
        }

        @PostMapping(value = "/test/day28/selections", consumes = MediaType.APPLICATION_JSON_VALUE)
        ResponseEntity<String> select(@RequestBody QuoteSelectionRequest request) throws Exception {
            selectionRequests.incrementAndGet();
            try {
                validator.validate(request);
                new SelectionService(dataSource).persistBatch(policy.select(request.selections()));
                return ResponseEntity.ok("saved");
            } catch (UnsupportedOperationException learnerTodo) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(learnerTodo.getMessage());
            }
        }

        @ExceptionHandler(UnsupportedOperationException.class)
        ResponseEntity<String> todo(UnsupportedOperationException exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
        }

        private static QuoteView view(QuoteSnapshot q) {
            return new QuoteView(q.quoteVersionId(), q.annualPremiumRupees().toPlainString(), q.currency(),
                    q.productVersion(), q.rateVersion(), q.expiresAt().toString());
        }
    }

    record QuoteView(String quoteVersionId, String annualPremiumRupees, String currency,
                     String productVersion, String rateVersion, String expiresAt) { }
}
