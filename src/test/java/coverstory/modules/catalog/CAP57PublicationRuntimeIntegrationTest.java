package coverstory.modules.catalog;

import coverstory.CoverStoryApplication;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
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
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

/** Actual Spring publication route -> CAP-57 instructor reference transaction -> PostgreSQL -> public Catalog read route. */
class CAP57PublicationRuntimeIntegrationTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final UUID EDITOR = UUID.fromString("57000000-0000-4000-8000-000000000001");
    private static final UUID PRODUCT = UUID.fromString("5f1b8b02-1b68-4de8-928c-f3eaa5f80001");
    private static final UUID VERSION_A1 = UUID.fromString("a8100000-0000-4000-8000-000000000001");
    private static final UUID VERSION_A2 = UUID.fromString("a8100000-0000-4000-8000-000000000002");
    private static final String HASH_A1 = "cb4a74791080a86290ead23a0e21db92cb5a0e8a77fd612830683158b5bd8160";
    private static final String HASH_A2 = "e51e4dced2050c681003db6715ed4a7365c286ed1dd3c1ce5fae673bfdcfe242";
    private static final String TEST_EDITOR_HEADER = "X-CAP57-Instructor-Session";

    @Test void publicationAuditAndExportUseOneRealHttpPostgresTransaction() throws Exception {
        try (CAP57TestDatabase database = CAP57TestDatabase.fresh()) {
            String jdbcUrl, username;
            try (Connection c = database.postgres().getPostgresDatabase().getConnection()) {
                jdbcUrl = c.getMetaData().getURL(); username = c.getMetaData().getUserName();
            }

            try (ConfigurableApplicationContext app = start(jdbcUrl, username)) {
                HttpResponse<String> anonymous = publish(app, VERSION_A1, HASH_A1, false, false);
                assertEquals(401, anonymous.statusCode(), "publication requires trusted synthetic editor: " + anonymous.body());
                assertEquals(201, publish(app, VERSION_A1, HASH_A1, true, false).statusCode());
                assertPublicVersion(app, VERSION_A1, "PUBLISHED");
                assertExport(app, VERSION_A1, HASH_A1, 123456L);
                assertAudit(database, 1, VERSION_A1, "source-A/v1");

                HttpResponse<String> duplicate = publish(app, VERSION_A1, HASH_A1, true, false);
                assertEquals(200, duplicate.statusCode());
                assertEquals("DUPLICATE", JSON.readTree(duplicate.body()).path("code").asText());
                assertAudit(database, 1, VERSION_A1, "source-A/v1");

                String changedTermsHash = "00".repeat(32);
                HttpResponse<String> conflict = publish(app, VERSION_A1, changedTermsHash, true, false);
                assertEquals(409, conflict.statusCode());
                assertEquals("VERSION_CONTENT_CONFLICT", JSON.readTree(conflict.body()).path("code").asText());
                assertPublicVersion(app, VERSION_A1, "PUBLISHED");
                assertAudit(database, 1, VERSION_A1, "source-A/v1");

                HttpResponse<String> rolledBack = publish(app, VERSION_A2, HASH_A2, true, true);
                assertEquals(503, rolledBack.statusCode());
                assertEquals("PUBLICATION_UNAVAILABLE", JSON.readTree(rolledBack.body()).path("code").asText());
                assertPublicVersion(app, VERSION_A1, "PUBLISHED");
                assertExport(app, VERSION_A1, HASH_A1, 123456L);
                assertAudit(database, 1, VERSION_A1, "source-A/v1");

                assertEquals(201, publish(app, VERSION_A2, HASH_A2, true, false).statusCode());
                assertPublicVersion(app, VERSION_A2, "PUBLISHED");
                assertPublicVersion(app, VERSION_A1, "RETIRED");
                assertExport(app, VERSION_A2, HASH_A2, 133456L);
                assertAudit(database, 2, VERSION_A2, "source-A/v2");
            }

            try (ConfigurableApplicationContext restarted = start(jdbcUrl, username)) {
                assertPublicVersion(restarted, VERSION_A2, "PUBLISHED");
                assertExport(restarted, VERSION_A2, HASH_A2, 133456L);
                assertAudit(database, 2, VERSION_A2, "source-A/v2");
            }
        }
    }

    @Test void publicCatalogVersionReadStillWorksWithoutTheTestEditorSession() throws Exception {
        try (CAP57TestDatabase database = CAP57TestDatabase.fresh()) {
            String jdbcUrl, username;
            try (Connection c = database.postgres().getPostgresDatabase().getConnection()) {
                jdbcUrl = c.getMetaData().getURL(); username = c.getMetaData().getUserName();
            }
            try (ConfigurableApplicationContext app = start(jdbcUrl, username)) {
                HttpResponse<String> response = get(app, "/api/catalog/versions");
                assertEquals(200, response.statusCode());
                JsonNode versions = JSON.readTree(response.body()).path("versions");
                assertEquals(3, versions.size());
                assertTrue(response.body().contains("RETIRED"));
                assertFalse(response.body().contains("restrictions"));
                assertFalse(response.body().contains("amountMinor"));
            }
        }
    }

    private static ConfigurableApplicationContext start(String url, String username) {
        return new SpringApplicationBuilder(CoverStoryApplication.class, RuntimeConfiguration.class)
                .web(WebApplicationType.SERVLET)
                .properties("server.port=0", "spring.main.banner-mode=off", "logging.level.root=ERROR",
                        "spring.profiles.active=cap57-runtime-test", "coverstory.catalog.jdbc.url=" + url,
                        "coverstory.catalog.jdbc.username=" + username, "coverstory.catalog.jdbc.password=")
                .run();
    }

    private static HttpResponse<String> publish(ConfigurableApplicationContext app, UUID version, String hash,
                                                boolean withEditor, boolean failBeforeCommit) throws Exception {
        int port = ((WebServerApplicationContext) app).getWebServer().getPort();
        String body = JSON.writeValueAsString(new PublishBody(version, hash));
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                        + "/api/catalog/__instructor/cap57/health/" + PRODUCT + "/publish?failBeforeCommit=" + failBeforeCommit))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body));
        if (withEditor) request.header(TEST_EDITOR_HEADER, "synthetic-cap57-editor");
        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> get(ConfigurableApplicationContext app, String path) throws Exception {
        int port = ((WebServerApplicationContext) app).getWebServer().getPort();
        return HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Accept", "application/json").GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private static void assertPublicVersion(ConfigurableApplicationContext app, UUID versionId, String status) throws Exception {
        HttpResponse<String> response = get(app, "/api/catalog/versions");
        assertEquals(200, response.statusCode(), response.body());
        JsonNode versions = JSON.readTree(response.body()).path("versions");
        JsonNode match = null;
        for (JsonNode version : versions) if (version.path("versionId").asText().equals(versionId.toString())) match = version;
        assertNotNull(match, "published version remains visible in public version-history route");
        assertEquals(status, match.path("status").asText());
        assertFalse(match.has("restrictions"));
    }

    private static void assertExport(ConfigurableApplicationContext app, UUID versionId, String hash, long amount) throws Exception {
        HttpResponse<String> response = get(app, "/api/catalog/__instructor/cap57/health/export");
        assertEquals(200, response.statusCode(), response.body());
        JsonNode rows = JSON.readTree(response.body()).path("rows");
        assertEquals(1, rows.size());
        JsonNode row = rows.get(0);
        assertEquals(PRODUCT.toString(), row.path("productId").asText());
        assertEquals(versionId.toString(), row.path("versionId").asText());
        assertEquals(hash, row.path("contentSha256").asText());
        assertEquals(amount, row.path("amountMinor").asLong());
        assertEquals("INR", row.path("currency").asText());
    }

    private static void assertAudit(CAP57TestDatabase database, int expectedCount, UUID latestVersion, String sourceVersion) throws Exception {
        try (Connection c = database.postgres().getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            try (ResultSet count = s.executeQuery("SELECT count(*) FROM catalog.publication_audit_event")) {
                assertTrue(count.next()); assertEquals(expectedCount, count.getInt(1));
            }
            try (ResultSet latest = s.executeQuery("""
                    SELECT version_id::text, source_version, encode(content_sha256, 'hex'), event_code, editor_id::text
                      FROM catalog.publication_audit_event ORDER BY occurred_at DESC, audit_id DESC LIMIT 1
                    """)) {
                assertTrue(latest.next());
                assertEquals(latestVersion.toString(), latest.getString(1));
                assertEquals(sourceVersion, latest.getString(2));
                assertEquals(latestVersion.equals(VERSION_A1) ? HASH_A1 : HASH_A2, latest.getString(3));
                assertEquals("CATALOG_VERSION_PUBLISHED", latest.getString(4));
                assertEquals(EDITOR.toString(), latest.getString(5));
            }
            List<String> columns = new java.util.ArrayList<>();
            try (ResultSet columnsRs = s.executeQuery("SELECT column_name FROM information_schema.columns WHERE table_schema='catalog' AND table_name='publication_audit_event' ORDER BY ordinal_position")) {
                while (columnsRs.next()) columns.add(columnsRs.getString(1));
            }
            assertEquals(List.of("audit_id", "editor_id", "event_code", "product_id", "version_id", "source_version", "content_sha256", "occurred_at"), columns);
            assertFalse(columns.stream().anyMatch(name -> List.of("payload", "restrictions", "consumer_id", "journey_id").contains(name)));
        }
    }

    record PublishBody(UUID versionId, String contentSha256) { }

    @Configuration @Profile("cap57-runtime-test")
    static class RuntimeConfiguration {
        @Bean CAP57ReferencePublicationAdapter cap57ReferencePublicationAdapter(Environment environment) {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setUrl(environment.getRequiredProperty("coverstory.catalog.jdbc.url"));
            dataSource.setUsername(environment.getRequiredProperty("coverstory.catalog.jdbc.username"));
            dataSource.setPassword(environment.getProperty("coverstory.catalog.jdbc.password", ""));
            return new CAP57ReferencePublicationAdapter(dataSource);
        }

        @Bean FilterRegistrationBean<Filter> trustedSyntheticEditorOnlyForThisTest() {
            FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
            registration.setFilter((request, response, chain) -> {
                HttpServletRequest http = (HttpServletRequest) request;
                if ("synthetic-cap57-editor".equals(http.getHeader(TEST_EDITOR_HEADER))) {
                    http = new HttpServletRequestWrapper(http) {
                        @Override public Principal getUserPrincipal() { return () -> EDITOR.toString(); }
                    };
                }
                chain.doFilter(http, response);
            });
            registration.setUrlPatterns(List.of("/api/catalog/__instructor/cap57/*"));
            return registration;
        }
    }

}
