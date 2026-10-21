package assignments.learning.day01;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("learnerAcceptance")
class Day01ApiPracticeAcceptanceTest {
    @Test
    void returnsTheRequestedMessageAsJson() throws Exception {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(Day01ApiBridgeApplication.class)
                .web(WebApplicationType.SERVLET)
                .properties("server.address=127.0.0.1", "server.port=0", "spring.main.banner-mode=off",
                        "management.endpoints.enabled-by-default=false", "logging.level.root=ERROR")
                .run()) {
            int port = ((WebServerApplicationContext) context).getWebServer().getPort();
            HttpResponse<String> response = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/learn/day1/practice")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertTrue(response.headers().firstValue("content-type").orElse("").contains("application/json"));
            assertEquals("API practice ready", new ObjectMapper().readTree(response.body()).get("message").asString());
        }
    }
}
