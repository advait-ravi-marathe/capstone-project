package assignments.local;

import assignments.insurance.PolicyIssueRequest;
import assignments.insurance.PolicyLifecycleService;
import assignments.insurance.PolicySnapshot;
import assignments.insurance.PolicyReadStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final PolicyReadStore policyReadStore;
    private final PolicyIssueReceiptReadStore issueReceiptReadStore;

    public LocalPolicyIssueController(PolicyLifecycleService lifecycleService,
                                      LocalPolicyOwnerResolver ownerResolver, DataSource dataSource,
                                      PolicyReadStore policyReadStore,
                                      PolicyIssueReceiptReadStore issueReceiptReadStore) {
        this.lifecycleService = lifecycleService;
        this.ownerResolver = ownerResolver;
        this.dataSource = dataSource;
        this.policyReadStore = policyReadStore;
        this.issueReceiptReadStore = issueReceiptReadStore;
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<?> read(@PathVariable("policyId") String policyId, Authentication authentication) {
        Optional<String> ownerId = ownerResolver.ownerId(authentication);
        if (ownerId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("code", "UNAUTHENTICATED"));
        }
        return lifecycleService.readForOwner(policyReadStore, ownerId.get(), policyId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("code", "POLICY_NOT_FOUND")));
    }

    @PostMapping
    public ResponseEntity<?> issue(@RequestBody PolicyIssueRequest request, Authentication authentication) {
        Optional<String> ownerId = ownerResolver.ownerId(authentication);
        if (ownerId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("code", "UNAUTHENTICATED"));
        }
        Optional<PolicySnapshot> existing = issueReceiptReadStore.find(ownerId.get(), request.clientRequestId());
        int status = lifecycleService.issueHttpStatus(request, existing);
        if (status == HttpStatus.OK.value() && existing.isPresent()) {
            return ResponseEntity.ok(existing.get());
        }
        if (status != HttpStatus.CREATED.value()) {
            return ResponseEntity.status(status).body(Map.of("code", "ISSUE_REJECTED"));
        }
        PolicySnapshot snapshot = lifecycleService.persistIssueAtomically(dataSource, request, ownerId.get());
        return ResponseEntity.status(status).body(snapshot);
    }
}
