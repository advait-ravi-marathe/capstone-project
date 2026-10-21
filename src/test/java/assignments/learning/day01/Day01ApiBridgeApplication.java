package assignments.learning.day01;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/** Test-runtime-only app. It explicitly registers two teaching controllers and scans no production beans. */
@SpringBootConfiguration(proxyBeanMethods = false)
@EnableAutoConfiguration(excludeName = {
        "org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration",
        "org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration",
        "org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration",
        "org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration",
        "org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration"
})
public class Day01ApiBridgeApplication {
    public static void main(String[] args) {
        SpringApplication.run(Day01ApiBridgeApplication.class, args);
    }

    @Bean
    Day01GreetingController day01GreetingController() {
        return new Day01GreetingController();
    }

    @Bean
    Day01ApiPractice day01ApiPractice() {
        return new Day01ApiPractice();
    }

    @Bean
    WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> loopbackOnly(Environment environment) {
        return factory -> {
            String configuredAddress = environment.getProperty("server.address", "127.0.0.1");
            try {
                InetAddress address = InetAddress.getByName(configuredAddress);
                if (!address.isLoopbackAddress()) {
                    throw new IllegalArgumentException("Day 1 teaching server must bind to loopback only");
                }
                factory.setAddress(InetAddress.getByName("127.0.0.1"));
            } catch (UnknownHostException exception) {
                throw new IllegalArgumentException("Day 1 teaching server address must be a loopback address", exception);
            }
        };
    }
}
