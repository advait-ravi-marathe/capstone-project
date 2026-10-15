package coverstory.modules.shortlist;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import coverstory.modules.catalog.CatalogPort;

@Configuration
public class ShortlistPersistenceConfiguration {
    @Bean @ConditionalOnProperty(name="coverstory.application.jdbc.url")
    ShortlistPort jdbcShortlistPort(@Qualifier("applicationResumeJdbcTemplate") JdbcTemplate jdbc, CatalogPort catalog) {
        return new JdbcShortlistAdapter(jdbc, catalog);
    }
}
