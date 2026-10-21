package assignments.renewal;

import assignments.insurance.PolicyRenewalEligibility;
import assignments.insurance.PolicyRenewalFacts;
import assignments.insurance.PolicyRenewalPreview;
import assignments.insurance.PolicyRenewalPreviewPolicy;
import assignments.insurance.PolicyRenewalReadStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/** Read-only owner-scoped renewal preview endpoint. */
@RestController
public final class PolicyRenewalPreviewController {
    private final PolicyRenewalReadStore readStore;
    private final PolicyRenewalPreviewPolicy policy;
    private final Clock clock;

    public PolicyRenewalPreviewController(PolicyRenewalReadStore readStore,
                                          PolicyRenewalPreviewPolicy policy, Clock clock) {
        this.readStore = readStore;
        this.policy = policy;
        this.clock = clock;
    }

    @GetMapping("/api/policies/{policyId}/renewal-preview")
    public ResponseEntity<?> getRenewalPreview(@PathVariable("policyId") String policyId, Principal principal) {
        Optional<String> owner = authenticatedOwner(principal);
        if (owner.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("code", "UNAUTHENTICATED"));
        Optional<PolicyRenewalFacts> facts = readStore.findByPolicyIdAndOwnerId(policyId, owner.get());
        if (facts.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("code", "POLICY_NOT_FOUND"));
        PolicyRenewalEligibility eligibility = policy.eligibility(facts.get(), LocalDate.now(clock));
        if (eligibility != PolicyRenewalEligibility.AVAILABLE) return unavailable(eligibility);
        return available(policy.buildPreview(facts.get()));
    }

    Optional<String> authenticatedOwner(Principal principal) {
        // LEARNER TODO Day 49: derive owner only from the authenticated server principal.
        throw todo(49);
    }

    ResponseEntity<?> unavailable(PolicyRenewalEligibility eligibility) {
        // LEARNER TODO Day 50: map non-ready decisions to a stable safe 409 response.
        throw todo(50);
    }

    ResponseEntity<?> available(PolicyRenewalPreview preview) {
        // LEARNER TODO Day 51: expose only the approved read-only projection.
        throw todo(51);
    }

    private static UnsupportedOperationException todo(int day) {
        return new UnsupportedOperationException("Day %02d learner TODO: implement renewal preview API".formatted(day));
    }
}
