package assignments.local;

import assignments.insurance.PolicyIssueRequest;
import assignments.insurance.PolicyLifecycleService;
import assignments.insurance.PolicySnapshot;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Optional;

/** Thin local HTTP adapter that delegates lifecycle decisions to the existing domain service. */
@RestController
@RequestMapping("/api/policies")
public final class LocalPolicyIssueController {
    private final PolicyLifecycleService lifecycleService;
    private final LocalPolicyOwnerResolver ownerResolver;
    private final DataSource dataSource;

    public LocalPolicyIssueController(PolicyLifecycleService lifecycleService,
                                      LocalPolicyOwnerResolver ownerResolver, DataSource dataSource) {
        this.lifecycleService = lifecycleService;
        this.ownerResolver = ownerResolver;
        this.dataSource = dataSource;
    }

    @PostMapping
    public ResponseEntity<?> issue(@RequestBody PolicyIssueRequest request, Authentication authentication) {
        Optional<String> ownerId = ownerResolver.ownerId(authentication);
        if (ownerId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("code", "UNAUTHENTICATED"));
        }
        PolicySnapshot snapshot = lifecycleService.persistIssueAtomically(dataSource, request, ownerId.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(snapshot);
    }
}
