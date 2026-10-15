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

/** Local instructor route that exercises the Support API boundary; it is profile-gated and read-only. */
@RestController
@Profile("coverstory-instructor-runtime")
@RequestMapping("/api/support/__instructor/lineage")
public final class InstructorSourceLineageController {
    private final SourceLineageReadPort sourceLineage;
    public InstructorSourceLineageController(SourceLineageReadPort sourceLineage) { this.sourceLineage = sourceLineage; }

    @GetMapping("/{citationId}")
    public ResponseEntity<?> read(@PathVariable("citationId") String citationId, HttpServletRequest request) {
        UUID owner = principal(request);
        if (owner == null) return ResponseEntity.status(401).cacheControl(CacheControl.noStore()).build();
        if (!citationId.matches("[A-Za-z0-9_-]{1,80}")) return ResponseEntity.badRequest()
                .cacheControl(CacheControl.noStore()).body(Map.of("code", "INVALID_CITATION_ID"));
        var result = sourceLineage.read(owner, citationId);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(result);
    }

    public static UUID principal(HttpServletRequest request) {
        try {
            var p = request.getUserPrincipal();
            return p == null ? null : UUID.fromString(p.getName());
        } catch (RuntimeException invalid) { return null; }
    }
}
