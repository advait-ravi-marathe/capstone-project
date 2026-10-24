package assignments.local;

import io.micrometer.core.instrument.Meter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Set;
import io.micrometer.core.instrument.config.MeterFilter;

/** Allow only the three bounded metrics needed by the loopback training recovery demo. */
@Configuration(proxyBeanMethods = false)
@Profile("local")
public class LocalMetricsConfiguration {
    private static final Set<String> OUTCOMES = Set.of("SUCCESS", "EMPTY", "BUSY", "UNAVAILABLE", "FAILURE");

    @Bean
    MeterFilter localDemoMetricAllowList() {
        return MeterFilter.denyUnless(LocalMetricsConfiguration::isAllowed);
    }

    private static boolean isAllowed(Meter.Id id) {
        if ("hikaricp.connections.active".equals(id.getName())) {
            return id.getTags().isEmpty() || (id.getTags().size() == 1
                    && "coverstory-local".equals(id.getTag("pool")));
        }
        if ("coverstory.preview.requests".equals(id.getName())
                || "coverstory.preview.latency".equals(id.getName())) {
            String outcome = id.getTag("outcome");
            return id.getTags().size() == 1 && outcome != null && OUTCOMES.contains(outcome);
        }
        return false;
    }
}
