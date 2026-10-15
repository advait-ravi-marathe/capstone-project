package coverstory.modules.support.api;

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

/** Synthetic reranking runtime only; clients select a fixed scenario, never evidence IDs or an owner. */
@RestController
@Profile("coverstory-instructor-runtime")
@RequestMapping("/api/support/__instructor/rerank")
public final class InstructorEvidenceRerankingController {
    private final EvidenceRerankingPort reranking;
    public InstructorEvidenceRerankingController(EvidenceRerankingPort reranking) { this.reranking = reranking; }

    @GetMapping("/{scenarioId}")
    public ResponseEntity<?> evaluate(@PathVariable("scenarioId") String scenarioId, HttpServletRequest request) {
        UUID owner = InstructorSourceLineageController.principal(request);
        if (owner == null) return ResponseEntity.status(401).cacheControl(CacheControl.noStore()).build();
        if (!scenarioId.matches("q[12]|timeout|invented")) return ResponseEntity.badRequest()
                .cacheControl(CacheControl.noStore()).body(Map.of("code", "UNKNOWN_FIXED_SCENARIO"));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(reranking.evaluate(owner, scenarioId));
    }
}
