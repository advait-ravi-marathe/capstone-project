package coverstory.modules.catalog;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Test-only HTTP adapter; never registered by the ordinary application. */
@RestController
@org.springframework.context.annotation.Profile("cap57-runtime-test")
@RequestMapping("/api/catalog/__instructor/cap57")
public final class CAP57InstructorPublicationController {
    private final CAP57ReferencePublicationAdapter publisher;

    public CAP57InstructorPublicationController(CAP57ReferencePublicationAdapter publisher) { this.publisher = publisher; }

    @PostMapping("/health/{productId}/publish")
    public ResponseEntity<?> publish(@PathVariable("productId") UUID productId, @RequestBody PublishRequest request,
                              @RequestParam(name = "failBeforeCommit", defaultValue = "false") boolean failBeforeCommit,
                              HttpServletRequest servletRequest) {
        Principal principal = servletRequest.getUserPrincipal();
        UUID editor;
        try { editor = principal == null ? null : UUID.fromString(principal.getName()); }
        catch (RuntimeException invalid) { editor = null; }
        if (editor == null) return ResponseEntity.status(401).build();
        if (request.versionId() == null || request.contentSha256() == null)
            return ResponseEntity.badRequest().body(new SafeError("INVALID_PUBLICATION_REQUEST"));
        CAP57ReferencePublicationAdapter.Result result = publisher.publish(editor, productId, request.versionId(),
                request.contentSha256(), failBeforeCommit);
        return ResponseEntity.status(result.status()).body(new PublishResponse(result.code(), result.productId(), result.versionId()));
    }

    @GetMapping("/health/export")
    public ResponseEntity<?> export() {
        try { return ResponseEntity.ok(new InstructorExport(publisher.exportPublished())); }
        catch (Exception unavailable) { return ResponseEntity.status(503).body(new SafeError("PUBLICATION_UNAVAILABLE")); }
    }

    public record PublishRequest(UUID versionId, String contentSha256) { }
    public record PublishResponse(String code, UUID productId, UUID versionId) { }
    public record InstructorExport(List<CAP57ReferencePublicationAdapter.ExportRow> rows) { }
    public record SafeError(String code) { }
}
