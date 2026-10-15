package coverstory.modules.payments;

import coverstory.CoverStoryApplication;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import jakarta.servlet.Filter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Principal;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/** Actual application HTTP path with an explicitly bounded test-only principal fixture. */
class CAP173PaymentAuditRuntimeIntegrationTest {
    @Test void configuredRouteRequiresTestPrincipalAndStopsAtPreservedLearnerTodo() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start()) {
            String url;
            try (Connection c = pg.getPostgresDatabase().getConnection()) {
                url = c.getMetaData().getURL();
                CAP173InstructorInfrastructureTest.loadSchema(c);
                CAP173InstructorInfrastructureTest.seed(c);
            }
            try (ConfigurableApplicationContext app = new SpringApplicationBuilder(
                    CoverStoryApplication.class, CAP173TestPrincipalConfiguration.class)
                    .web(WebApplicationType.SERVLET)
                    .properties("server.port=0", "spring.main.banner-mode=off", "logging.level.root=ERROR",
                            "coverstory.application.jdbc.url=" + url,
                            "coverstory.application.jdbc.username=postgres",
                            "coverstory.application.jdbc.password=")
                    .run()) {
                int port = ((WebServerApplicationContext) app).getWebServer().getPort();
                HttpClient client = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();
                String path = "/api/v1/me/payments/audit?startInclusive=2026-12-15T00:00:00Z&endExclusive=2026-12-16T00:00:00Z";
                assertEquals(401, request(client, port, path, null).statusCode(), "anonymous request must stop before audit lookup");
                HttpResponse<String> learner = request(client, port, path, "cap173-test-a");
                assertEquals(501, learner.statusCode(), learner.body());
                assertTrue(learner.body().contains("CAP173_NOT_IMPLEMENTED"), "trusted test principal reaches only the preserved learner TODO");
            }
        }
    }

    private static HttpResponse<String> request(HttpClient client, int port, String path, String session) throws Exception {
        var b = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET();
        if (session != null) b.header("Cookie", "cap173_test_session=" + session);
        return client.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Configuration(proxyBeanMethods = false)
    static class CAP173TestPrincipalConfiguration {
        @Bean FilterRegistrationBean<Filter> cap173TestPrincipalFilter() {
            Filter filter = (request, response, chain) -> {
                HttpServletRequest http = (HttpServletRequest) request;
                if (http.getUserPrincipal() != null) { chain.doFilter(request, response); return; }
                String owner = null;
                Cookie[] cookies = http.getCookies();
                if (cookies != null) for (Cookie cookie : cookies) if (cookie.getName().equals("cap173_test_session")) {
                    owner = switch (cookie.getValue()) {
                        case "cap173-test-a" -> CAP173InstructorInfrastructureTest.OWNER_A.toString();
                        case "cap173-test-b" -> CAP173InstructorInfrastructureTest.OWNER_B.toString();
                        default -> null;
                    };
                }
                if (owner == null) { chain.doFilter(request, response); return; }
                String trustedOwner = owner;
                chain.doFilter(new HttpServletRequestWrapper(http) {
                    @Override public Principal getUserPrincipal() { return () -> trustedOwner; }
                }, response);
            };
            FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(filter);
            registration.setOrder(-100); registration.addUrlPatterns("/api/*"); registration.setName("cap173TestPrincipalFilter");
            return registration;
        }
    }
}
