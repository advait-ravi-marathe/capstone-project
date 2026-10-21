package assignments.local;

import assignments.insurance.PolicyLifecycleService;
import assignments.insurance.PolicyRenewalPreviewPolicy;
import assignments.insurance.PolicyRenewalReadStore;
import assignments.insurance.PolicyReadStore;
import assignments.renewal.PreviewReadBudgetPolicy;
import assignments.renewal.TimedPolicyRenewalReadStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.support.TransactionTemplate;

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

    /** Local runtime composition: transaction infrastructure shares the raw pool; only the
     * existing hand-managed JDBC adapter sees Spring's transaction-aware connection proxy. */
    public static Components compose(Clock clock, DataSource dataSource, JdbcTemplate jdbcTemplate,
                                     TransactionTemplate transactionTemplate,
                                     PreviewReadBudgetPolicy budgets) {
        Objects.requireNonNull(clock, "clock");
        Objects.requireNonNull(dataSource, "dataSource");
        Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
        Objects.requireNonNull(transactionTemplate, "transactionTemplate");
        Objects.requireNonNull(budgets, "budgets");
        PolicyRenewalReadStore existingOwnerScopedAdapter =
                new assignments.insurance.JdbcPolicyRenewalReadStore(
                        new TransactionAwareDataSourceProxy(dataSource));
        PolicyRenewalReadStore timedStore = new TimedPolicyRenewalReadStore(
                transactionTemplate, jdbcTemplate, existingOwnerScopedAdapter, budgets);
        return new Components(clock, dataSource, new PolicyLifecycleService(clock),
                new PolicyRenewalPreviewPolicy(), new JdbcPolicyReadStore(dataSource), timedStore);
    }

    public record Components(Clock clock, DataSource dataSource,
                             PolicyLifecycleService lifecycleService,
                             PolicyRenewalPreviewPolicy renewalPreviewPolicy,
                             PolicyReadStore policyReadStore,
                             PolicyRenewalReadStore renewalReadStore) { }
}
