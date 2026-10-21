package assignments.learning;

import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Toy projection separates dependency state from a public response allowlist. */
class Day23ReadinessProjectionReferenceTest {
    static final class ToyIndicator implements HealthIndicator {
        private final boolean available;
        ToyIndicator(boolean available) { this.available = available; }
        @Override public Health health() { return available ? Health.up().build() : Health.down().build(); }
    }
    static byte[] publicBody(Health health) throws Exception {
        return new ObjectMapper().writeValueAsBytes(java.util.Map.of("status", health.getStatus().getCode()));
    }
    @Test void indicatorTruthAndStatusOnlyProjectionAreSeparate() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        var up = mapper.readTree(publicBody(new ToyIndicator(true).health()));
        var down = mapper.readTree(publicBody(new ToyIndicator(false).health()));
        assertEquals("UP", up.get("status").asString());
        assertEquals(1, up.size());
        assertEquals("DOWN", down.get("status").asString());
        assertEquals(1, down.size());
    }
}
