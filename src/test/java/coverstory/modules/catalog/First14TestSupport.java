package coverstory.modules.catalog;

import coverstory.CoverStoryApplication;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/** Shared disposable runtime and SQL helpers for the two instructor-only CS90 integration witnesses. */
final class First14TestSupport {
    static final ObjectMapper JSON = new ObjectMapper();
    static final UUID EDITOR = UUID.fromString("57000000-0000-4000-8000-000000000001");
    static final String PRODUCT_A = "5f1b8b02-1b68-4de8-928c-f3eaa5f80001";
    static final String PRODUCT_B = "5f1b8b02-1b68-4de8-928c-f3eaa5f80002";
    static final String PRODUCT_C = "5f1b8b02-1b68-4de8-928c-f3eaa5f80003";
    static final String A1 = "a8100000-0000-4000-8000-000000000001";
    static final String A2 = "a8100000-0000-4000-8000-000000000002";
    static final String B1 = "b8100000-0000-4000-8000-000000000001";
    static final String C1 = "c8100000-0000-4000-8000-000000000001";
    static final String HASH_A1 = "cb4a74791080a86290ead23a0e21db92cb5a0e8a77fd612830683158b5bd8160";
    static final String HASH_A2 = "e51e4dced2050c681003db6715ed4a7365c286ed1dd3c1ce5fae673bfdcfe242";
    static final String HASH_B1 = "7773be7dae3f70e9dde64f20dcb23ea101e6858643ff88937a53b607d2a2cd1b";
    static final String HASH_C1 = "dd23ecc3efba36f4a9dc41dae3fd1bb5f1f9ecb1be5543eb4d244a4b400b8747";
    private static final String TEST_EDITOR_HEADER = "X-CAP57-Instructor-Session";

    record PublishedIdentity(String sourceId, String sourceVersion, String versionId, String contentSha256) { }
    record PublishResponse(int status, String code, String body) { }

    private First14TestSupport() { }

    static EmbeddedPostgres freshDatabase(String fixture) throws Exception {
        EmbeddedPostgres pg = EmbeddedPostgres.builder().start();
        try (Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            CAP166CatalogRuntimeIntegrationTest.executeResource(s, "/db/schema/ownership.sql");
            CAP166CatalogRuntimeIntegrationTest.executeResource(s, "/db/schema/catalog_publication.sql");
            CAP166CatalogRuntimeIntegrationTest.executeResource(s, "/db/schema/catalog_discovery.sql");
            CAP166CatalogRuntimeIntegrationTest.executeResource(s, "/coverstory/catalog/" + fixture);
            try (ResultSet rows = s.executeQuery("SELECT count(*) FROM catalog.health_offer_version")) {
                if (!rows.next() || rows.getInt(1) != 3) throw new AssertionError("CS90 initial DB must contain exactly A/v1, B/v1, and C/v1; candidate A/v2 is not inserted yet");
            }
            return pg;
        } catch (Exception error) {
            pg.close();
            throw error;
        }
    }

    static ConfigurableApplicationContext startApp(EmbeddedPostgres postgres) throws Exception {
        String url, username;
        try (Connection c = postgres.getPostgresDatabase().getConnection()) {
            url = c.getMetaData().getURL(); username = c.getMetaData().getUserName();
        }
        return new SpringApplicationBuilder(CoverStoryApplication.class, CAP57PublicationRuntimeIntegrationTest.RuntimeConfiguration.class)
                .web(WebApplicationType.SERVLET)
                .properties("server.port=0", "spring.main.banner-mode=off", "logging.level.root=ERROR",
                        "spring.profiles.active=cap57-runtime-test", "coverstory.catalog.jdbc.url=" + url,
                        "coverstory.catalog.jdbc.username=" + username, "coverstory.catalog.jdbc.password=")
                .run();
    }

    static int port(ConfigurableApplicationContext app) {
        return ((WebServerApplicationContext) app).getWebServer().getPort();
    }

