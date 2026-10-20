package assignments.insurance;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Adds the Day 23 readiness indicator only when its probe is explicitly configured. */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "coverstory.health.database-enabled", havingValue = "true")
public class ReadinessActuatorConfiguration {
    @Bean
    DatabaseReadinessIndicator databaseReadiness(DatabaseReadinessProbe probe) {
        return new DatabaseReadinessIndicator(probe);
    }
}
