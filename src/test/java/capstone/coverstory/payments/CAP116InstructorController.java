package capstone.coverstory.payments;

import capstone.coverstory.payments.internal.AcceptedRevisionBalance;
import capstone.coverstory.payments.internal.AcceptedRevisionBalance.*;
import coverstory.modules.application.api.AcceptedOfferRevisionFactsPort;
import coverstory.modules.payments.api.ConfirmedPaymentEffectFactsPort;
import coverstory.modules.payments.api.PendingAdjustmentFactsPort;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

/** Test-profile only read endpoint. It cannot create a payment or refund allocation. */
@RestController
@Profile("cap116-instructor-test")
@RequestMapping("/api/payments/__instructor/cap116")
final class CAP116InstructorController {
    private static final String INSTRUCTOR_OWNER = "11500000-0000-4000-8000-000000000001";
    private final AcceptedOfferRevisionFactsPort application;
    private final ConfirmedPaymentEffectFactsPort payments;
    private final PendingAdjustmentFactsPort pending;
    private final CAP116ReferenceBalanceCalculator oracle;
    CAP116InstructorController(AcceptedOfferRevisionFactsPort application, ConfirmedPaymentEffectFactsPort payments,
                               PendingAdjustmentFactsPort pending, CAP116ReferenceBalanceCalculator oracle) {
        this.application = application; this.payments = payments; this.pending = pending; this.oracle = oracle;
    }

    @GetMapping("/{applicationId}") ResponseEntity<?> reconcile(HttpServletRequest request, @PathVariable("applicationId") UUID applicationId) {
        if (!authorized(request)) return ResponseEntity.status(401).body(new SafeError("INSTRUCTOR_SESSION_REQUIRED"));
        var facts = application.findAcceptedOffer(applicationId);
        if (facts == null) return ResponseEntity.notFound().build();
        var accepted = new AcceptedOffer(facts.ownerId(), facts.applicationId(), facts.offerId(), facts.revision(),
                facts.contentSha256(), facts.totalMinor(), facts.currency(), facts.accepted());
        Reconciliation report = oracle.calculate(accepted, payments.findObservedEffects(applicationId), pending.findPendingAllocations(applicationId));
        return ResponseEntity.ok(report);
    }
    private static boolean authorized(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        return principal != null && INSTRUCTOR_OWNER.equals(principal.getName());
    }
    record SafeError(String code) { }
}
