package assignments.local;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Passwordless session switcher over two fixed synthetic identities. */
@RestController
public final class LocalSessionController {
    private final SecurityContextRepository contexts;

    public LocalSessionController(SecurityContextRepository contexts) { this.contexts = contexts; }

    @GetMapping("/api/csrf")
    public Map<String, String> csrf(@RequestAttribute("_csrf") CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }

    @GetMapping("/api/session")
    public ResponseEntity<?> session(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("code", "UNAUTHENTICATED"));
        }
        return ResponseEntity.ok(Map.of("ownerId", authentication.getName()));
    }

    @PostMapping("/api/session")
    public ResponseEntity<?> selectSyntheticOwner(@RequestBody LocalSessionRequest request,
                                                   HttpServletRequest servletRequest,
                                                   HttpServletResponse servletResponse) {
        if (request == null || !LocalPolicyOwnerResolver.isKnownSyntheticOwner(request.ownerId())) {
            return ResponseEntity.badRequest().body(Map.of("code", "UNKNOWN_SYNTHETIC_OWNER"));
        }
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                request.ownerId(), "", java.util.List.of(new SimpleGrantedAuthority("ROLE_TRAINING_USER")));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        if (servletRequest.getSession(false) != null) servletRequest.changeSessionId();
        contexts.saveContext(context, servletRequest, servletResponse);
        return ResponseEntity.ok(Map.of("ownerId", request.ownerId()));
    }

    @PostMapping("/api/session/logout")
    public ResponseEntity<Void> logout(Authentication authentication,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    public record LocalSessionRequest(String ownerId) { }
}
