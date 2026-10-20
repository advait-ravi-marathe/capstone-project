package assignments.insurance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Security libraries are present for the later learner exercise, but the current
 * Day 1 slice remains unauthenticated. The learner installs the application
 * filter chain in the Day 12/21 contract; tests use a real isolated chain.
 */
@SpringBootApplication(excludeName = {
        "org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration",
        "org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration",
        "org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration",
        "org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration",
        "org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration"
})
public class InsuranceDayOneApplication {
    public static void main(String[] args) {
        SpringApplication.run(InsuranceDayOneApplication.class, args);
    }
}
