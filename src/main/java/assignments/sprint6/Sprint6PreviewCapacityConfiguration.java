package assignments.sprint6;

import assignments.renewal.PreviewAdmission;
import assignments.renewal.PreviewCapacityPolicy;
import assignments.renewal.PreviewMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Capacity settings used by the bounded Days 71–84 runtime. */
@Configuration(proxyBeanMethods = false)
@Profile("sprint6-local")
public class Sprint6PreviewCapacityConfiguration {
    @Bean
    PreviewCapacityPolicy sprint6PreviewCapacityPolicy(
            @Value("${coverstory.preview.permit-capacity:2}") int permitCapacity,
            @Value("${spring.datasource.hikari.maximum-pool-size:4}") int poolMaximumSize,
            @Value("${spring.datasource.hikari.connection-timeout:1000}") int connectionTimeoutMillis) {
        PreviewCapacityPolicy policy = new PreviewCapacityPolicy(permitCapacity, poolMaximumSize, connectionTimeoutMillis);
        policy.validate();
        return policy;
    }

    @Bean
    PreviewAdmission sprint6PreviewAdmission(PreviewCapacityPolicy policy) {
        return new PreviewAdmission(policy);
    }

    @Bean
    PreviewMetrics sprint6PreviewMetrics(MeterRegistry registry) {
        return new PreviewMetrics(registry);
    }
}
