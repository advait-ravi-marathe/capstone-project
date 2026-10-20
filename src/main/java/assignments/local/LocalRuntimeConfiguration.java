package assignments.local;

import assignments.insurance.PolicyLifecycleService;
import assignments.insurance.PolicyRenewalPreviewPolicy;
import assignments.insurance.PolicyRenewalReadStore;
import assignments.insurance.PolicyReadStore;
import assignments.renewal.PolicyRenewalPreviewController;
import assignments.renewal.PreviewAdmission;
import assignments.renewal.PreviewCapacityPolicy;
import assignments.renewal.PreviewCapacityExceptionHandler;
import assignments.renewal.PreviewMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/** Explicitly assembles the local app; it does not scan the Day 1 or test harness packages. */
@Configuration(proxyBeanMethods = false)
@Profile("local")
@Import({PolicyRenewalPreviewController.class, PreviewCapacityExceptionHandler.class})
public class LocalRuntimeConfiguration {
    @Bean
    Clock localClock(@Value("${coverstory.local.fixed-clock:}") String fixedClock) {
        return fixedClock.isBlank() ? Clock.systemUTC()
                : Clock.fixed(Instant.parse(fixedClock), ZoneOffset.UTC);
    }

    @Bean
    LocalApplicationComposition.Components localComponents(Clock localClock, DataSource dataSource) {
        return LocalApplicationComposition.compose(localClock, dataSource);
    }

    @Bean
    PolicyLifecycleService policyLifecycleService(LocalApplicationComposition.Components components) {
        return components.lifecycleService();
    }

    @Bean
    PolicyRenewalPreviewPolicy policyRenewalPreviewPolicy(LocalApplicationComposition.Components components) {
        return components.renewalPreviewPolicy();
    }

    @Bean
    PolicyReadStore policyReadStore(LocalApplicationComposition.Components components) {
        return components.policyReadStore();
    }

    @Bean
    PreviewCapacityPolicy previewCapacityPolicy(
            @Value("${coverstory.preview.permit-capacity:2}") int permitCapacity,
            @Value("${spring.datasource.hikari.maximum-pool-size:4}") int poolMaximumSize,
            @Value("${spring.datasource.hikari.connection-timeout:1000}") int connectionTimeoutMillis) {
        PreviewCapacityPolicy policy = new PreviewCapacityPolicy(permitCapacity, poolMaximumSize, connectionTimeoutMillis);
        policy.validate();
        return policy;
    }

    @Bean
    PreviewAdmission previewAdmission(PreviewCapacityPolicy policy) {
        return new PreviewAdmission(policy);
    }

    @Bean
    PreviewMetrics previewMetrics(MeterRegistry registry) {
        return new PreviewMetrics(registry);
    }

    @Bean
    PolicyRenewalReadStore policyRenewalReadStore(LocalApplicationComposition.Components components,
                                                   PreviewAdmission admission, PreviewMetrics metrics) {
        // LEARNER TODO Day 79: wrap this exact existing owner-scoped store once in the
        // bounded singleton decorator; do not duplicate or replace its SQL adapter.
        throw new UnsupportedOperationException("Day 79 learner TODO: wire one bounded store around the existing adapter");
    }

    @Bean
    LocalPolicyOwnerResolver localPolicyOwnerResolver() { return new LocalPolicyOwnerResolver(); }

    @Bean
    PolicyIssueReceiptReadStore policyIssueReceiptReadStore(DataSource dataSource) {
        return new PolicyIssueReceiptReadStore(dataSource);
    }
}
