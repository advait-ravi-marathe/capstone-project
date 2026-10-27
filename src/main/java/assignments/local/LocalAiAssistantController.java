package assignments.local;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Assistant-supplied local API boundary; caller identity comes only from the session. */
@RestController
@RequestMapping("/api/ai")
public final class LocalAiAssistantController {
    private final LocalAiAssistantGateway gateway;

    public LocalAiAssistantController(LocalAiAssistantGateway gateway) { this.gateway = gateway; }

    @PostMapping("/policy-assistant")
    public ResponseEntity<?> ask(@RequestBody(required = false) QuestionRequest request,
                                 Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !LocalPolicyOwnerResolver.isKnownSyntheticOwner(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("code", "UNAUTHENTICATED"));
        }
        if (request == null || request.question() == null || request.question().isBlank()
                || request.question().length() > 1000) {
            return ResponseEntity.badRequest().body(Map.of("code", "INVALID_QUESTION"));
        }
        try {
            return ResponseEntity.ok(gateway.answer(request.question(), authentication.getName()));
        } catch (AiAssistantUnavailableException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("code", "AI_PROVIDER_UNAVAILABLE"));
        }
    }

    public record QuestionRequest(String question) { }
}
