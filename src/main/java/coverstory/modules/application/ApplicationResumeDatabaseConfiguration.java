package coverstory.modules.application;

import coverstory.modules.application.api.CAP54Seam;
import coverstory.modules.application.api.ApplicationJourneyOwnershipPort;
import coverstory.modules.application.api.OnboardingPaymentTransitionPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Set;
import java.util.UUID;

/** Optional Application-owned JDBC reads; the starter still runs without a database. */
@Configuration
@ConditionalOnProperty(name="coverstory.application.jdbc.url")
public class ApplicationResumeDatabaseConfiguration {
    @Bean DataSource applicationResumeDataSource(Environment environment) {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl(environment.getRequiredProperty("coverstory.application.jdbc.url"));
        ds.setUsername(environment.getProperty("coverstory.application.jdbc.username", ""));
        ds.setPassword(environment.getProperty("coverstory.application.jdbc.password", ""));
        ds.setDriverClassName("org.postgresql.Driver");
        return ds;
    }

    @Bean JdbcTemplate applicationResumeJdbcTemplate(DataSource applicationResumeDataSource) {
        return new JdbcTemplate(applicationResumeDataSource);
    }

    @Bean JdbcCAP54ResumeReader applicationResumeReader(JdbcTemplate applicationResumeJdbcTemplate) {
        return new JdbcCAP54ResumeReader(applicationResumeJdbcTemplate);
    }

    @Bean CAP54Seam cap54Seam(JdbcCAP54ResumeReader reader) {
        return new CAP54Seam(reader, reader,
                state -> JdbcCAP54ResumeReader.action(state.status()),
                JdbcCAP54ResumeReader.CAP54Cursor::belongsTo);
    }

    @Bean ApplicationJourneyOwnershipPort applicationJourneyOwnershipPort(JdbcTemplate applicationResumeJdbcTemplate) {
        return (owner, candidates) -> {
            if (candidates.isEmpty()) return Set.of();
            String placeholders = String.join(",", java.util.Collections.nCopies(candidates.size(), "?::uuid"));
            java.util.ArrayList<Object> args = new java.util.ArrayList<>(); args.add(owner); args.addAll(candidates);
            return Set.copyOf(applicationResumeJdbcTemplate.query("""
                    SELECT j.journey_id
                      FROM application.journey j
                      JOIN application.application_record a
                        ON a.application_id=j.application_id AND a.owner_id=j.owner_id AND a.revision=j.revision
                     WHERE j.owner_id=?::uuid AND j.journey_id IN (""" + placeholders + ")",
                    (rs, row) -> UUID.fromString(rs.getString(1)), args.toArray()));
        };
    }

    @Bean OnboardingPaymentTransitionPort onboardingPaymentTransitionPort(JdbcTemplate applicationResumeJdbcTemplate) {
        return new OnboardingPaymentTransitionAdapter(applicationResumeJdbcTemplate);
    }
}
