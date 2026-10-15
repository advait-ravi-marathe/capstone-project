package coverstory.modules.policy.api;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Synthetic policy/help read route, profile-gated and backed only by a Policy-owned port. */
@RestController
@Profile("coverstory-instructor-runtime")
@RequestMapping("/api/policy/__instructor/help")
public final class InstructorPolicyHelpController {
    private final PolicyHelpReadPort policyHelp;
    public InstructorPolicyHelpController(PolicyHelpReadPort policyHelp) { this.policyHelp = policyHelp; }

    @GetMapping("/{questionId}")
    public ResponseEntity<?> read(@PathVariable("questionId") String questionId, HttpServletRequest request) {
        UUID owner = principal(request);
        if (owner == null) return ResponseEntity.status(401).cacheControl(CacheControl.noStore()).build();
        if (!questionId.matches("issued|pending|unsupported")) return ResponseEntity.badRequest()
                .cacheControl(CacheControl.noStore()).body(Map.of("code", "UNKNOWN_FIXED_QUESTION"));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(policyHelp.read(owner, questionId));
    }

    private static UUID principal(HttpServletRequest request) {
        try {
            var p = request.getUserPrincipal();
            return p == null ? null : UUID.fromString(p.getName());
        } catch (RuntimeException invalid) { return null; }
    }
}
