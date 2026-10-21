package assignments.reboot;

import assignments.insurance.JourneyAccessPolicy;
import assignments.insurance.JourneyReadRequest;
import assignments.insurance.ResourceNotFoundException;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.concurrent.atomic.AtomicInteger;

/** Test-only form-login and cookie-CSRF fixture. It does not configure production authentication. */
@TestConfiguration(proxyBeanMethods = false)
@EnableWebSecurity
@Import(Day21SecurityHarness.Controller.class)
class Day21SecurityHarness {
    @Bean
    SecurityFilterChain day21TestFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/test/day21/csrf", "/login").permitAll()
                        .requestMatchers("/test/day21/**").authenticated()
                        .anyRequest().denyAll())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .formLogin(login -> login.permitAll())
                .build();
    }

    @Bean
    UserDetailsService day21TestUsers() {
        return new InMemoryUserDetailsManager(
                User.withUsername("owner-a").password("{noop}test-password").roles("USER").build(),
                User.withUsername("owner-b").password("{noop}test-password").roles("USER").build());
    }

    @Bean JourneyAccessPolicy day21ActualOwnerPolicy() { return new JourneyAccessPolicy(); }
    @Bean Day21SecurityHarness.Counters day21TestCounters() { return new Counters(); }

    static final class Counters {
        final AtomicInteger writes = new AtomicInteger();
        final AtomicInteger ownerChecks = new AtomicInteger();
        void reset() { writes.set(0); ownerChecks.set(0); }
    }

    @RestController
    static final class Controller {
        private final JourneyAccessPolicy accessPolicy;
        private final Counters counters;

        Controller(JourneyAccessPolicy accessPolicy, Counters counters) {
            this.accessPolicy = accessPolicy;
            this.counters = counters;
        }

        @GetMapping("/test/day21/csrf")
        ResponseEntity<Void> issueCsrfCookie(CsrfToken token) {
            token.getToken(); // Resolve the deferred token so the response writes XSRF-TOKEN.
            return ResponseEntity.noContent().build();
        }

        @PostMapping("/test/day21/write")
        ResponseEntity<Void> write() {
            counters.writes.incrementAndGet();
            return ResponseEntity.noContent().build();
        }

        @PostMapping("/test/day21/journeys/{journeyId}/read")
        ResponseEntity<Void> readJourney(Principal principal, @PathVariable("journeyId") String journeyId,
                @RequestParam(name = "ownerId", defaultValue = "owner-a") String ownerId) {
            counters.ownerChecks.incrementAndGet();
            try {
                accessPolicy.assertCanAccess(principal, "owner-a", new JourneyReadRequest(journeyId, ownerId));
                return ResponseEntity.ok().build();
            } catch (ResourceNotFoundException concealed) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }
    }
}
