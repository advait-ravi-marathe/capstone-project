package capstone.coverstory.application;

import coverstory.modules.application.api.ReviewStatusFactsPort;
import coverstory.modules.payments.api.ReviewPaymentFactsPort;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/** Test-only instructor transport. It is never registered in the ordinary application. */
@RestController
@Profile("cap114-instructor-test")
@RequestMapping("/api/support/__instructor/cap114")
final class CAP114InstructorController {
    private static final UUID SYNTHETIC_APPLICATION = UUID.fromString("11400000-0000-4000-8000-000000000001");
    private final CAP114ReferenceAlertWindow reference;
    private final ReviewStatusFactsPort application;
    private final ReviewPaymentFactsPort payments;

    CAP114InstructorController(CAP114ReferenceAlertWindow reference, ReviewStatusFactsPort application,
                               ReviewPaymentFactsPort payments) {
        this.reference=reference; this.application=application; this.payments=payments;
    }

    @GetMapping("/summary")
    ResponseEntity<?> summary(HttpServletRequest request) {
        if (!instructor(request)) return ResponseEntity.status(401).body(new SafeError("AUTH_REQUIRED"));
        var review=application.findSyntheticReview(SYNTHETIC_APPLICATION);
        var payment=payments.findForApplication(SYNTHETIC_APPLICATION);
        var state=reference.snapshot();
        return ResponseEntity.ok(new Summary(state.alertState(),state.qualifyingWindows(),state.firingTransitions(),
                state.resolutionTransitions(),state.recordedWindows(),state.needsInformationCount(),
                review.status(),review.requestedFieldCodes(),payment.status(),payment.dispatchConfirmed(),"SYNTHETIC_ONLY"));
    }

    @PostMapping("/windows")
    ResponseEntity<?> record(HttpServletRequest request,@RequestBody WindowRequest input) {
        if (!instructor(request)) return ResponseEntity.status(401).body(new SafeError("AUTH_REQUIRED"));
        try {
            var result=reference.record(input.windowId(),input.eligibleRequests(),input.errorRequests(),input.needsInformationCount());
            return ResponseEntity.ok(new WindowResult(result.alertState(),result.qualifyingWindows(),result.firingTransitions(),
                    result.resolutionTransitions(),result.recordedWindows(),result.needsInformationCount(),result.duplicate()));
        } catch (IllegalArgumentException invalid) { return ResponseEntity.badRequest().body(new SafeError("INVALID_SYNTHETIC_WINDOW")); }
    }

    @PostMapping("/reset")
    ResponseEntity<?> reset(HttpServletRequest request) {
        if (!instructor(request)) return ResponseEntity.status(401).body(new SafeError("AUTH_REQUIRED"));
        reference.reset(); return ResponseEntity.noContent().build();
    }

    private static boolean instructor(HttpServletRequest request) {
        Principal principal=request.getUserPrincipal();
        return principal!=null && "11400000-0000-4000-8000-000000000999".equals(principal.getName());
    }
    record WindowRequest(String windowId,int eligibleRequests,int errorRequests,int needsInformationCount) { }
    record WindowResult(String alertState,int qualifyingWindows,int firingTransitions,int resolutionTransitions,
                        int recordedWindows,int needsInformationCount,boolean duplicate) { }
    record Summary(String alertState,int qualifyingWindows,int firingTransitions,int resolutionTransitions,
                   int recordedWindows,int needsInformationCount,String applicationReviewStatus,
                   List<String> requestedFieldCodes,String paymentStatus,boolean dispatchConfirmed,String classification) { }
    record SafeError(String code) { }
}
