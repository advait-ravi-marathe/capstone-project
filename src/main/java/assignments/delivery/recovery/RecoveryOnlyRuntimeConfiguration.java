package assignments.delivery.recovery;

import assignments.delivery.IssuanceWorkStore;
import assignments.delivery.JdbcIssuanceWorkStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@Import(LocalManualRecoveryCommand.class)
public class RecoveryOnlyRuntimeConfiguration {
    @Bean PlatformTransactionManager issuanceDeliveryTransactionManager(DataSource source) { return new DataSourceTransactionManager(source); }
    @Bean JdbcTemplate issuanceDeliveryJdbcTemplate(DataSource source) { return new JdbcTemplate(source); }
    @Bean IssuanceWorkStore rawIssuanceWorkStore(@Qualifier("issuanceDeliveryJdbcTemplate") JdbcTemplate jdbc) { return new JdbcIssuanceWorkStore(jdbc); }
    @Bean JdbcManualRecoveryStore jdbcManualRecoveryStore(@Qualifier("issuanceDeliveryJdbcTemplate") JdbcTemplate jdbc) { return new JdbcManualRecoveryStore(jdbc); }
    @Bean @Primary IssuanceWorkStore issuanceWorkStore(@Qualifier("rawIssuanceWorkStore") IssuanceWorkStore raw,
            JdbcManualRecoveryStore recovery,@Qualifier("issuanceDeliveryJdbcTemplate") JdbcTemplate jdbc) {
        return new RecoveryAwareIssuanceWorkStore(raw,recovery,jdbc);
    }
    @Bean ManualRecoveryService manualRecoveryService(JdbcManualRecoveryStore recovery,
            @Qualifier("issuanceWorkStore") IssuanceWorkStore guarded,
            @Qualifier("issuanceDeliveryTransactionManager") PlatformTransactionManager tx) {
        return new ManualRecoveryService(recovery,guarded,tx);
    }
}
