package coverstory.modules.payments.api;

import capstone.coverstory.payments.CAP173AuditAction;
import capstone.coverstory.payments.ReconcilePaymentAuditUtcBoundaries;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Read-only transport; it never accepts an owner ID or initiates payment work. */
@RestController
@Profile("!synthetic-demo")
@ConditionalOnProperty(name = "coverstory.application.jdbc.url")
public final class PaymentAuditWindowController {
    private final CAP173AuditAction audit;

    public PaymentAuditWindowController(CAP173AuditAction audit) { this.audit = audit; }

    @GetMapping("/api/v1/me/payments/audit")
    public ResponseEntity<?> select(@RequestParam("startInclusive") String startInclusive,
                                    @RequestParam("endExclusive") String endExclusive,
                                    HttpServletRequest request) {
        var principal = request.getUserPrincipal();
        if (principal == null || principal.getName() == null || principal.getName().isBlank())
            return ResponseEntity.status(401).cacheControl(CacheControl.noStore()).body(Map.of("code", "SESSION_REQUIRED"));
        final UUID owner;
        final Instant start;
        final Instant end;
        try {
            owner = UUID.fromString(principal.getName());
            start = Instant.parse(startInclusive);
            end = Instant.parse(endExclusive);
        } catch (IllegalArgumentException invalid) {
            return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(Map.of("code", "INVALID_RANGE"));
        }
        try {
            var reply = audit.select(owner, start, end);
            if (reply.status() == ReconcilePaymentAuditUtcBoundaries.Status.INVALID_RANGE)
                return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(Map.of("code", "INVALID_RANGE"));
            return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of(
                    "ids", reply.result().ids(), "count", reply.result().count(), "classification", "SYNTHETIC_READ_ONLY"));
        } catch (UnsupportedOperationException todo) {
            return ResponseEntity.status(501).cacheControl(CacheControl.noStore()).body(Map.of("code", "CAP173_NOT_IMPLEMENTED"));
        }
    }
}
