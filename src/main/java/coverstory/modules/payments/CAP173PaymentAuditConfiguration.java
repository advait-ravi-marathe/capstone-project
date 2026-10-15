package coverstory.modules.payments;

import coverstory.modules.payments.api.PaymentAuditWindowReadPort;
import capstone.coverstory.payments.ReconcilePaymentAuditUtcBoundaries;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/** Optional Payments JDBC wiring; the default application still starts without a database. */
@Configuration
@ConditionalOnProperty(name = "coverstory.application.jdbc.url")
public class CAP173PaymentAuditConfiguration {
    @Bean PaymentAuditWindowReadPort paymentAuditWindowReadPort(JdbcTemplate applicationResumeJdbcTemplate) {
        return new JdbcPaymentAuditWindowAdapter(applicationResumeJdbcTemplate);
    }

    @Bean ReconcilePaymentAuditUtcBoundaries reconcilePaymentAuditUtcBoundaries(PaymentAuditWindowReadPort audit) {
        return new ReconcilePaymentAuditUtcBoundaries(audit);
    }
}
