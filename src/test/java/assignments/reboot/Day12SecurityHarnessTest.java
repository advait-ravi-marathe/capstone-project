package assignments.reboot;

import assignments.insurance.InsuranceDayOneApplication;
import assignments.insurance.JourneyAccessPolicy;
import assignments.insurance.JourneyReadRequest;
import assignments.insurance.QuoteSelectionKey;
import assignments.insurance.QuoteSelectionRequestValidation;
import assignments.insurance.QuoteSelectionService;
import assignments.insurance.ResourceNotFoundException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = InsuranceDayOneApplication.class)
@AutoConfigureMockMvc
@Import(Day12SecurityHarnessTest.SecurityHarness.class)
class Day12SecurityHarnessTest {
    @Autowired MockMvc mvc;

    @Test
    void realSpringSecurityRequiresAuthentication() throws Exception {
        mvc.perform(get("/test/security/identity")).andExpect(status().isUnauthorized());
        MockHttpSession session = login();
        mvc.perform(get("/test/security/identity").session(session))
                .andExpect(status().isOk()).andExpect(authenticated().withUsername("owner-a"));
    }

    @Test
    void realSpringSecurityEnforcesCsrfOnWrites() throws Exception {
        MockHttpSession session = login();
        mvc.perform(post("/test/security/write").session(session))
                .andExpect(status().isForbidden());
        mvc.perform(post("/test/security/write").session(session).with(csrf().useInvalidToken()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/test/security/write").session(session).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void actualQuoteSelectionRouteIsProtectedBeforeItsLearnerPolicyRuns() throws Exception {
        MockHttpSession session = login();
        mvc.perform(post("/api/quote-selections").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selections\":[]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void validCsrfOnQuoteSelectionReachesTestAdapterCollaborators() throws Exception {
        MockHttpSession session = login();
        mvc.perform(post("/api/quote-selections").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selections\":[]}"))
                .andExpect(status().isOk());
    }

    @Test
    @Tag("learnerAcceptance")
    void ownerScopedJourneyHttpReadUsesLearnerPolicy() throws Exception {
        MockHttpSession owner = login();
        mvc.perform(get("/test/journeys/journey-a").session(owner))
                .andExpect(status().isOk());
        MockHttpSession other = login("owner-b");
        mvc.perform(get("/test/journeys/journey-a").session(other))
                .andExpect(status().isNotFound());
    }

    private MockHttpSession login() throws Exception { return login("owner-a"); }

    private MockHttpSession login(String username) throws Exception {
        MvcResult result = mvc.perform(post("/login").param("username", username).param("password", "secret")
                        .with(csrf()))
                .andExpect(authenticated().withUsername(username)).andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @TestConfiguration
    @EnableWebSecurity
    static class SecurityHarness {
        @Bean SecurityFilterChain harnessFilterChain(HttpSecurity http) throws Exception {
            return http.authorizeHttpRequests(auth -> auth
                            .requestMatchers("/test/security/**", "/test/journeys/**").authenticated()
                            .anyRequest().permitAll())
                    .exceptionHandling(errors -> errors.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                    .formLogin(login -> login.permitAll()).build();
        }
        @Bean UserDetailsService harnessUsers() {
            return new InMemoryUserDetailsManager(
                    User.withUsername("owner-a").password("{noop}secret").roles("USER").build(),
                    User.withUsername("owner-b").password("{noop}secret").roles("USER").build());
        }
        @Bean JourneyAccessPolicy journeyAccessPolicy() { return new JourneyAccessPolicy(); }
        @Bean @Primary QuoteSelectionRequestValidation securityHarnessRequestValidation() {
            return request -> { };
        }
        @Bean @Primary QuoteSelectionService securityHarnessSelectionService() {
            return selections -> selections.stream()
                    .map(input -> new QuoteSelectionKey(input.journeyId(), input.quoteVersionId()))
                    .toList();
        }
        @Bean SecurityHarnessController securityHarnessController(JourneyAccessPolicy policy) {
            return new SecurityHarnessController(policy);
        }
    }

    @RestController
    static class SecurityHarnessController {
        private final JourneyAccessPolicy policy;
        SecurityHarnessController(JourneyAccessPolicy policy) { this.policy = policy; }
        @GetMapping("/test/security/identity") String identity(Principal principal) { return principal.getName(); }
        @PostMapping(value="/test/security/write", consumes=MediaType.ALL_VALUE) String write() { return "ok"; }
        @GetMapping("/test/journeys/{journeyId}") org.springframework.http.ResponseEntity<String> journey(
                Principal principal, @PathVariable("journeyId") String journeyId) {
            try {
                policy.assertCanAccess(principal, "owner-a", new JourneyReadRequest(journeyId, "attacker"));
                return org.springframework.http.ResponseEntity.ok(journeyId);
            } catch (ResourceNotFoundException hidden) {
                return org.springframework.http.ResponseEntity.notFound().build();
            }
        }
    }
}
