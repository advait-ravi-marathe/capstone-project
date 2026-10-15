package coverstory;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import coverstory.modules.catalog.CatalogImportIdentityPort;
import coverstory.modules.catalog.CatalogImportIdentityService;
import coverstory.modules.catalog.ImportIdentityCommand;
import coverstory.modules.catalog.api.CAP43Seam;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/** Starts the actual application context and crosses the HTTP/module boundary without a database. */
class ApplicationRuntimeIntegrationTest {
    @Test
    void publicCatalogRunsThroughApplicationWiringAndSessionStillFailsClosed() throws Exception {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(CoverStoryApplication.class)
                .web(WebApplicationType.SERVLET)
                .properties("server.port=0", "spring.main.banner-mode=off", "logging.level.root=ERROR")
                .run()) {
            CatalogImportIdentityService importService = context.getBean(CatalogImportIdentityService.class);
            assertEquals(1, context.getBeansOfType(CatalogImportIdentityPort.class).size());
            assertInstanceOf(CAP43Seam.class, context.getBean(CatalogImportIdentityPort.class),
                    "the Catalog public port resolves to the preserved CAP-43 learner seam");
            UnsupportedOperationException todo = assertThrows(UnsupportedOperationException.class,
                    () -> importService.classify(new java.util.LinkedHashMap<>(),
                            new ImportIdentityCommand("editor-E", "health-A", "v1", "synthetic-hash")));
            assertTrue(todo.getMessage().contains("CAP-43 learner TODO"),
                    "the running module service reaches the unchanged learner boundary");
            int port = ((WebServerApplicationContext) context).getWebServer().getPort();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> catalog = client.send(HttpRequest.newBuilder(URI.create(
                            "http://127.0.0.1:" + port + "/api/catalog?category=health"))
                    .GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, catalog.statusCode());
            JsonNode payload = new ObjectMapper().readTree(catalog.body());
            assertEquals(1, payload.path("schemaMajor").asInt());
            assertEquals(3, payload.path("offers").size());
            assertEquals("SYNTHETIC", payload.path("offers").get(0).path("evidenceStatus").asText());
            assertFalse(payload.path("offers").get(0).has("price"));

            HttpResponse<String> selectionOrder = client.send(HttpRequest.newBuilder(URI.create(
                            "http://127.0.0.1:" + port + "/api/shortlist/selection-order"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"selections\":[{\"deviceScope\":\"tab-A\",\"catalogVersionId\":\"10000000-0000-4000-8000-000000000001@v1\"}]}"))
                    .build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(501, selectionOrder.statusCode(),
                    "the running app must expose the preserved CAP-40 learner TODO safely");

            HttpResponse<String> session = client.send(HttpRequest.newBuilder(URI.create(
                            "http://127.0.0.1:" + port + "/api/session"))
                    .GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(401, session.statusCode());

            HttpResponse<String> catalogImport = client.send(HttpRequest.newBuilder(URI.create(
                            "http://127.0.0.1:" + port + "/api/v1/catalog/imports"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "{\"sourceId\":\"synthetic-source\",\"sourceVersion\":\"v1\","
                                    + "\"canonicalHash\":\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"}"))
                    .build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(401, catalogImport.statusCode(),
                    "the real CAP-52 route is present and fails closed without the instructor trusted-principal adapter");
        }
    }
}
