package assignments.delivery.recovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Minimal CLI context selected only by an explicit manual-recovery action. */
@SpringBootApplication(scanBasePackages = "assignments.delivery.recovery")
public class RecoveryOnlyApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RecoveryOnlyApplication.class);
        application.setAdditionalProfiles("local");
        application.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE);
        application.run(args);
    }
}
