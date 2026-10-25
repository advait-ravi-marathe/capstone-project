package assignments.oidc;

import org.springframework.security.oauth2.jwt.Jwt;

/** Learner seam for tenant/scope authorization after Spring has verified the bearer token. */
public class OidcTenantPolicy {
    public boolean mayRead(Jwt token, String requestedTenant) {
        // TODO: fail closed unless the signed tenant claim matches and policy.read is granted.
        return false;
    }
}
