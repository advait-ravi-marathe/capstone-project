package coverstory.modules.application.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

/** Authenticated Application boundary; UUIDs are lookup keys, never authorization. */
@RestController
@RequestMapping("/api/v1/me/journeys")
public final class CAP54ResumeController {
    private final CAP54ResumeUseCase application;

    public CAP54ResumeController(CAP54ResumeUseCase application) { this.application = application; }

    @GetMapping
    public ResponseEntity<?> list(HttpServletRequest request,
                                  @RequestParam(name="limit", defaultValue = "20") int limit,
                                  @RequestParam(name="cursor", required = false) String cursor) {
        String owner = trustedOwner(request);
        if (owner == null) return ResponseEntity.status(401).body(new SafeError("AUTH_REQUIRED"));
        if (limit < 1 || limit > 50) return ResponseEntity.badRequest().body(new SafeError("INVALID_LIMIT"));
        try { return response(application.list(owner, limit, cursor)); }
        catch (UnsupportedOperationException pending) { return ResponseEntity.status(501).body(new SafeError("RESUME_NOT_IMPLEMENTED")); }
        catch (RuntimeException unavailable) { return ResponseEntity.status(503).body(new SafeError("APPLICATION_UNAVAILABLE")); }
    }

    @GetMapping("/{journeyId}/resume")
    public ResponseEntity<?> resume(HttpServletRequest request, @PathVariable("journeyId") UUID journeyId,
                                    @RequestParam(name = "owner_user_id", required = false) String ownerOverride,
                                    @RequestParam(name = "current_step", required = false) String ignoredBrowserStep) {
        String owner = trustedOwner(request);
        if (owner == null) return ResponseEntity.status(401).body(new SafeError("AUTH_REQUIRED"));
        if (ownerOverride != null) return ResponseEntity.badRequest().body(new SafeError("INVALID_OWNER_OVERRIDE"));
        // Browser state is deliberately not passed to Application. It cannot advance a journey.
        try { return response(application.resume(owner, journeyId)); }
        catch (UnsupportedOperationException pending) { return ResponseEntity.status(501).body(new SafeError("RESUME_NOT_IMPLEMENTED")); }
        catch (RuntimeException unavailable) { return ResponseEntity.status(503).body(new SafeError("APPLICATION_UNAVAILABLE")); }
    }

    private static String trustedOwner(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        return principal == null || principal.getName() == null || principal.getName().isBlank() ? null : principal.getName();
    }

    private static ResponseEntity<?> response(CAP54Seam.Reply reply) {
        if (reply.status() == 200) return ResponseEntity.ok(reply.journey() != null ? reply.journey() : reply.page());
        if (reply.status() == 404) return ResponseEntity.status(404).body(new SafeError("NOT_FOUND"));
        if (reply.status() == 400) return ResponseEntity.badRequest().body(new SafeError("INVALID_REQUEST"));
        return ResponseEntity.status(reply.status()).body(new SafeError(reply.errorCode() == null ? "REQUEST_REJECTED" : reply.errorCode()));
    }

    public record SafeError(String code) { }
}
