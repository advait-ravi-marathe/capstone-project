package assignments.oidc;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/** Fixed synthetic response used only to verify the isolated local resource-server boundary. */
@RestController
public class OidcContractLabController {
    @GetMapping("/api/oidc/tenant/{tenant}/read")
    public Map<String, String> read(@PathVariable("tenant") String tenant, JwtAuthenticationToken principal) {
        return Map.of("subject", principal.getToken().getSubject(), "tenant", tenant, "scope", "policy.read");
    }

    @GetMapping("/api/oidc/login")
    public Map<String, String> login(@AuthenticationPrincipal OidcUser principal) {
        return Map.of("subject", principal.getSubject(), "issuer", principal.getIssuer().toString());
    }
}
