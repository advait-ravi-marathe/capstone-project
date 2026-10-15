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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Read-only evidence endpoint used by the isolated instructor profile; no command route is exposed. */
@RestController
@Profile("coverstory-instructor-runtime")
@RequestMapping("/api/support/__instructor/applications")
public final class InstructorLifecycleEvidenceController {
    private final LifecycleEvidenceReadPort evidence;
    public InstructorLifecycleEvidenceController(LifecycleEvidenceReadPort evidence) { this.evidence = evidence; }

    @GetMapping("/{applicationId}/lifecycle")
    public ResponseEntity<?> read(@PathVariable("applicationId") UUID applicationId,
            @RequestParam(name="maxRows", defaultValue="20") int maxRows,
            @RequestParam(name="maxUtf8Bytes", defaultValue="4096") int maxUtf8Bytes,
            HttpServletRequest request) {
        UUID owner = InstructorSourceLineageController.principal(request);
        if (owner == null) return ResponseEntity.status(401).cacheControl(CacheControl.noStore()).build();
        if (maxRows < 0 || maxRows > 50 || maxUtf8Bytes < 64 || maxUtf8Bytes > 16384)
            return ResponseEntity.badRequest().cacheControl(CacheControl.noStore())
                    .body(Map.of("code", "INVALID_EVIDENCE_BUDGET"));
        LifecycleEvidenceReadPort.EvidenceResult result;
        try { result = evidence.read(owner, applicationId, maxRows, maxUtf8Bytes); }
        catch (LifecycleEvidenceReadPort.TooSmallException tooSmall) {
            return ResponseEntity.status(413).cacheControl(CacheControl.noStore())
                    .body(Map.of("code", "TOO_SMALL"));
        }
        if (result.availableCount() == 0 && "Evidence not found.".equals(result.safeConclusion()))
            return ResponseEntity.status(404).cacheControl(CacheControl.noStore()).body(Map.of("code", "NOT_FOUND"));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(result);
    }
}
