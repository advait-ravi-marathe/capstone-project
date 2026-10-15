package coverstory.modules.payments.api;

import coverstory.modules.payments.api.CAP80Seam.ReplayResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

/** Local synthetic replay endpoint. It never accepts amount, owner, application, or checkpoint from the client. */
@RestController
public final class CAP80ReplayController {
    private final ObjectProvider<CAP80ReplayCommand> command;
    public CAP80ReplayController(ObjectProvider<CAP80ReplayCommand> command) { this.command = command; }

    @PostMapping("/api/v1/me/payments/mock-confirmations/{eventId}/replay")
    public ResponseEntity<?> replay(@PathVariable("eventId") String eventId, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal == null || principal.getName() == null) return ResponseEntity.status(401).body(Map.of("code", "AUTH_REQUIRED"));
        final UUID owner;
        try { owner = UUID.fromString(principal.getName()); }
        catch (IllegalArgumentException invalid) { return ResponseEntity.status(401).body(Map.of("code", "AUTH_REQUIRED")); }
        CAP80ReplayCommand useCase = command.getIfAvailable();
        if (useCase == null) return ResponseEntity.status(503).body(Map.of("code", "REPLAY_UNAVAILABLE"));
        try {
            ReplayResult result = useCase.replay(owner, eventId);
            return ResponseEntity.ok(Map.of("status", result.status(), "checkpoint", result.checkpointAfter()));
        } catch (CAP80ReplayCommand.ReplayNotFoundException missing) {
            return ResponseEntity.status(404).body(Map.of("code", "NOT_FOUND"));
        } catch (UnsupportedOperationException todo) {
            return ResponseEntity.status(501).body(Map.of("code", "REPLAY_NOT_IMPLEMENTED"));
        } catch (IllegalArgumentException invalid) {
            return ResponseEntity.badRequest().body(Map.of("code", "INVALID_REFERENCE"));
        } catch (RuntimeException unavailable) {
            return ResponseEntity.status(503).body(Map.of("code", "REPLAY_UNAVAILABLE"));
        }
    }
}
