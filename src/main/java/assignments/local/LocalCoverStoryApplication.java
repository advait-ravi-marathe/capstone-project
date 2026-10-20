package assignments.local;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Dedicated application entry point; intentionally separate from Day 1's starter. */
@SpringBootApplication(scanBasePackages = "assignments.local")
public class LocalCoverStoryApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(LocalCoverStoryApplication.class);
        application.setAdditionalProfiles("local");
        application.setDefaultProperties(java.util.Map.of("server.address", "127.0.0.1"));
        application.addListeners(event -> {
            if (event instanceof org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent prepared) {
                LocalBindAddressGuard.requireLoopback(prepared.getEnvironment().getProperty("server.address"));
            }
        });
        application.run(args);
    }
}
