package assignments.local;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration(proxyBeanMethods = false)
@Profile("local")
@EnableWebSecurity
public class LocalSecurityConfiguration {
    @Bean
    UserDetailsService noPasswordAuthentication() {
        return username -> { throw new UsernameNotFoundException("Use a fixed synthetic local session."); };
    }

    @Bean
    SecurityContextRepository localSecurityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SecurityFilterChain localSecurityFilterChain(HttpSecurity http,
                                                 SecurityContextRepository localSecurityContextRepository) throws Exception {
        var csrfRequestHandler = new CsrfTokenRequestAttributeHandler();
        http
                .securityContext(context -> context.securityContextRepository(localSecurityContextRepository))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/", "/index.html", "/assets/**", "/favicon.ico",
                                "/actuator/health", "/api/csrf", "/api/session").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/session").permitAll()
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(csrfRequestHandler))
                .exceptionHandling(errors -> errors.authenticationEntryPoint(
                        LocalSecurityConfiguration::writeUnauthenticated))
                .requestCache(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);
        return http.build();
    }

    private static void writeUnauthenticated(jakarta.servlet.http.HttpServletRequest request,
                                             HttpServletResponse response,
                                             org.springframework.security.core.AuthenticationException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"code\":\"UNAUTHENTICATED\"}");
    }
}
