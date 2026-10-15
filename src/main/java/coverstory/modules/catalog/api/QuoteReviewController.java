package coverstory.modules.catalog.api;

import coverstory.modules.catalog.QuoteReviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

/** Owned quote validation boundary; a successful response never creates an order or activates cover. */
@RestController
public final class QuoteReviewController {
    private final ObjectProvider<QuoteReviewService> service;
    public QuoteReviewController(ObjectProvider<QuoteReviewService> service) { this.service=service; }

    @PostMapping("/api/v1/me/quote-validation")
    public ResponseEntity<?> validate(@RequestBody(required=false) QuoteReviewService.Request body, HttpServletRequest request) {
        Principal principal=request.getUserPrincipal();
        if(principal==null || principal.getName()==null) return error(401,"AUTH_REQUIRED");
        final UUID owner;
        try { owner=UUID.fromString(principal.getName()); } catch(IllegalArgumentException invalid) { return error(401,"AUTH_REQUIRED"); }
        QuoteReviewService useCase=service.getIfAvailable(); if(useCase==null) return error(503,"QUOTE_REVIEW_UNAVAILABLE");
        try {
            QuoteReviewService.Reply reply=useCase.validate(owner,body);
            return "REVIEW_REQUIRED".equals(reply.status())
                    ? ResponseEntity.status(409).body(reply) : ResponseEntity.ok(reply);
        }
        catch(QuoteReviewService.InvalidRequest invalid) { return error(400,"INVALID_REVIEW"); }
        catch(QuoteReviewService.NotFound missing) { return error(404,"NOT_FOUND"); }
        catch(QuoteReviewService.Unavailable stale) { return error(409,"QUOTE_UNAVAILABLE"); }
        catch(QuoteReviewService.IncompleteQuote incomplete) { return error(422,"QUOTE_REQUIRES_REVIEW"); }
        catch(QuoteReviewService.StaleReview stale) { return error(409,"REVIEW_VERSION_STALE"); }
        catch(RuntimeException unavailable) { return error(503,"QUOTE_REVIEW_UNAVAILABLE"); }
    }
    private static ResponseEntity<Map<String,String>> error(int status,String code) { return ResponseEntity.status(status).body(Map.of("code",code)); }
}
