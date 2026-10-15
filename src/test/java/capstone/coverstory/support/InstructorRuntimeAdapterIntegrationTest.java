package capstone.coverstory.support;

import capstone.coverstory.policy.CAP123RuntimePortAdapter;
import coverstory.CoverStoryApplication;
import coverstory.modules.policy.api.InstructorPolicyHelpController;
import coverstory.modules.policy.api.PolicyHelpReadPort;
import coverstory.modules.support.api.*;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

/** Actual HTTP listener exercises profile-gated module controllers through synthetic instructor ports. */
class InstructorRuntimeAdapterIntegrationTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String HEADER = "X-CoverStory-Instructor-Owner";
    private static final UUID OWNER_A = UUID.fromString("12100000-0000-4000-8000-000000000001");
    private static final UUID OWNER_B = UUID.fromString("12100000-0000-4000-8000-000000000002");

    @Test void lineageAndRerankRoutesAreAuthenticatedBoundedAndReadOnly() throws Exception {
        try (var app = start()) {
            int port = port(app);
            var anonymousLineage = get(port, "/api/support/__instructor/lineage/ai1", null);
            assertEquals(401, anonymousLineage.statusCode(), anonymousLineage.body());
            JsonNode exact = body(get(port, "/api/support/__instructor/lineage/ai1", OWNER_A));
            assertEquals("FOUND", exact.path("status").asText());
            assertEquals("v2", exact.path("versionId").asText());
            assertEquals("m2", exact.path("batchId").asText());
            assertEquals(23, exact.path("startByte").asInt());
            assertEquals(39, exact.path("endByte").asInt());
            assertEquals(0, exact.path("writeCount").asInt());
            JsonNode broken = body(get(port, "/api/support/__instructor/lineage/ai1-v1-only", OWNER_A));
            assertEquals("LINEAGE_BROKEN", broken.path("status").asText());
            assertEquals("EXACT_VERSION_MISSING", broken.path("code").asText());
            String foreign = get(port, "/api/support/__instructor/lineage/bi1", OWNER_A).body();
            assertTrue(foreign.contains("NOT_FOUND"));
            assertFalse(foreign.contains("owner-B"));

            JsonNode timeout = body(get(port, "/api/support/__instructor/rerank/timeout", OWNER_A));
            assertEquals("TIMEOUT_FALLBACK", timeout.path("status").asText());
            assertEquals(1, timeout.path("rerankCalls").asInt());
            assertTrue(timeout.path("timeoutRecorded").asBoolean());
            JsonNode invalid = body(get(port, "/api/support/__instructor/rerank/invented", OWNER_A));
            assertEquals("INVALID_RERANK", invalid.path("status").asText());
            assertTrue(invalid.path("candidateIds").isEmpty());
            assertEquals(400, get(port, "/api/support/__instructor/rerank/arbitrary-evidence-id", OWNER_A).statusCode());
        }
    }

    @Test void policyHelpAndLifecycleEvidenceRoutesNeverExposeMutationsOrClaimCompleteness() throws Exception {
        try (var app = start()) {
            int port = port(app);
            var anonymousPolicy = get(port, "/api/policy/__instructor/help/issued", null);
            assertEquals(401, anonymousPolicy.statusCode(), anonymousPolicy.body());
            JsonNode issued = body(get(port, "/api/policy/__instructor/help/issued", UUID.fromString("12300000-0000-4000-8000-000000000001")));
            assertEquals("ACTIVE_COVER", issued.path("state").asText());
            assertEquals(1, issued.path("policies").size());
            assertEquals("policy-terms-sim-v3", issued.path("citations").get(0).path("version").asText());
            assertEquals(0, issued.path("writes").asInt());
            JsonNode pending = body(get(port, "/api/policy/__instructor/help/pending", UUID.fromString("12300000-0000-4000-8000-000000000001")));
            assertEquals("ISSUANCE_PENDING", pending.path("state").asText());
            assertTrue(pending.path("policies").isEmpty());
            JsonNode denied = body(get(port, "/api/policy/__instructor/help/issued", UUID.fromString("12300000-0000-4000-8000-000000000002")));
            assertEquals("DENY", denied.path("state").asText());
            assertTrue(denied.path("policies").isEmpty());

            String base = "/api/support/__instructor/applications/17700000-0000-4000-8000-000000000011/lifecycle";
            assertEquals(401, get(port, base, null).statusCode());
            JsonNode truncated = body(get(port, base + "?maxRows=2&maxUtf8Bytes=4096", UUID.fromString("17700000-0000-4000-8000-000000000001")));
            assertEquals(List.of("r1", "r2"), List.of(truncated.path("rows").get(0).path("id").asText(), truncated.path("rows").get(1).path("id").asText()));
            assertTrue(truncated.path("truncated").asBoolean());
            assertTrue(truncated.path("safeConclusion").asText().contains("do not establish that dispatch did not happen"));
            assertFalse(truncated.path("safeConclusion").asText().contains("not submitted"));
            JsonNode full = body(get(port, base + "?maxRows=3&maxUtf8Bytes=4096", UUID.fromString("17700000-0000-4000-8000-000000000001")));
            assertEquals(3, full.path("includedCount").asInt());
            int exactBytes = full.path("serializedEnvelopeBytes").asInt();
            JsonNode exact = body(get(port, base + "?maxRows=3&maxUtf8Bytes=" + exactBytes, UUID.fromString("17700000-0000-4000-8000-000000000001")));
            assertEquals(3, exact.path("includedCount").asInt());
            JsonNode oneLess = body(get(port, base + "?maxRows=3&maxUtf8Bytes=" + (exactBytes - 1), UUID.fromString("17700000-0000-4000-8000-000000000001")));
            assertTrue(oneLess.path("truncated").asBoolean());
            assertTrue(oneLess.path("includedCount").asInt() < 3);
            assertEquals(413, get(port, base + "?maxRows=3&maxUtf8Bytes=64", UUID.fromString("17700000-0000-4000-8000-000000000001")).statusCode());
            assertEquals(400, get(port, base + "?maxRows=51&maxUtf8Bytes=4096", UUID.fromString("17700000-0000-4000-8000-000000000001")).statusCode());
            assertEquals(404, get(port, "/api/support/__instructor/applications/17700000-0000-4000-8000-000000000099/lifecycle", UUID.fromString("17700000-0000-4000-8000-000000000001")).statusCode());
        }
    }

    private static ConfigurableApplicationContext start() {
        return new SpringApplicationBuilder(CoverStoryApplication.class, RuntimeConfiguration.class)
                .profiles("coverstory-instructor-runtime")
                .web(WebApplicationType.SERVLET).properties("server.port=0", "spring.main.banner-mode=off",
                        "logging.level.root=ERROR").run();
    }
    private static int port(ConfigurableApplicationContext app) { return ((WebServerApplicationContext) app).getWebServer().getPort(); }
    private static HttpResponse<String> get(int port, String path, UUID owner) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET();
        if (owner != null) request.header(HEADER, owner.toString());
        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
    private static JsonNode body(HttpResponse<String> response) throws Exception {
        assertEquals(200, response.statusCode(), response.body());
        return JSON.readTree(response.body());
    }

    @Configuration @Profile("coverstory-instructor-runtime")
    @Import({InstructorSourceLineageController.class, InstructorEvidenceRerankingController.class,
            InstructorLifecycleEvidenceController.class, InstructorPolicyHelpController.class})
    static class RuntimeConfiguration {
        @Bean SourceLineageReadPort lineage() { return new CAP121RuntimePortAdapter(); }
        @Bean EvidenceRerankingPort reranking() { return new CAP122RuntimePortAdapter(); }
        @Bean LifecycleEvidenceReadPort lifecycle() { return new CAP177RuntimePortAdapter(); }
        @Bean PolicyHelpReadPort policyHelp() { return new CAP123RuntimePortAdapter(); }
        @Bean FilterRegistrationBean<Filter> trustedSyntheticOwnerForRuntimeTests() {
            FilterRegistrationBean<Filter> reg = new FilterRegistrationBean<>();
            reg.setFilter((req, res, chain) -> {
                String id = ((HttpServletRequest) req).getHeader(HEADER);
                if (id == null) { chain.doFilter(req, res); return; }
                UUID owner;
                try { owner = UUID.fromString(id); } catch (IllegalArgumentException invalid) { chain.doFilter(req, res); return; }
                if (!List.of(OWNER_A, OWNER_B, UUID.fromString("12300000-0000-4000-8000-000000000001"),
                        UUID.fromString("12300000-0000-4000-8000-000000000002"), CAP177RuntimePortAdapter.OWNER_A).contains(owner)) {
                    chain.doFilter(req, res); return;
                }
                UUID authenticated = owner;
                chain.doFilter(new HttpServletRequestWrapper((HttpServletRequest) req) {
                    @Override public Principal getUserPrincipal() { return () -> authenticated.toString(); }
                }, res);
            });
            reg.setUrlPatterns(List.of("/api/*"));
            return reg;
        }
    }
}
