package assignments.reboot;

import assignments.insurance.DatabaseReadinessProbe;
import assignments.insurance.InsuranceDayOneApplication;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Live Actuator endpoint test backed by real disposable PostgreSQL, not a canned health result. */
class Day23ActuatorInfrastructureTest {
    private static volatile DataSource healthDataSource;

    @Test
    void exposesMinimalHealthPathsAndReflectsRealDatabaseAvailability() throws Exception {
        EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
        healthDataSource = postgres.getPostgresDatabase();
        boolean postgresStopped = false;
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
                InsuranceDayOneApplication.class, TestHealthConfiguration.class)
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
            HttpResponse<String> health = get(client, port, "/actuator/health");
            HttpResponse<String> readiness = get(client, port, "/actuator/health/readiness");
            HttpResponse<String> env = get(client, port, "/actuator/env");

            assertEquals(200, health.statusCode());
            assertEquals(200, readiness.statusCode());
            assertEquals("UP", mapper.readTree(health.body()).get("status").asString());
            assertEquals("UP", mapper.readTree(readiness.body()).get("status").asString());
            assertEquals(2, mapper.readTree(health.body()).size());
            assertEquals(1, mapper.readTree(readiness.body()).size());
            assertEquals(true, mapper.readTree(health.body()).has("groups"));
            assertEquals(false, mapper.readTree(readiness.body()).has("groups"));
            assertEquals(404, env.statusCode());
            assertFalse(health.body().contains("databaseReadiness"));

            postgres.close();
            postgresStopped = true;
            HttpResponse<String> unavailable = get(client, port, "/actuator/health/readiness");
            assertEquals(503, unavailable.statusCode());
            assertEquals("DOWN", mapper.readTree(unavailable.body()).get("status").asString());
            assertEquals(1, mapper.readTree(unavailable.body()).size());
        } finally {
            if (!postgresStopped) postgres.close();
            healthDataSource = null;
        }
    }

    private static HttpResponse<String> get(HttpClient client, int port, String path) throws Exception {
        return client.send(HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:" + port + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    @Configuration(proxyBeanMethods = false)
    static class TestHealthConfiguration {
        @Bean DatabaseReadinessProbe databaseReadinessProbe() {
            return () -> {
                try (var connection = healthDataSource.getConnection()) {
                    return connection.isValid(1);
                } catch (SQLException exception) {
                    return false;
                }
            };
        }
        @Bean HealthIndicator databaseReadiness(DatabaseReadinessProbe probe) {
            return () -> probe.isAvailable() ? Health.up().build() : Health.down().build();
        }
    }
}
