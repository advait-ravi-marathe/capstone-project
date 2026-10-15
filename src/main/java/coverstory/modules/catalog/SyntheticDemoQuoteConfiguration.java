package coverstory.modules.catalog;

import coverstory.api.SyntheticDemoSessionFilter;
import coverstory.modules.catalog.api.CurrentQuotePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Fixed synthetic quote for local UI walkthroughs; there is no quote provider or order operation. */
@Configuration
@Profile("synthetic-demo")
public class SyntheticDemoQuoteConfiguration {
    @Bean CurrentQuotePort syntheticDemoCurrentQuotePort() {
        return (owner,quoteId) -> {
            if(!"demo-health-A-v1".equals(quoteId) ||
                    !(SyntheticDemoSessionFilter.OWNER_A.equals(owner) || SyntheticDemoSessionFilter.OWNER_B.equals(owner)))
                return Optional.empty();
            return Optional.of(new CurrentQuotePort.Snapshot(owner,quoteId,"revision-v2","health-basic",
                    "sandbox-health-A","source-v2",100101L,"INR",true,true,false,
                    "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",Instant.now().plusSeconds(3600)));
        };
    }
}
