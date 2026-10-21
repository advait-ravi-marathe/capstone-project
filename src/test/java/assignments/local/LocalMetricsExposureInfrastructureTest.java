package assignments.local;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifies the local actuator metrics view is authenticated and limited to the demo allow-list. */
@SpringBootTest(classes = LocalMetricsExposureInfrastructureTest.TestApplication.class, properties = {
        "management.endpoints.web.exposure.include=health,metrics",
        "management.endpoints.access.default=none",
        "management.endpoint.health.access=read-only",
        "management.endpoint.metrics.access=read-only"
})
@AutoConfigureMockMvc
@ActiveProfiles("local")
class LocalMetricsExposureInfrastructureTest {
    private static final Set<String> ALLOWED_NAMES = Set.of(
            "coverstory.preview.requests",
            "coverstory.preview.latency",
            "hikaricp.connections.active");

    @jakarta.annotation.Resource
    MockMvc mvc;

    @jakarta.annotation.Resource
    MeterRegistry registry;

    @Test
    void metricsRequireAuthenticationAreReadOnlyAndExposeOnlyBoundedDemoSeries() throws Exception {
        var requests = registry.counter("coverstory.preview.requests", "outcome", "BUSY");
        requests.increment();
        registry.timer("coverstory.preview.latency", "outcome", "BUSY").record(Duration.ofMillis(4));
        registry.gauge("hikaricp.connections.active", new AtomicInteger(2), AtomicInteger::get);
        registry.counter("jvm.memory.used").increment();
        registry.counter("coverstory.preview.requests", "policyId", "raw-policy-id").increment();
        Gauge.builder("hikaricp.connections.active", new AtomicInteger(9), AtomicInteger::get)
                .tag("pool", "raw-pool-name").register(registry);

        mvc.perform(get("/actuator/metrics")).andExpect(status().isUnauthorized());

        String body = mvc.perform(get("/actuator/metrics").with(user("owner-a")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var namesNode = new ObjectMapper().readTree(body).path("names");
        Set<String> exposedNames = new HashSet<>();
        namesNode.forEach(name -> exposedNames.add(name.asText()));
        assertEquals(ALLOWED_NAMES, exposedNames);
        assertTrue(registry.getMeters().stream().flatMap(meter -> meter.getId().getTags().stream())
                .noneMatch(tag -> tag.getKey().equals("policyId") || tag.getValue().equals("raw-pool-name")));

        mvc.perform(get("/actuator/env").with(user("owner-a"))).andExpect(status().isNotFound());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/actuator/metrics/coverstory.preview.requests").with(user("owner-a")).with(csrf()))
                .andExpect(status().isMethodNotAllowed());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({LocalSecurityConfiguration.class, LocalMetricsConfiguration.class})
    static class TestApplication {
        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }
}
