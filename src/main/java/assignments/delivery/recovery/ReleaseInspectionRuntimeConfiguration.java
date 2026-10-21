package assignments.delivery.recovery;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
@Import({RecoveryOnlyRuntimeConfiguration.class, ReleaseInspectionCommandRunner.class})
public class ReleaseInspectionRuntimeConfiguration {
    @Bean RecoveryDecisionPolicy recoveryDecisionPolicy() { return new RecoveryDecisionPolicy(); }
    @Bean InspectionReportMapper inspectionReportMapper(RecoveryDecisionPolicy policy) { return new InspectionReportMapper(policy); }
    @Bean InspectionReportCodec inspectionReportCodec(ObjectMapper mapper) { return new InspectionReportCodec(mapper); }
    @Bean ReleaseInspectionRequestPolicy releaseInspectionRequestPolicy() { return new ReleaseInspectionRequestPolicy(); }
    @Bean ReleaseSchemaVerifier releaseSchemaVerifier(org.springframework.jdbc.core.JdbcTemplate jdbc) { return new ReleaseSchemaVerifier(jdbc); }
}
