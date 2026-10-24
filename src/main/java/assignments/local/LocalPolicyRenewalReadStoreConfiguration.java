package assignments.local;

import assignments.insurance.PolicyRenewalReadStore;
import assignments.renewal.BoundedPolicyRenewalReadStore;
import assignments.renewal.PreviewAdmission;
import assignments.renewal.PreviewMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Shared Day 79 boundary: one bounded singleton around the runtime's supplied read store. */
@Configuration(proxyBeanMethods = false)
@Profile({"local", "sprint6-local"})
public class LocalPolicyRenewalReadStoreConfiguration {
    @Bean
    PolicyRenewalReadStore policyRenewalReadStore(LocalApplicationComposition.Components components,
                                                   PreviewAdmission admission, PreviewMetrics metrics) {
        // LEARNER TODO Day 79: wrap this exact store once in the bounded singleton decorator.
        // Sprint 6 and later local runtimes import this same bean; do not duplicate the seam.
        throw new UnsupportedOperationException("Day 79 learner TODO: wire one bounded store around the existing adapter");
    }
}
