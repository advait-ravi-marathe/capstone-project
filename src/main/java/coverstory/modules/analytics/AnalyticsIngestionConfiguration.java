package coverstory.modules.analytics;

import tools.jackson.databind.ObjectMapper;
import coverstory.modules.analytics.api.AnalyticsReceiptStore;
import coverstory.modules.analytics.api.AnalyticsScopeHashPort;
import coverstory.modules.analytics.api.CAP58Seam;
import coverstory.modules.application.api.ApplicationJourneyOwnershipPort;
import coverstory.modules.policy.api.CollectionConsentPort;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.time.Clock;

@Configuration
public class AnalyticsIngestionConfiguration {
    /** This TODO seam is on the live request path; an unimplemented validator returns safe 501. */
    @Bean @ConditionalOnMissingBean(CAP58Seam.class)
    CAP58Seam cap58LearnerValidator() { return new CAP58Seam(); }

    @Bean @ConditionalOnProperty(name="coverstory.application.jdbc.url") @ConditionalOnMissingBean(AnalyticsReceiptStore.class)
    AnalyticsReceiptStore analyticsReceiptStore(DataSource dataSource) { return new JdbcAnalyticsReceiptStore(dataSource); }

    /** The versioned HMAC key is supplied by deployment configuration; this code never creates one. */
    @Bean @ConditionalOnProperty(name="coverstory.analytics.scope-hash-key-base64")
    @ConditionalOnMissingBean(AnalyticsScopeHashPort.class)
    AnalyticsScopeHashPort analyticsScopeHasher(org.springframework.core.env.Environment environment) {
        return new HmacAnalyticsScopeHasher(environment.getRequiredProperty("coverstory.analytics.scope-hash-key-base64"));
    }

    /** Missing consent, hashing, or persistence is distinguished from the learner validator TODO. */
    @Bean @ConditionalOnMissingBean(AnalyticsIngestionPort.class)
    AnalyticsIngestionPort analyticsIngestionPort(CAP58Seam validator,
            ObjectProvider<CollectionConsentPort> consent,
            ObjectProvider<ApplicationJourneyOwnershipPort> journeys,
            ObjectProvider<AnalyticsScopeHashPort> hashes,
            ObjectProvider<AnalyticsReceiptStore> receipts,
            ObjectMapper json) {
        return new CAP58AnalyticsAdapter(validator,consent.getIfAvailable(),journeys.getIfAvailable(),
                hashes.getIfAvailable(),receipts.getIfAvailable(),json,Clock.systemUTC());
    }
}
