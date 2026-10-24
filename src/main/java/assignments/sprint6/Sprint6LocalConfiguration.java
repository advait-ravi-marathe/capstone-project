package assignments.sprint6;

import assignments.insurance.PolicyLifecycleService;
import assignments.insurance.PolicyRenewalPreviewPolicy;
import assignments.insurance.PolicyRenewalReadStore;
import assignments.insurance.PolicyReadStore;
import assignments.local.LocalApplicationComposition;
import assignments.local.LocalPolicyIssueController;
import assignments.local.LocalPolicyOwnerResolver;
import assignments.local.LocalSecurityConfiguration;
import assignments.local.LocalSessionController;
import assignments.local.PolicyIssueReceiptReadStore;
import assignments.renewal.PreviewAdmission;
import assignments.renewal.PreviewCapacityExceptionHandler;
import assignments.renewal.PreviewMetrics;
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

/** Explicit Days 71–84 composition. It does not import the later LocalRuntimeConfiguration. */
@Configuration(proxyBeanMethods = false)
@Profile("sprint6-local")
@Import({Sprint6PreviewCapacityConfiguration.class, LocalSecurityConfiguration.class,
        LocalSessionController.class, LocalPolicyIssueController.class,
        PolicyRenewalPreviewController.class, PreviewCapacityExceptionHandler.class})
public class Sprint6LocalConfiguration {
    @Bean
    Clock sprint6Clock(@Value("${coverstory.local.fixed-clock:}") String fixedClock) {
        return fixedClock.isBlank() ? Clock.systemUTC()
                : Clock.fixed(Instant.parse(fixedClock), ZoneOffset.UTC);
    }

    @Bean
    LocalApplicationComposition.Components sprint6Components(Clock sprint6Clock, DataSource dataSource) {
        // The two-argument composition uses the existing owner-scoped JDBC adapter. It avoids
        // the later read-budget/transaction wrapper, which belongs to Days 86+.
        return LocalApplicationComposition.compose(sprint6Clock, dataSource);
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
    PolicyRenewalReadStore policyRenewalReadStore(LocalApplicationComposition.Components components,
                                                   PreviewAdmission admission, PreviewMetrics metrics) {
        // LEARNER TODO Day 79: wrap this exact owner-scoped adapter once in the bounded singleton decorator.
        // Do not import or invoke the Day 86+ read-budget policy or timed-store path here.
        throw new UnsupportedOperationException("Day 79 learner TODO: wire one bounded store around the existing adapter");
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
