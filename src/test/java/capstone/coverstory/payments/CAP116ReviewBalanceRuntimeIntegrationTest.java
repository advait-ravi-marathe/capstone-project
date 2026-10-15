package capstone.coverstory.payments;

import coverstory.CoverStoryApplication;
import coverstory.modules.application.api.AcceptedOfferRevisionFactsPort;
import coverstory.modules.payments.api.ConfirmedPaymentEffectFactsPort;
import coverstory.modules.payments.api.PendingAdjustmentFactsPort;
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
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Test-only HTTP read flow through Spring, public facts ports, and disposable PostgreSQL. */
class CAP116ReviewBalanceRuntimeIntegrationTest {
    private static final String HEADER = "X-CAP116-Instructor-Session";
    private static final String OWNER = "11600000-0000-4000-8000-000000000001";

    @Test void reportsExactAcceptedRevisionAndReservationsWithoutCreatingMoneyEffects() throws Exception {
        try (CAP116TestDatabase database = CAP116TestDatabase.fresh()) {
            String url, user;
            try (Connection c = database.postgres().getPostgresDatabase().getConnection()) {
                url = c.getMetaData().getURL(); user = c.getMetaData().getUserName();
            }
            var fixture = new tools.jackson.databind.ObjectMapper().readTree(Files.readString(Path.of("docs/coverstory/fixtures/cap-116.json")));
            int beforeObservations = database.jdbc().queryForObject("SELECT count(*) FROM cap116_harness.effect_observation", Integer.class);
            int beforeAllocations = database.jdbc().queryForObject("SELECT count(*) FROM cap116_harness.pending_allocation", Integer.class);
            try (ConfigurableApplicationContext app = start(url, user)) {
                int port = ((WebServerApplicationContext) app).getWebServer().getPort();
                var first = fixture.path("scenarios").get(0);
                UUID applicationId = UUID.fromString(first.path("accepted_offer").path("application_id").asText());
                var authenticated = request(port, applicationId, true);
                assertEquals(200, authenticated.statusCode(), authenticated.body());
                var anonymous = request(port, applicationId, false);
                assertEquals(401, anonymous.statusCode(), anonymous.body());
                var report = new tools.jackson.databind.ObjectMapper().readTree(authenticated.body());
                assertEquals("o2", report.path("acceptedRevision").asText());
                assertEquals(first.path("accepted_offer").path("content_sha256").asText(), report.path("acceptedContentSha256").asText());
                assertEquals(100000, report.path("confirmedNetMinor").asLong());
                assertEquals(10000, report.path("adjustmentMinor").asLong());
                assertEquals("CREATE_ADDITIONAL_PAYMENT", report.path("nextAction").asText());

                for (int i = 1; i < fixture.path("scenarios").size(); i++) {
                    var scenario = fixture.path("scenarios").get(i);
                    var accepted = scenario.path("accepted_offer");
                    UUID appId = UUID.fromString(accepted.path("application_id").asText());
                    var response = request(port, appId, true);
                    assertEquals(200, response.statusCode(), response.body());
                    var actual = new tools.jackson.databind.ObjectMapper().readTree(response.body());
                    var expected = scenario.path("expected");
                    for (String key : List.of("confirmed_net_minor", "pending_additional_minor", "pending_refund_minor", "adjustment_minor"))
                        assertEquals(expected.path(key).asLong(), actual.path(camel(key)).asLong(), scenario.path("id").asText()+" "+key);
                    assertEquals(expected.path("next_action").asText(), actual.path("nextAction").asText());
                    for (var code : expected.path("discrepancy_codes")) assertTrue(actual.path("discrepancyCodes").toString().contains(code.asText()), actual.toString());
                    assertEquals(accepted.path("offer_id").asText(), actual.path("acceptedOfferId").asText());
                }
                assertEquals(404, request(port, UUID.fromString("11600000-0000-4000-8000-000000000999"), true).statusCode());
            }
            assertEquals(beforeObservations, database.jdbc().queryForObject("SELECT count(*) FROM cap116_harness.effect_observation", Integer.class));
            assertEquals(beforeAllocations, database.jdbc().queryForObject("SELECT count(*) FROM cap116_harness.pending_allocation", Integer.class));
            assertEquals(0, database.jdbc().queryForObject("SELECT count(*) FROM information_schema.tables WHERE table_schema='cap116_harness' AND table_name LIKE '%created_payment%'", Integer.class));
        }
    }

    private static String camel(String snake) {
        StringBuilder out = new StringBuilder(); boolean upper = false;
        for (char c : snake.toCharArray()) { if (c == '_') upper = true; else { out.append(upper ? Character.toUpperCase(c) : c); upper = false; } }
        return out.toString();
    }
    private static HttpResponse<String> request(int port, UUID applicationId, boolean authenticated) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api/payments/__instructor/cap116/"+applicationId)).header("Accept","application/json");
        if (authenticated) builder.header(HEADER, OWNER);
        return HttpClient.newHttpClient().send(builder.GET().build(), HttpResponse.BodyHandlers.ofString());
    }
    private static ConfigurableApplicationContext start(String url, String user) {
        return new SpringApplicationBuilder(CoverStoryApplication.class, RuntimeConfiguration.class).web(WebApplicationType.SERVLET)
                .properties("server.port=0", "spring.main.banner-mode=off", "logging.level.root=ERROR", "spring.profiles.active=cap116-instructor-test",
                        "spring.datasource.url="+url, "spring.datasource.username="+user, "spring.datasource.password=").run();
    }

    @Configuration @Profile("cap116-instructor-test") @Import(CAP116InstructorController.class)
    static class RuntimeConfiguration {
        @Bean javax.sql.DataSource cap116DataSource(org.springframework.core.env.Environment env) {
            return new DriverManagerDataSource(env.getRequiredProperty("spring.datasource.url"), env.getRequiredProperty("spring.datasource.username"), "");
        }
        @Bean AcceptedOfferRevisionFactsPort acceptedOfferFacts(javax.sql.DataSource ds) { return new CAP116ApplicationFactsAdapter(ds); }
        @Bean CAP116PaymentFactsAdapter paymentFacts(javax.sql.DataSource ds) { return new CAP116PaymentFactsAdapter(ds); }
        @Bean CAP116ReferenceBalanceCalculator referenceBalance() { return new CAP116ReferenceBalanceCalculator(); }
        @Bean FilterRegistrationBean<Filter> syntheticPrincipal() {
            FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
            registration.setFilter((request,response,chain) -> {
                String supplied = ((HttpServletRequest)request).getHeader(HEADER);
                if (OWNER.equals(supplied)) chain.doFilter(new HttpServletRequestWrapper((HttpServletRequest)request) {
                    @Override public Principal getUserPrincipal() { return () -> OWNER; }
                }, response); else chain.doFilter(request,response);
            });
            registration.setUrlPatterns(List.of("/api/payments/__instructor/cap116/*")); return registration;
        }
    }
}
