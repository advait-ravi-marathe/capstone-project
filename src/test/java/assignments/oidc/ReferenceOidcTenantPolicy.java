package assignments.oidc;

import org.springframework.security.oauth2.jwt.Jwt;

/** Instructor-only policy witness used to exercise the actual HTTP authorization path. */
final class ReferenceOidcTenantPolicy extends OidcTenantPolicy {
    @Override public boolean mayRead(Jwt token, String requestedTenant) {
        if (requestedTenant == null || requestedTenant.isBlank()) return false;
        Object scopeClaim = token.getClaims().get("scope");
        boolean scope = scopeClaim instanceof String value && java.util.Arrays.asList(value.split("\\s+")).contains("policy.read");
        Object tenant = token.getClaims().get("tenant_id");
        return tenant instanceof String value && !value.isBlank() && value.equals(requestedTenant) && scope;
    }
}
