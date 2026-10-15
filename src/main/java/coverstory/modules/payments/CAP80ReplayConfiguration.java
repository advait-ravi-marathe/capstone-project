package coverstory.modules.payments;

import coverstory.modules.payments.api.CAP80ReplayCommand;
import coverstory.modules.payments.api.CAP80Seam;
import coverstory.modules.payments.api.MockConfirmationFeedPort;
import coverstory.modules.application.api.OnboardingPaymentTransitionPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import javax.sql.DataSource;

@Configuration
public class CAP80ReplayConfiguration {
    @Bean
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnMissingBean(PlatformTransactionManager.class)
    PlatformTransactionManager coverStoryDataSourceTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    CAP80Seam cap80LearnerSeam() { return new CAP80Seam(); }

    @Bean @ConditionalOnProperty(name="coverstory.application.jdbc.url")
    MockConfirmationFeedPort jdbcMockConfirmationFeedPort(
            @Qualifier("applicationResumeJdbcTemplate") JdbcTemplate jdbc,
            PlatformTransactionManager transactions, OnboardingPaymentTransitionPort application) {
        return new JdbcMockConfirmationFeedAdapter(jdbc, transactions, application);
    }

    @Bean @ConditionalOnBean(MockConfirmationFeedPort.class)
    CAP80ReplayCommand cap80ReplayCommand(ObjectProvider<MockConfirmationFeedPort> feed,
                                          CAP80Seam learner) {
        return new CAP80ReplayCommand(feed.getObject(), learner);
    }
}
