package assignments.local;

import assignments.insurance.PolicyLifecycleService;
import assignments.insurance.PolicyRenewalPreviewPolicy;
import assignments.insurance.PolicyRenewalReadStore;
import assignments.insurance.PolicyReadStore;

import javax.sql.DataSource;
import java.time.Clock;

/** Explicit composition boundary for the separately runnable synthetic local application. */
public final class LocalApplicationComposition {
    private LocalApplicationComposition() { }

    public static Components compose(Clock clock, DataSource dataSource) {
        // LEARNER TODO Day 58: explicitly compose production adapters and services from these inputs.
        throw new UnsupportedOperationException("Day 58 learner TODO: compose local application");
    }

    public record Components(Clock clock, DataSource dataSource,
                             PolicyLifecycleService lifecycleService,
                             PolicyRenewalPreviewPolicy renewalPreviewPolicy,
                             PolicyReadStore policyReadStore,
                             PolicyRenewalReadStore renewalReadStore) { }
}
