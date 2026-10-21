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
import org.springframework.context.annotation.Primary;
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
    static volatile DataSource healthDataSource;

    @Test
    void exposesMinimalHealthPathsAndReflectsRealDatabaseAvailability() throws Exception {
        EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
        healthDataSource = postgres.getPostgresDatabase();
        boolean postgresStopped = false;
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
                InsuranceDayOneApplication.class, TestDatabaseHealthConfiguration.class, ProjectionWitnessConfiguration.class)
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
            assertEquals(mapper.readTree("{\"status\":\"UP\"}"), mapper.readTree(health.body()));
            assertEquals(mapper.readTree("{\"status\":\"UP\"}"), mapper.readTree(readiness.body()));
            assertEquals(404, env.statusCode());
            assertEquals(404, get(client, port, "/actuator/configprops").statusCode());
            assertFalse(health.body().contains("databaseReadiness"));

            postgres.close();
            postgresStopped = true;
            HttpResponse<String> unavailable = get(client, port, "/actuator/health/readiness");
            assertEquals(503, unavailable.statusCode());
            assertEquals("DOWN", mapper.readTree(unavailable.body()).get("status").asString());
            assertEquals(mapper.readTree("{\"status\":\"DOWN\"}"), mapper.readTree(unavailable.body()));
            HttpResponse<String> overallUnavailable = get(client, port, "/actuator/health");
            assertEquals(503, overallUnavailable.statusCode());
            assertEquals(mapper.readTree("{\"status\":\"DOWN\"}"), mapper.readTree(overallUnavailable.body()));
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
    static class TestDatabaseHealthConfiguration {
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

    @Configuration(proxyBeanMethods = false)
    static class ProjectionWitnessConfiguration {
        @Bean @Primary
        assignments.insurance.HealthResponseProjection day23ProjectionWitness() {
            return new assignments.insurance.HealthResponseProjection() {
                @Override
                public Options publicOptions() {
                    return new Options(false, java.util.Set.of());
                }
            };
        }
    }
}