    static HttpResponse<String> get(ConfigurableApplicationContext app, String path) throws Exception {
        return HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port(app) + path))
                .header("Accept", "application/json").GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    static PublishResponse publishPrepared(ConfigurableApplicationContext app, String product, String version,
                                           String hash, boolean failBeforeCommit) throws Exception {
        String path = "/api/catalog/__instructor/cap57/health/" + product + "/publish?failBeforeCommit=" + failBeforeCommit;
        String body = JSON.writeValueAsString(Map.of("versionId", version, "contentSha256", hash));
        return post(app, path, body);
    }

    static PublishResponse stageAndPublish(ConfigurableApplicationContext app, String product, String version,
                                           String sourceVersion, String sourceId, String hash,
                                           boolean failBeforeCommit) throws Exception {
        String path = "/api/catalog/__instructor/cap57/health/" + product + "/stage-and-publish?failBeforeCommit=" + failBeforeCommit;
        String body = JSON.writeValueAsString(Map.of("versionId", version, "sourceVersion", sourceVersion,
                "sourceId", sourceId, "contentSha256", hash));
        return post(app, path, body);
    }

    private static PublishResponse post(ConfigurableApplicationContext app, String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port(app) + path))
                .header("Content-Type", "application/json").header(TEST_EDITOR_HEADER, "synthetic-cap57-editor")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        String code = JSON.readTree(response.body()).path("code").asText();
        return new PublishResponse(response.statusCode(), code, response.body());
    }

    static Map<String, PublishedIdentity> sqlActiveSnapshot(EmbeddedPostgres postgres) throws Exception {
        Map<String, PublishedIdentity> result = new HashMap<>();
        try (Connection c = postgres.getPostgresDatabase().getConnection(); Statement s = c.createStatement();
             ResultSet rows = s.executeQuery("""
                 SELECT m.source_id, v.source_version, v.version_id::text, encode(v.content_sha256, 'hex')
                   FROM catalog.active_health_offer a
                   JOIN catalog.health_offer_version v USING (product_id, version_id)
                   JOIN catalog.health_offer_discovery_metadata m USING (product_id, version_id)
                  ORDER BY m.source_id
                 """)) {
            while (rows.next()) result.put(rows.getString(1), new PublishedIdentity(rows.getString(1), rows.getString(2), rows.getString(3), rows.getString(4)));
        }
        return Map.copyOf(result);
    }

    static JsonNode apiEnvelope(ConfigurableApplicationContext app) throws Exception {
        HttpResponse<String> response = get(app, "/api/catalog/versions?category=health");
        if (response.statusCode() != 200) throw new AssertionError("Catalog API returned " + response.statusCode() + ": " + response.body());
        return JSON.readTree(response.body());
    }

    static Map<String, PublishedIdentity> apiPublishedSnapshot(JsonNode envelope) {
        Map<String, PublishedIdentity> result = new HashMap<>();
        for (JsonNode row : envelope.path("versions")) {
            if ("PUBLISHED".equals(row.path("status").asText())) {
                String sourceId=row.path("sourceId").asText();
                result.put(sourceId,new PublishedIdentity(sourceId,row.path("sourceVersion").asText(),row.path("versionId").asText(),""));
            }
        }
        return Map.copyOf(result);
    }

    static int count(EmbeddedPostgres postgres, String sql) throws Exception {
        try (Connection c = postgres.getPostgresDatabase().getConnection(); Statement s = c.createStatement(); ResultSet row = s.executeQuery(sql)) {
            if (!row.next()) throw new AssertionError("count query returned no row");
            return row.getInt(1);
        }
    }

    static String fixture(String relativePath) throws Exception {
        return Files.readString(Path.of(relativePath));
    }

    static void executeResource(EmbeddedPostgres postgres, String resource) throws Exception {
        try (Connection c = postgres.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            CAP166CatalogRuntimeIntegrationTest.executeResource(s, resource);
        }
    }

    static void awaitLine(java.io.BufferedReader output, String marker, Duration timeout) throws Exception {
        CompletableFuture<String> next = CompletableFuture.supplyAsync(() -> {
            try { return output.readLine(); } catch (Exception e) { throw new RuntimeException(e); }
        });
        long deadline=System.nanoTime()+timeout.toNanos();
        while (System.nanoTime()<deadline) {
            String line=next.get(Math.max(1,deadline-System.nanoTime()),TimeUnit.NANOSECONDS);
            if (line==null) throw new AssertionError("Browser harness ended before marker " + marker);
            System.out.println("CS90 browser: " + line);
            if (line.startsWith("Error:") || line.startsWith("AssertionError")) throw new AssertionError("Browser failure: " + line);
            if (line.contains(marker)) return;
            next=CompletableFuture.supplyAsync(() -> { try { return output.readLine(); } catch(Exception e){throw new RuntimeException(e);} });
        }
        throw new AssertionError("Timed out waiting for browser marker " + marker);
    }
}
