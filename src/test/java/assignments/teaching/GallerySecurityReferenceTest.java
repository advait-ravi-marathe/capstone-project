package assignments.teaching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** A self-contained filter-chain example; it does not test native session or database behavior. */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = GallerySecurityReferenceTest.Config.class)
@WebAppConfiguration
class GallerySecurityReferenceTest {
    @Autowired WebApplicationContext context;
    @Autowired Gallery gallery;
    MockMvc mvc;

    @BeforeEach void setUp() {
        gallery.calls = 0;
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @EnableWebSecurity
    static class Config {
        @Bean Gallery gallery() { return new Gallery(); }
        @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
            return http.authorizeHttpRequests(auth -> auth
                            .requestMatchers("/gallery/public").permitAll()
                            .anyRequest().authenticated())
                    .exceptionHandling(errors -> errors.authenticationEntryPoint(
                            (request, response, failure) -> response.sendError(401)))
                    .requestCache(AbstractHttpConfigurer::disable)
                    .httpBasic(AbstractHttpConfigurer::disable)
                    .formLogin(AbstractHttpConfigurer::disable)
                    .logout(AbstractHttpConfigurer::disable)
                    .build();
        }
    }

    @RestController
    static class Gallery {
        int calls;
        @GetMapping("/gallery/public") String publicInfo() { return "Open today"; }
        @GetMapping("/gallery/count") String count() { return Integer.toString(calls); }
        @PostMapping("/gallery/check-ins") ResponseEntity<Void> checkIn() {
            calls++;
            return ResponseEntity.noContent().build();
        }
    }

    @Test void publicAndAuthenticatedRoutesDiffer() throws Exception {
        mvc.perform(get("/gallery/public")).andExpect(status().isOk());
        mvc.perform(get("/gallery/count")).andExpect(status().isUnauthorized());
        mvc.perform(get("/gallery/count").with(user("curator"))).andExpect(status().isOk());
        assertEquals(0, gallery.calls);
    }

    @Test void csrfAndIdentityAreSeparateGates() throws Exception {
        mvc.perform(post("/gallery/check-ins").with(csrf().asHeader())).andExpect(status().isUnauthorized());
        assertEquals(0, gallery.calls);
        mvc.perform(post("/gallery/check-ins").with(user("curator"))).andExpect(status().isForbidden());
        assertEquals(0, gallery.calls);
        mvc.perform(post("/gallery/check-ins").with(user("curator")).with(csrf().asHeader().useInvalidToken()))
                .andExpect(status().isForbidden());
        assertEquals(0, gallery.calls);
        mvc.perform(post("/gallery/check-ins").with(user("curator")).with(csrf().asHeader()))
                .andExpect(status().isNoContent());
        assertEquals(1, gallery.calls);
    }
}
