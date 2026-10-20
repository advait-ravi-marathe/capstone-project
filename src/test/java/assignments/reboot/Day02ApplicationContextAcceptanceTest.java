package assignments.reboot;

import assignments.insurance.InsuranceDayOneApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Full Boot application check with the Day 3 global advice and real MVC wiring active. */
class Day02ApplicationContextAcceptanceTest {
    @Test
    void malformedAndMissingBodiesRemain400WithGlobalAdviceInstalled() throws Exception {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(InsuranceDayOneApplication.class)
                .web(WebApplicationType.SERVLET)
                .properties("server.port=0", "spring.main.banner-mode=off", "logging.level.root=ERROR")
                .run()) {
            int port = ((WebServerApplicationContext) context).getWebServer().getPort();
            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> missing = client.send(HttpRequest.newBuilder()
                            .uri(URI.create("http://127.0.0.1:" + port + "/api/quote-selections"))
                            .POST(HttpRequest.BodyPublishers.noBody()).build(),
                    HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> malformed = client.send(HttpRequest.newBuilder()
                            .uri(URI.create("http://127.0.0.1:" + port + "/api/quote-selections"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString("{broken")).build(),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(400, missing.statusCode());
            assertEquals(400, malformed.statusCode());
            assertTrue(missing.body().contains("INVALID_REQUEST"));
            assertTrue(malformed.body().contains("INVALID_REQUEST"));
            assertTrue(missing.body().contains("Request validation failed"));
            assertTrue(malformed.body().contains("Request validation failed"));
        }
    }
}
