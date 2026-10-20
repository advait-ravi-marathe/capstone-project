package assignments.local;

import assignments.insurance.PolicyLifecycleService;
import assignments.insurance.PolicyRenewalPreviewPolicy;
import assignments.insurance.PolicyRenewalReadStore;
import assignments.insurance.PolicyReadStore;
import assignments.renewal.PolicyRenewalPreviewController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.time.Clock;

/** Explicitly assembles the local app; it does not scan the Day 1 or test harness packages. */
@Configuration(proxyBeanMethods = false)
@Profile("local")
@Import(PolicyRenewalPreviewController.class)
public class LocalRuntimeConfiguration {
    @Bean
    Clock localClock() { return Clock.systemUTC(); }

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
    PolicyRenewalReadStore policyRenewalReadStore(LocalApplicationComposition.Components components) {
        return components.renewalReadStore();
    }

    @Bean
    LocalPolicyOwnerResolver localPolicyOwnerResolver() { return new LocalPolicyOwnerResolver(); }

    @Bean
    PolicyIssueReceiptReadStore policyIssueReceiptReadStore(DataSource dataSource) {
        return new PolicyIssueReceiptReadStore(dataSource);
    }
}
