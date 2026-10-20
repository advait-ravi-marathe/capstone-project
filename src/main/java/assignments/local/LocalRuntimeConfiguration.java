package assignments.local;

import assignments.insurance.PolicyLifecycleService;
import assignments.insurance.PolicyRenewalPreviewPolicy;
import assignments.insurance.PolicyRenewalReadStore;
import assignments.insurance.PolicyReadStore;
import assignments.delivery.IssuanceWorkStore;
import assignments.delivery.JdbcIssuanceWorkStore;
import assignments.delivery.LocalIssuanceDeliveryCommand;
import assignments.delivery.OneBatchDispatcher;
import assignments.renewal.PolicyRenewalPreviewController;
import assignments.renewal.PreviewAdmission;
import assignments.renewal.PreviewCapacityPolicy;
import assignments.renewal.PreviewCapacityExceptionHandler;
import assignments.renewal.PreviewMetrics;
import assignments.renewal.PreviewReadBudgetPolicy;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
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

/** Explicitly assembles the local app; it does not scan the Day 1 or test harness packages. */
@Configuration(proxyBeanMethods = false)
@Profile("local")
@Import({PolicyRenewalPreviewController.class, PreviewCapacityExceptionHandler.class,
        LocalIssuanceDeliveryCommand.class})
public class LocalRuntimeConfiguration {
    @Bean
    Clock localClock(@Value("${coverstory.local.fixed-clock:}") String fixedClock) {
        return fixedClock.isBlank() ? Clock.systemUTC()
                : Clock.fixed(Instant.parse(fixedClock), ZoneOffset.UTC);
    }

    @Bean
    PlatformTransactionManager renewalReadTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    PlatformTransactionManager issuanceDeliveryTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    JdbcTemplate renewalReadJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    JdbcTemplate issuanceDeliveryJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    IssuanceWorkStore issuanceWorkStore(@Qualifier("issuanceDeliveryJdbcTemplate") JdbcTemplate issuanceDeliveryJdbcTemplate) {
        return new JdbcIssuanceWorkStore(issuanceDeliveryJdbcTemplate);
    }

    @Bean
    OneBatchDispatcher oneBatchDispatcher(IssuanceWorkStore store,
            @Qualifier("issuanceDeliveryTransactionManager")
            PlatformTransactionManager issuanceDeliveryTransactionManager) {
        return new OneBatchDispatcher(store, issuanceDeliveryTransactionManager);
    }

    @Bean
    TransactionTemplate renewalReadTransactionTemplate(
            @Qualifier("renewalReadTransactionManager") PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        template.setReadOnly(true);
        return template;
    }

    @Bean
    PreviewReadBudgetPolicy previewReadBudgetPolicy(
            @Value("${coverstory.preview.lock-timeout-millis:500}") long lockTimeoutMillis,
            @Value("${coverstory.preview.statement-timeout-millis:2000}") long statementTimeoutMillis) {
        PreviewReadBudgetPolicy policy = new PreviewReadBudgetPolicy(lockTimeoutMillis, statementTimeoutMillis);
        policy.validate();
        return policy;
    }

    @Bean
    LocalApplicationComposition.Components localComponents(Clock localClock, DataSource dataSource,
            @Qualifier("renewalReadJdbcTemplate") JdbcTemplate renewalReadJdbcTemplate,
            @Qualifier("renewalReadTransactionTemplate") TransactionTemplate renewalReadTransactionTemplate,
            PreviewReadBudgetPolicy previewReadBudgetPolicy) {
        return LocalApplicationComposition.compose(localClock, dataSource, renewalReadJdbcTemplate,
                renewalReadTransactionTemplate, previewReadBudgetPolicy);
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
