package coverstory.modules.catalog;

import coverstory.CoverStoryApplication;
import coverstory.modules.catalog.api.CAP57Seam;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import jakarta.servlet.Filter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Principal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.*;

/** Learner-mode route tests call CAP57Seam directly; instructor oracle tests stay separate. */
@Tag("learnerAcceptance")
class CAP57LearnerRouteTransactionTest {
    private static final UUID EDITOR = UUID.fromString("57000000-0000-4000-8000-000000000001");
    private static final String EDITOR_HEADER = "X-CAP57-Learner-Session";
    private static final UUID PRODUCT = UUID.fromString("5f1b8b02-1b68-4de8-928c-f3eaa5f80001");
    private static final UUID VERSION = UUID.fromString("a8100000-0000-4000-8000-000000000001");
    private static final String HASH = "cb4a74791080a86290ead23a0e21db92cb5a0e8a77fd612830683158b5bd8160";

    @Test void learnerRoutePersistsPublicationAndItsAuditTogether() throws Exception {
        try (CAP57TestDatabase database = CAP57TestDatabase.fresh()) {
            try (ConfigurableApplicationContext app = start(database, new AtomicBoolean())) {
                HttpResponse<String> response = publish(app);
                assertEquals(201, response.statusCode(), response.body());
                assertEquals(1, count(database.postgres(), "SELECT count(*) FROM catalog.active_health_offer"));
                assertEquals(1, count(database.postgres(), "SELECT count(*) FROM catalog.publication_audit_event"));
                assertEquals(1, count(database.postgres(), """
                    SELECT count(*) FROM catalog.publication_audit_event a
                    JOIN catalog.active_health_offer p USING (product_id, version_id)
                    WHERE a.editor_id='57000000-0000-4000-8000-000000000001'
                      AND a.product_id='5f1b8b02-1b68-4de8-928c-f3eaa5f80001'
                      AND a.version_id='a8100000-0000-4000-8000-000000000001'
                      AND encode(a.content_sha256,'hex')='cb4a74791080a86290ead23a0e21db92cb5a0e8a77fd612830683158b5bd8160'
                    """));
            }
        }
    }

    @Test void learnerRouteFailureLeavesNeitherPublicationNorAuditRow() throws Exception {
        try (CAP57TestDatabase database = CAP57TestDatabase.fresh()) {
            AtomicBoolean failBeforeCommit = new AtomicBoolean(true);
            try (ConfigurableApplicationContext app = start(database, failBeforeCommit)) {
                HttpResponse<String> response = publish(app);
                assertEquals(503, response.statusCode(), response.body());
                assertEquals(0, count(database.postgres(), "SELECT count(*) FROM catalog.active_health_offer"));
                assertEquals(0, count(database.postgres(), "SELECT count(*) FROM catalog.publication_audit_event"));
            }
        }
    }

    private static ConfigurableApplicationContext start(CAP57TestDatabase database, AtomicBoolean fail) throws Exception {
        String url, username;
        try (Connection c = database.postgres().getPostgresDatabase().getConnection()) {
            url = c.getMetaData().getURL(); username = c.getMetaData().getUserName();
        }
        return new SpringApplicationBuilder(CoverStoryApplication.class, LearnerRouteConfiguration.class)
                .web(WebApplicationType.SERVLET)
                .properties("server.port=0", "spring.main.banner-mode=off", "logging.level.root=ERROR",
                        "spring.profiles.active=cap57-learner-route-test", "coverstory.catalog.jdbc.url=" + url,
                        "coverstory.catalog.jdbc.username=" + username, "coverstory.catalog.jdbc.password=")
                .initializers(context -> context.getBeanFactory().registerSingleton("cap57LearnerFailureSwitch", fail))
                .run();
    }

    private static HttpResponse<String> publish(ConfigurableApplicationContext app) throws Exception {
        int port = ((WebServerApplicationContext) app).getWebServer().getPort();
        String json = "{\"productId\":\"" + PRODUCT + "\",\"versionId\":\"" + VERSION
                + "\",\"contentSha256\":\"" + HASH + "\",\"amountMinor\":123456,\"currency\":\"INR\"}";
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                        + "/api/catalog/__learner/cap57/publish"))
                .header("Content-Type", "application/json").header(EDITOR_HEADER, "synthetic-cap57-editor")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static int count(EmbeddedPostgres pg, String sql) throws Exception {
        try (Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            assertTrue(rs.next()); return rs.getInt(1);
        }
    }

    @Configuration @Profile("cap57-learner-route-test")
    static class LearnerRouteConfiguration {
        @Bean @Primary DriverManagerDataSource learnerDataSource(Environment environment) {
            DriverManagerDataSource source = new DriverManagerDataSource();
            source.setDriverClassName("org.postgresql.Driver");
            source.setUrl(environment.getRequiredProperty("coverstory.catalog.jdbc.url"));
            source.setUsername(environment.getRequiredProperty("coverstory.catalog.jdbc.username"));
            source.setPassword(environment.getProperty("coverstory.catalog.jdbc.password", ""));
            return source;
        }

        @Bean CAP57Seam cap57LearnerSeam(DriverManagerDataSource source, AtomicBoolean cap57LearnerFailureSwitch) {
            return new CAP57Seam(source, () -> { if (cap57LearnerFailureSwitch.get()) throw new java.sql.SQLException("synthetic pre-commit fault"); });
        }

        @Bean FilterRegistrationBean<Filter> testEditorIdentity() {
            FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
            registration.setFilter((request, response, chain) -> {
                var http = (jakarta.servlet.http.HttpServletRequest) request;
                if ("synthetic-cap57-editor".equals(http.getHeader(EDITOR_HEADER))) {
                    http = new jakarta.servlet.http.HttpServletRequestWrapper(http) {
                        @Override public Principal getUserPrincipal() { return () -> EDITOR.toString(); }
                    };
                }
                chain.doFilter(http, response);
            });
            registration.setUrlPatterns(List.of("/api/catalog/__learner/cap57/*"));
            return registration;
        }
    }

    @RestController
    @Profile("cap57-learner-route-test")
    @RequestMapping("/api/catalog/__learner/cap57")
    static class LearnerPublicationController {
        private final CAP57Seam seam;
        LearnerPublicationController(CAP57Seam seam) { this.seam = seam; }

        @PostMapping("/publish")
        org.springframework.http.ResponseEntity<?> publish(@RequestBody PublishRequest request,
                jakarta.servlet.http.HttpServletRequest servletRequest) {
            Principal principal = servletRequest.getUserPrincipal();
            if (principal == null || !EDITOR.toString().equals(principal.getName()))
                return org.springframework.http.ResponseEntity.status(401).build();
            try {
                CAP57Seam.PublishResult result = seam.publish(UUID.fromString(principal.getName()),
                        new CAP57Seam.PreparedVersion(request.productId(), request.versionId(),
                        java.util.HexFormat.of().parseHex(request.contentSha256()), request.amountMinor(), request.currency()));
                return org.springframework.http.ResponseEntity.status(result.statusCode()).body(result);
            } catch (UnsupportedOperationException learnerTodo) {
                return org.springframework.http.ResponseEntity.status(501).body(new SafeError("CAP57_LEARNER_TODO"));
            }
        }
        record SafeError(String code) { }
    }

    record PublishRequest(UUID productId, UUID versionId, String contentSha256, long amountMinor, String currency) { }
}
