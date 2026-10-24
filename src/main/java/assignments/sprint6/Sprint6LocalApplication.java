package assignments.sprint6;

import assignments.local.LocalBindAddressGuard;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Import;

/** Bounded Days 71–84 runtime; later timed-read configuration is deliberately excluded. */
@SpringBootConfiguration
@EnableAutoConfiguration
@Import(Sprint6LocalConfiguration.class)
public class Sprint6LocalApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Sprint6LocalApplication.class);
        application.setAdditionalProfiles("local", "sprint6-local");
        application.setDefaultProperties(java.util.Map.of("server.address", "127.0.0.1"));
        application.addListeners(event -> {
            if (event instanceof org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent prepared) {
                LocalBindAddressGuard.requireLoopback(prepared.getEnvironment().getProperty("server.address"));
            }
        });
        application.run(args);
    }
}
