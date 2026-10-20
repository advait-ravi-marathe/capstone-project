package assignments.local;

import assignments.insurance.PolicyLifecycleService;
import assignments.insurance.PolicyRenewalPreviewPolicy;
import assignments.insurance.PolicyRenewalReadStore;
import assignments.insurance.PolicyReadStore;

import javax.sql.DataSource;
import java.time.Clock;
import java.util.Objects;

/** Explicit composition boundary for the separately runnable synthetic local application. */
public final class LocalApplicationComposition {
    private LocalApplicationComposition() { }

    public static Components compose(Clock clock, DataSource dataSource) {
        Objects.requireNonNull(clock, "clock");
        Objects.requireNonNull(dataSource, "dataSource");
        return new Components(clock, dataSource, new PolicyLifecycleService(clock),
                new PolicyRenewalPreviewPolicy(), new JdbcPolicyReadStore(dataSource),
                new assignments.insurance.JdbcPolicyRenewalReadStore(dataSource));
    }

    public record Components(Clock clock, DataSource dataSource,
                             PolicyLifecycleService lifecycleService,
                             PolicyRenewalPreviewPolicy renewalPreviewPolicy,
                             PolicyReadStore policyReadStore,
                             PolicyRenewalReadStore renewalReadStore) { }
}
