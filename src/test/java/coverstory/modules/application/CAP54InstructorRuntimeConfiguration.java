package coverstory.modules.application;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.security.Principal;

/** Test-only synthetic auth/session adapter. No production cookie or identity is created. */
@Configuration
class CAP54InstructorRuntimeConfiguration {
    @Bean @Primary FilterRegistrationBean<Filter> cap54SyntheticSessionFilter() {
        Filter filter = (request, response, chain) -> {
            HttpServletRequest http = (HttpServletRequest) request;
            String owner = null;
            Cookie[] cookies = http.getCookies();
            if (cookies != null) for (Cookie cookie : cookies) if (cookie.getName().equals("cap54_test_session")) {
                owner = switch (cookie.getValue()) {
                    case "synthetic-session-a" -> CAP54InstructorResumeOracle.OWNER_A.toString();
                    case "synthetic-session-a-reloaded" -> CAP54InstructorResumeOracle.OWNER_A.toString();
                    case "synthetic-session-b" -> CAP54InstructorResumeOracle.OWNER_B.toString();
                    default -> null;
                };
            }
            if (owner == null) chain.doFilter(request, response);
            else {
                String trustedOwner = owner;
                chain.doFilter(new HttpServletRequestWrapper(http) {
                    @Override public Principal getUserPrincipal() { return () -> trustedOwner; }
                }, response);
            }
        };
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(-100);
        registration.addUrlPatterns("/api/*");
        registration.setName("cap54SyntheticSessionFilter");
        return registration;
    }
}
