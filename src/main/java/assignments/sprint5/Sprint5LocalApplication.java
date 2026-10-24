package assignments.sprint5;

import assignments.local.LocalBindAddressGuard;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Import;

/** Bounded early-stage application; deliberately does not scan future local runtime configuration. */
@SpringBootConfiguration
@EnableAutoConfiguration
@Import(Sprint5LocalConfiguration.class)
public class Sprint5LocalApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Sprint5LocalApplication.class);
        application.setAdditionalProfiles("local", "sprint5-local");
        application.setDefaultProperties(java.util.Map.of("server.address", "127.0.0.1"));
        application.addListeners(event -> {
            if (event instanceof org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent prepared) {
                LocalBindAddressGuard.requireLoopback(prepared.getEnvironment().getProperty("server.address"));
            }
        });
        application.run(args);
    }
}
