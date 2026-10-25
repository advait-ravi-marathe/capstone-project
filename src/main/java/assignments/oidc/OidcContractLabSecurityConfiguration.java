package assignments.oidc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.config.http.SessionCreationPolicy;

/** Isolated offline OIDC resource-server path; it never changes the existing cookie-session profile. */
@Configuration(proxyBeanMethods = false)
@Profile("oidc-contract-lab")
@EnableWebSecurity
public class OidcContractLabSecurityConfiguration {
    @Bean
    SecurityFilterChain oidcContractLabChain(HttpSecurity http,
            org.springframework.security.authorization.AuthorizationManager<RequestAuthorizationContext> tenantReadAuthorization)
            throws Exception {
        http.securityMatcher("/api/oidc/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/oidc/tenant/{tenant}/read").access(tenantReadAuthorization)
                        .anyRequest().denyAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(resource -> resource.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    NimbusJwtDecoder oidcContractLabDecoder(@Value("${assignments.oidc.issuer}") String issuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withIssuerLocation(issuer).build();
        OAuth2TokenValidator<Jwt> issuerAndTime = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> audience = jwt -> jwt.getAudience().contains("coverstory-oidc-lab")
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Required audience is absent", null));
        decoder.setJwtValidator(new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(issuerAndTime, audience));
        return decoder;
    }

    @Bean
    org.springframework.security.authorization.AuthorizationManager<RequestAuthorizationContext> tenantReadAuthorization(
            OidcTenantPolicy policy) {
        return (authentication, context) -> {
            Authentication current = authentication.get();
            if (!(current instanceof JwtAuthenticationToken token)) return new AuthorizationDecision(false);
            String requestedTenant = context.getVariables().get("tenant");
            return new AuthorizationDecision(requestedTenant != null
                    && policy.mayRead(token.getToken(), requestedTenant));
        };
    }

    @Bean
    OidcTenantPolicy oidcTenantPolicy() { return new OidcTenantPolicy(); }
}
