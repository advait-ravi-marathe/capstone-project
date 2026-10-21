package assignments.reboot;

import assignments.insurance.InsuranceDayOneApplication;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Learner acceptance for strict real-HTTP status projection; the projection TODO is intentional. */
@Tag("learnerAcceptance")
class Day23HealthProjectionContractTest {
    @Test
    void healthAndReadinessExposeOnlyRealDatabaseStatus() throws Exception {
        EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
        Day23ActuatorInfrastructureTest.healthDataSource = postgres.getPostgresDatabase();
        boolean stopped = false;
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
                InsuranceDayOneApplication.class, Day23ActuatorInfrastructureTest.TestDatabaseHealthConfiguration.class)
                .web(WebApplicationType.SERVLET)
                .properties("server.port=0", "spring.main.banner-mode=off", "logging.level.root=ERROR",
                        "management.endpoints.web.exposure.include=health",
                        "management.endpoint.health.show-details=never",
                        "management.endpoint.health.probes.enabled=false",
                        "management.endpoint.health.group.readiness.include=databaseReadiness")
                .run()) {
            int port = ((WebServerApplicationContext) context).getWebServer().getPort();
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();
            assertHealth(client, port, "/actuator/health", 200, "UP", mapper);
            assertHealth(client, port, "/actuator/health/readiness", 200, "UP", mapper);
            assertEquals(404, get(client, port, "/actuator/env").statusCode());
            assertEquals(404, get(client, port, "/actuator/configprops").statusCode());

            postgres.close();
            stopped = true;
            assertHealth(client, port, "/actuator/health", 503, "DOWN", mapper);
            assertHealth(client, port, "/actuator/health/readiness", 503, "DOWN", mapper);
        } finally {
            if (!stopped) postgres.close();
            Day23ActuatorInfrastructureTest.healthDataSource = null;
        }
    }

    private static void assertHealth(HttpClient client, int port, String path, int expectedStatus,
            String expectedHealth, ObjectMapper mapper) throws Exception {
        HttpResponse<String> response = get(client, port, path);
        assertEquals(expectedStatus, response.statusCode());
        assertEquals(mapper.readTree("{\"status\":\"" + expectedHealth + "\"}"),
                mapper.readTree(response.body()));
    }

    private static HttpResponse<String> get(HttpClient client, int port, String path) throws Exception {
        return client.send(HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + port + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
