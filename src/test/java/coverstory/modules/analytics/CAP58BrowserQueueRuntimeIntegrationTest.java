package coverstory.modules.analytics;

import coverstory.CoverStoryApplication;
import coverstory.modules.analytics.api.CAP58Seam;
import coverstory.modules.policy.api.CollectionConsentPort;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Instructor-only browser -> real Spring route -> disposable Analytics PostgreSQL witness. */
class CAP58BrowserQueueRuntimeIntegrationTest {
    private static final String OWNER = "58000000-0000-4000-8000-000000000999";
    private static final AtomicInteger REQUESTS = new AtomicInteger();

    @Test void boundedBrowserQueueRetriesThroughLiveEndpointAndDoesNotBlockCheckout() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            String url;
            String user;
            try (Connection connection = postgres.getPostgresDatabase().getConnection(); Statement statement = connection.createStatement()) {
                url = connection.getMetaData().getURL();
                user = connection.getMetaData().getUserName();
                statement.execute("CREATE SCHEMA application");
                statement.execute("CREATE TABLE application.application_record(application_id UUID,owner_id UUID,revision BIGINT,PRIMARY KEY(application_id,revision))");
                statement.execute("CREATE TABLE application.journey(journey_id UUID PRIMARY KEY,owner_id UUID,application_id UUID,revision BIGINT)");
                statement.execute("CREATE SCHEMA analytics");
                String ddl = Files.readString(Path.of("src/main/resources/db/schema/constraints.sql"));
                String marker = "CREATE TABLE IF NOT EXISTS analytics.minimal_event_receipt (";
                int start = ddl.indexOf(marker);
                int end = ddl.indexOf(");", start) + 2;
                assertTrue(start >= 0 && end > 2, "analytics-owned receipt schema exists");
                statement.execute(ddl.substring(start, end));
            }

            REQUESTS.set(0);
            String key = Base64.getEncoder().encodeToString(new byte[32]);
            try (ConfigurableApplicationContext app = new SpringApplicationBuilder(
                    CoverStoryApplication.class, BrowserRuntimeConfiguration.class)
                    .web(WebApplicationType.SERVLET)
                    .properties("server.port=0", "spring.main.banner-mode=off", "logging.level.root=ERROR",
                            "spring.profiles.active=cap58-browser-test",
                            "coverstory.application.jdbc.url=" + url,
                            "coverstory.application.jdbc.username=" + user,
                            "coverstory.application.jdbc.password=",
                            "coverstory.analytics.scope-hash-key-base64=" + key)
                    .run()) {
                int apiPort = ((WebServerApplicationContext) app).getWebServer().getPort();
                assertTrue(apiPort > 0);
                runBrowser(apiPort);

                JdbcTemplate jdbc = app.getBean(JdbcTemplate.class);
                assertEquals(6, REQUESTS.get(), "two batches each receive the bounded three-attempt budget");
                assertEquals(10, jdbc.queryForObject("SELECT count(*) FROM analytics.minimal_event_receipt", Integer.class));
                assertEquals(10, jdbc.queryForObject("SELECT count(DISTINCT event_id) FROM analytics.minimal_event_receipt", Integer.class));
                assertEquals(1, jdbc.queryForObject("SELECT count(DISTINCT owner_hash) FROM analytics.minimal_event_receipt", Integer.class));
                assertEquals(1, jdbc.queryForObject("SELECT count(DISTINCT session_scope_hash) FROM analytics.minimal_event_receipt", Integer.class));
                assertEquals(10, jdbc.queryForObject("SELECT count(*) FROM analytics.minimal_event_receipt WHERE event_type='PAGE_VIEW' AND page_id='health_discovery'", Integer.class));
                List<String> columns = jdbc.query("SELECT column_name FROM information_schema.columns WHERE table_schema='analytics' AND table_name='minimal_event_receipt'", (row, n) -> row.getString(1));
                assertEquals(List.of("owner_hash", "event_id", "payload_hash", "session_scope_hash", "event_type",
                        "page_id", "step_code", "target_id", "duration_ms", "server_received_at").stream().sorted().toList(),
                        columns.stream().sorted().toList());
            }
        }
    }

    private static void runBrowser(int apiPort) throws Exception {
        ProcessBuilder builder = new ProcessBuilder("node", "scripts/cap58-browser-queue.mjs", "http://127.0.0.1:" + apiPort)
                .directory(Path.of("web").toFile()).redirectErrorStream(true);
        builder.environment().put("npm_config_cache", "/tmp/npm-cache");
        Process browser = builder.start();
        if (!browser.waitFor(120, TimeUnit.SECONDS)) {
            browser.toHandle().descendants().forEach(ProcessHandle::destroyForcibly);
            browser.destroyForcibly();
            fail("CAP-58 browser-to-API integration timed out");
        }
        String output = new String(browser.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        assertEquals(0, browser.exitValue(), output);
        assertTrue(output.contains("CAP58_BROWSER_QUEUE"), output);
        assertTrue(output.contains("\"mode\":\"real-spring-api\""), output);
        assertTrue(output.contains("\"sent\":10"), output);
        assertTrue(output.contains("\"dropped\":10"), output);
    }

    @Configuration @Profile("cap58-browser-test")
    static class BrowserRuntimeConfiguration {
        @Bean @Primary CAP58Seam cap58InstructorReferenceValidator() { return new CAP58ReferenceValidator(); }

        @Bean @Primary CollectionConsentPort syntheticConsent() { return owner -> true; }

        @Bean FilterRegistrationBean<Filter> syntheticPrincipalForThisTestOnly() {
            FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
            registration.setFilter((request, response, chain) -> chain.doFilter(
                    new HttpServletRequestWrapper((HttpServletRequest) request) {
                        @Override public Principal getUserPrincipal() { return () -> OWNER; }
                    }, response));
            registration.setUrlPatterns(List.of("/api/v1/analytics/events"));
            registration.setOrder(1);
            return registration;
        }

        @Bean FilterRegistrationBean<Filter> instructorRateLimitFixture() {
            FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
            registration.setFilter((request, response, chain) -> {
                if (REQUESTS.incrementAndGet() < 6) {
                    ((jakarta.servlet.http.HttpServletResponse) response).setStatus(429);
                    return;
                }
                chain.doFilter(request, response);
            });
            registration.setUrlPatterns(List.of("/api/v1/analytics/events"));
            registration.setOrder(2);
            return registration;
        }
    }
}
