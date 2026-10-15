package coverstory.modules.catalog;

import coverstory.modules.catalog.api.CurrentQuotePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@ConditionalOnProperty(name="coverstory.catalog.quote-review.enabled", havingValue="true")
public class QuoteReviewConfiguration {
    @Bean
    QuoteReviewService quoteReviewService(CurrentQuotePort quotes) {
        return new QuoteReviewService(quotes, Clock.systemUTC());
    }
}
