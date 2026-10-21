package assignments.local;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Dedicated application entry point; intentionally separate from Day 1's starter. */
@SpringBootApplication(scanBasePackages = "assignments.local")
public class LocalCoverStoryApplication {
    public static void main(String[] args) {
        boolean recoveryOnly = java.util.Arrays.stream(args).anyMatch(arg -> arg.startsWith("--coverstory.recovery.action="));
        SpringApplication application = new SpringApplication(recoveryOnly
                ? assignments.delivery.recovery.RecoveryOnlyApplication.class : LocalCoverStoryApplication.class);
        application.setAdditionalProfiles("local");
        if (recoveryOnly || java.util.Arrays.stream(args).anyMatch("--coverstory.delivery.run-once"::equals)) {
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
