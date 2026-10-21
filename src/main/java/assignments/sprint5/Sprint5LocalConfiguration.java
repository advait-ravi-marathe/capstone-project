package assignments.sprint5;

import assignments.insurance.PolicyLifecycleService;
import assignments.insurance.PolicyRenewalPreviewPolicy;
import assignments.insurance.PolicyRenewalReadStore;
import assignments.insurance.PolicyReadStore;
import assignments.local.LocalApplicationComposition;
import assignments.local.LocalPolicyIssueController;
import assignments.local.PolicyIssueReceiptReadStore;
import assignments.local.LocalPolicyOwnerResolver;
import assignments.local.LocalSecurityConfiguration;
import assignments.local.LocalSessionController;
import assignments.renewal.PolicyRenewalPreviewController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/** Explicit instructor-owned assembly for the scheduled 57–70 runtime slice. */
@Configuration(proxyBeanMethods = false)
@Profile("sprint5-local")
@Import({LocalSecurityConfiguration.class, LocalSessionController.class,
        LocalPolicyIssueController.class, PolicyRenewalPreviewController.class})
public class Sprint5LocalConfiguration {
    @Bean
    Clock sprint5Clock(@Value("${coverstory.local.fixed-clock:}") String fixedClock) {
        return fixedClock.isBlank() ? Clock.systemUTC()
                : Clock.fixed(Instant.parse(fixedClock), ZoneOffset.UTC);
    }

    @Bean
    LocalApplicationComposition.Components sprint5Components(Clock sprint5Clock, DataSource dataSource) {
        return LocalApplicationComposition.compose(sprint5Clock, dataSource);
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
    LocalPolicyOwnerResolver localPolicyOwnerResolver() {
        return new LocalPolicyOwnerResolver();
    }

    @Bean
    PolicyIssueReceiptReadStore policyIssueReceiptReadStore(DataSource dataSource) {
        return new PolicyIssueReceiptReadStore(dataSource);
    }
}
