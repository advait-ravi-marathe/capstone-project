package coverstory.modules.catalog;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/** Optional local JDBC binding. The ordinary starter still boots without a database. */
@Configuration
@ConditionalOnProperty(name = "coverstory.catalog.jdbc.url")
public class CatalogDiscoveryConfiguration {
    @Bean
    DataSource coverStoryCatalogDataSource(
            org.springframework.core.env.Environment environment) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(environment.getRequiredProperty("coverstory.catalog.jdbc.url"));
        dataSource.setUsername(environment.getProperty("coverstory.catalog.jdbc.username", ""));
        dataSource.setPassword(environment.getProperty("coverstory.catalog.jdbc.password", ""));
        dataSource.setDriverClassName("org.postgresql.Driver");
        return dataSource;
    }

    @Bean
    JdbcTemplate catalogDiscoveryJdbcTemplate(DataSource coverStoryCatalogDataSource) {
        return new JdbcTemplate(coverStoryCatalogDataSource);
    }

    @Bean
    CatalogDiscoveryPort catalogDiscoveryPort(JdbcTemplate catalogDiscoveryJdbcTemplate) {
        return new JdbcCatalogDiscoveryAdapter(catalogDiscoveryJdbcTemplate);
    }
}
