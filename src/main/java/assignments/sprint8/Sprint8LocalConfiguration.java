package assignments.sprint8;

import assignments.delivery.LocalIssuanceDeliveryCommand;
import assignments.insurance.PolicyLifecycleService;
import assignments.insurance.PolicyReadStore;
import assignments.insurance.PolicyRenewalPreviewPolicy;
import assignments.local.LocalApplicationComposition;
import assignments.local.LocalMetricsConfiguration;
import assignments.local.LocalPolicyIssueController;
import assignments.local.LocalPolicyOwnerResolver;
import assignments.local.LocalPolicyRenewalReadStoreConfiguration;
import assignments.local.LocalSecurityConfiguration;
import assignments.local.LocalSessionController;
import assignments.local.PolicyIssueReceiptReadStore;
import assignments.renewal.PolicyRenewalPreviewController;
import assignments.renewal.PreviewCapacityExceptionHandler;
import assignments.renewal.PreviewReadBudgetPolicy;
import assignments.sprint6.Sprint6PreviewCapacityConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/** Bounded Days 99–112 web and delivery composition; no manual-recovery wrapper is wired. */
@Configuration(proxyBeanMethods = false)
@Profile("sprint8-local")
@Import({Sprint8IssuanceDeliveryConfiguration.class, Sprint6PreviewCapacityConfiguration.class,
        LocalPolicyRenewalReadStoreConfiguration.class, LocalMetricsConfiguration.class,
        LocalSecurityConfiguration.class, LocalSessionController.class, LocalPolicyIssueController.class,
        PolicyRenewalPreviewController.class, PreviewCapacityExceptionHandler.class,
        LocalIssuanceDeliveryCommand.class})
public class Sprint8LocalConfiguration {
    @Bean
    Clock sprint8Clock(@Value("${coverstory.local.fixed-clock:}") String fixedClock) {
        return fixedClock.isBlank() ? Clock.systemUTC()
                : Clock.fixed(Instant.parse(fixedClock), ZoneOffset.UTC);
    }

    @Bean
    PlatformTransactionManager sprint8RenewalReadTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    PlatformTransactionManager issuanceDeliveryTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    JdbcTemplate sprint8RenewalReadJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    JdbcTemplate issuanceDeliveryJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    TransactionTemplate sprint8RenewalReadTransactionTemplate(
            @Qualifier("sprint8RenewalReadTransactionManager") PlatformTransactionManager manager) {
        TransactionTemplate template = new TransactionTemplate(manager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        template.setReadOnly(true);
        return template;
    }

    @Bean
    PreviewReadBudgetPolicy sprint8PreviewReadBudgetPolicy(
            @Value("${coverstory.preview.lock-timeout-millis:500}") long lockTimeoutMillis,
            @Value("${coverstory.preview.statement-timeout-millis:2000}") long statementTimeoutMillis) {
        PreviewReadBudgetPolicy policy = new PreviewReadBudgetPolicy(lockTimeoutMillis, statementTimeoutMillis);
        policy.validate();
        return policy;
    }

    @Bean
    LocalApplicationComposition.Components sprint8Components(
            Clock sprint8Clock, DataSource dataSource,
            @Qualifier("sprint8RenewalReadJdbcTemplate") JdbcTemplate jdbc,
            @Qualifier("sprint8RenewalReadTransactionTemplate") TransactionTemplate transaction,
            PreviewReadBudgetPolicy budgets) {
        return LocalApplicationComposition.compose(sprint8Clock, dataSource, jdbc, transaction, budgets);
    }

    @Bean
    PolicyLifecycleService sprint8PolicyLifecycleService(LocalApplicationComposition.Components components) {
        return components.lifecycleService();
    }

    @Bean
    PolicyRenewalPreviewPolicy sprint8PolicyRenewalPreviewPolicy(LocalApplicationComposition.Components components) {
        return components.renewalPreviewPolicy();
    }

    @Bean
    PolicyReadStore sprint8PolicyReadStore(LocalApplicationComposition.Components components) {
        return components.policyReadStore();
    }

    @Bean
    LocalPolicyOwnerResolver sprint8LocalPolicyOwnerResolver() {
        return new LocalPolicyOwnerResolver();
    }

    @Bean
    PolicyIssueReceiptReadStore sprint8PolicyIssueReceiptReadStore(DataSource dataSource) {
        return new PolicyIssueReceiptReadStore(dataSource);
    }
}
