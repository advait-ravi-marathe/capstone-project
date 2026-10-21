package assignments.insurance;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.health.actuate.endpoint.HealthEndpointGroups;
import org.springframework.boot.health.actuate.endpoint.HealthEndpointWebExtension;
import org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointProperties;
import org.springframework.boot.health.registry.HealthContributorRegistry;
import org.springframework.boot.health.registry.ReactiveHealthContributorRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Supplies the narrow status-only extension when the Actuator health endpoint is present. */
@Configuration(proxyBeanMethods = false)
public class HealthResponseProjectionActuatorConfiguration {
    @Bean
    @ConditionalOnMissingBean(HealthResponseProjection.class)
    HealthResponseProjection healthResponseProjection() {
        return new HealthResponseProjection();
    }

    @Bean
    @ConditionalOnMissingBean(HealthEndpointWebExtension.class)
    HealthEndpointWebExtension day23HealthEndpointWebExtension(HealthContributorRegistry contributors,
            ObjectProvider<ReactiveHealthContributorRegistry> reactiveContributors,
            HealthEndpointGroups groups, HealthEndpointProperties properties,
            HealthResponseProjection projection) {
        return new Day23HealthEndpointWebExtension(contributors, reactiveContributors.getIfAvailable(), groups,
                properties.getLogging().getSlowIndicatorThreshold(), projection);
    }
}
