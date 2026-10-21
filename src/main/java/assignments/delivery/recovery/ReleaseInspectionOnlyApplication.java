package assignments.delivery.recovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.annotation.Import;

/** Small non-web entry point for the additive inspection report; it never runs migrations. */
@SpringBootApplication(scanBasePackages = "assignments.delivery.recovery")
@Import(ReleaseInspectionRuntimeConfiguration.class)
public class ReleaseInspectionOnlyApplication {
    public static void main(String[] args) {
        // Validate before creating a Spring context, DataSource, or database connection.
        new ReleaseInspectionRequestPolicy().parse(args);
        SpringApplication app = new SpringApplication(ReleaseInspectionOnlyApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.setAdditionalProfiles("local", "release-inspection");
        app.setDefaultProperties(java.util.Map.of("spring.flyway.enabled", "false", "spring.sql.init.mode", "never"));
        app.run(args);
    }
}
