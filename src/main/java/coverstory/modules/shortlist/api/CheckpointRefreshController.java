package coverstory.modules.shortlist.api;

import coverstory.modules.shortlist.refresh.PreviewCheckpointRefresh;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** CAP-636 disabled-by-default route seam. Enabling it before the use case is implemented is not supported. */
@RestController
@RequestMapping("/api/v1/shortlists/{shortlistId}/checkpoint-refresh-previews")
@ConditionalOnProperty(name="coverstory.shortlist.refresh.enabled", havingValue="true")
public final class CheckpointRefreshController {
    private final PreviewCheckpointRefresh preview;
    public CheckpointRefreshController(PreviewCheckpointRefresh preview) { this.preview=preview; }
    @PostMapping
    public ResponseEntity<?> create(@PathVariable UUID shortlistId, @RequestBody(required=false) Body body,
                                    HttpServletRequest request) {
        Principal principal=request.getUserPrincipal();
        UUID owner;
        try { owner=principal==null?null:UUID.fromString(principal.getName()); }
        catch (RuntimeException invalid) { owner=null; }
        if(owner==null)return ResponseEntity.status(401).body(Map.of("code","AUTH_REQUIRED"));
        if(body==null || body.expectedRevision()<1)return ResponseEntity.badRequest().body(Map.of("code","INVALID_PREVIEW"));
        try {
            PreviewCheckpointRefresh.Reply reply=preview.preview(owner,shortlistId,
                    new PreviewCheckpointRefresh.Request(body.ownerId(),body.expectedRevision()));
            return ResponseEntity.status(reply.status()).body(reply.preview()==null?Map.of("code",reply.safeCode()):reply.preview());
        } catch (UnsupportedOperationException todo) {
            return ResponseEntity.status(501).body(Map.of("code","CHECKPOINT_REFRESH_NOT_IMPLEMENTED"));
        } catch (RuntimeException unavailable) {
            return ResponseEntity.status(503).body(Map.of("code","CHECKPOINT_REFRESH_UNAVAILABLE"));
        }
    }
    public record Body(UUID ownerId, long expectedRevision) { }
}
