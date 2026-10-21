package assignments.insurance;

import org.springframework.boot.actuate.endpoint.ApiVersion;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpointGroups;
import org.springframework.boot.health.actuate.endpoint.HealthEndpointWebExtension;
import org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointProperties;
import org.springframework.boot.health.registry.HealthContributorRegistry;
import org.springframework.boot.health.registry.ReactiveHealthContributorRegistry;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

/** Preserves Actuator's evaluated status and HTTP code while delegating body allowlisting. */
public final class Day23HealthEndpointWebExtension extends HealthEndpointWebExtension {
    private final HealthResponseProjection projection;

    public Day23HealthEndpointWebExtension(HealthContributorRegistry contributors,
            ReactiveHealthContributorRegistry reactiveContributors, HealthEndpointGroups groups,
            Duration slowIndicatorThreshold, HealthResponseProjection projection) {
        super(contributors, reactiveContributors, groups, slowIndicatorThreshold);
        this.projection = projection;
    }

    @Override
    protected HealthDescriptor aggregateDescriptors(ApiVersion version, Map<String, HealthDescriptor> contributors,
            org.springframework.boot.health.actuate.endpoint.StatusAggregator statusAggregator,
            boolean showComponents, Set<String> groups) {
        HealthResponseProjection.Options options = projection.publicOptions();
        return super.aggregateDescriptors(version, contributors, statusAggregator,
                options.showComponents(), options.groups());
    }
}
