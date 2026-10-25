package assignments.oidc;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

/** Learner acceptance seam; the main harness excludes this TODO until explicitly enabled. */
@Tag("learnerAcceptance")
class OidcTenantPolicyAcceptanceTest {
    private final OidcTenantPolicy policy = new OidcTenantPolicy();

    @Test void matchingTenantAndReadScopeAreRequired() {
        assertTrue(policy.mayRead(token("tenant-a", "policy.read"), "tenant-a"));
        assertFalse(policy.mayRead(token("tenant-a", "policy.write"), "tenant-a"));
        assertFalse(policy.mayRead(token("tenant-a", "policy.read"), "tenant-b"));
        assertFalse(policy.mayRead(token("", "policy.read"), "tenant-a"));
        assertFalse(policy.mayRead(token(7, "policy.read"), "tenant-a"));
        assertFalse(policy.mayRead(token("tenant-a", "xpolicy.ready policy.readwrite"), "tenant-a"));
        assertFalse(policy.mayRead(token("tenant-a", 7), "tenant-a"));
        assertFalse(policy.mayRead(token("tenant-a", "policy.read"), " "));
        assertTrue(policy.mayRead(token("tenant-a", "policy.read policy.write"), "tenant-a"),
                "the exact read scope remains allowed when another independent scope is present");
    }

    private static Jwt token(Object tenant, Object scope) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("synthetic-test-token").header("alg", "RS256")
                .issuer("http://127.0.0.1/test-issuer").subject("synthetic-user")
                .issuedAt(now.minusSeconds(1)).expiresAt(now.plusSeconds(60))
                .claim("tenant_id", tenant).claim("scope", scope).build();
    }
}
