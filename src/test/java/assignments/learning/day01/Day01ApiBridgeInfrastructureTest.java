package assignments.learning.day01;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import assignments.insurance.QuoteSelectionController;
import assignments.insurance.QuoteSelectionPolicy;
import assignments.insurance.QuoteSelectionRequestValidator;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Day01ApiBridgeInfrastructureTest {
    @Test
    void servesTheReferenceRoutesOverLoopbackAndDoesNotLoadProductionComponents() throws Exception {
        int port;
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(Day01ApiBridgeApplication.class)
                .web(WebApplicationType.SERVLET)
                .properties("server.address=127.0.0.1", "server.port=0", "spring.main.banner-mode=off",
                        "management.endpoints.enabled-by-default=false", "logging.level.root=ERROR")
                .run()) {
            port = ((WebServerApplicationContext) context).getWebServer().getPort();
            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> plain = send(client, "GET", port, "/learn/day1/hello");
            assertEquals(200, plain.statusCode());
            assertTrue(plain.headers().firstValue("content-type").orElse("").startsWith("text/plain"));
            assertEquals("Hello from the API", plain.body());

            HttpResponse<String> json = send(client, "GET", port, "/learn/day1/greeting");
            assertEquals(200, json.statusCode());
            assertTrue(json.headers().firstValue("content-type").orElse("").contains("application/json"));
            assertEquals("{\"message\":\"Hello from the API\"}", json.body());

            assertEquals(404, send(client, "GET", port, "/learn/day1/missing").statusCode());
            assertEquals(405, send(client, "POST", port, "/learn/day1/hello").statusCode());
            assertEquals(404, send(client, "GET", port, "/api/quote-selections").statusCode());
            assertEquals(404, send(client, "GET", port, "/actuator/health").statusCode());
            assertEquals(500, send(client, "GET", port, "/learn/day1/practice").statusCode());
            assertFalse(context.getBeansOfType(QuoteSelectionController.class).size() > 0);
            assertTrue(context.getBeansOfType(QuoteSelectionPolicy.class).isEmpty());
            assertTrue(context.getBeansOfType(QuoteSelectionRequestValidator.class).isEmpty());
            assertTrue(context.getBeansOfType(SecurityFilterChain.class).isEmpty());
            assertTrue(context.getBeansOfType(DataSource.class).isEmpty());
        }
        assertThrows(Exception.class, () -> HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/learn/day1/hello")).GET().build(),
                HttpResponse.BodyHandlers.ofString()), "Closing the teaching context must stop its listener");
    }

    @Test
    void refusesNonLoopbackBeforeOpeningAListener() {
        Exception failure = assertThrows(Exception.class, () -> new SpringApplicationBuilder(Day01ApiBridgeApplication.class)
                .web(WebApplicationType.SERVLET)
                .properties("server.address=0.0.0.0", "server.port=0", "spring.main.banner-mode=off",
                        "management.endpoints.enabled-by-default=false", "logging.level.root=ERROR")
                .run());
        assertTrue(rootMessages(failure).contains("must bind to loopback only"), rootMessages(failure));
    }

    private static HttpResponse<String> send(HttpClient client, String method, int port, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .method(method, HttpRequest.BodyPublishers.noBody()).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String rootMessages(Throwable failure) {
        StringBuilder messages = new StringBuilder();
        for (Throwable current = failure; current != null; current = current.getCause()) {
            messages.append(current.getMessage()).append('\n');
        }
        return messages.toString();
    }
}
