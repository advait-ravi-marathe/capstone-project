package assignments.sprint8;

import assignments.local.LocalBindAddressGuard;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Import;

/** Explicit bounded Sprint 8 runtime; excludes the later recovery-aware broad local composition. */
@SpringBootConfiguration
@EnableAutoConfiguration
@Import(Sprint8LocalConfiguration.class)
public class Sprint8LocalApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Sprint8LocalApplication.class);
        application.setAdditionalProfiles("local", "sprint8-local");
        if (java.util.Arrays.stream(args).anyMatch("--coverstory.delivery.run-once"::equals)) {
            application.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE);
        }
        application.setDefaultProperties(java.util.Map.of("server.address", "127.0.0.1"));
        application.addListeners(event -> {
            if (event instanceof org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent prepared) {
                LocalBindAddressGuard.requireLoopback(prepared.getEnvironment().getProperty("server.address"));
            }
        });
        application.run(args);
    }
}
