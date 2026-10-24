package assignments.sprint8;

import assignments.delivery.IssuanceWorkStore;
import assignments.delivery.JdbcIssuanceWorkStore;
import assignments.delivery.OneBatchDispatcher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/** Sprint 8 delivery wiring deliberately stops at the raw JDBC store. */
@Configuration(proxyBeanMethods = false)
public class Sprint8IssuanceDeliveryConfiguration {
    @Bean
    IssuanceWorkStore sprint8IssuanceWorkStore(
            @Qualifier("issuanceDeliveryJdbcTemplate") JdbcTemplate jdbc) {
        return new JdbcIssuanceWorkStore(jdbc);
    }

    @Bean
    OneBatchDispatcher sprint8OneBatchDispatcher(
            @Qualifier("sprint8IssuanceWorkStore") IssuanceWorkStore store,
            @Qualifier("issuanceDeliveryTransactionManager") PlatformTransactionManager transactionManager) {
        return new OneBatchDispatcher(store, transactionManager);
    }
}
