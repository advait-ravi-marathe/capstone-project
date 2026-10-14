package coverstory.modules.catalog.api;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/** Instructor transport scaffold. An upstream trusted adapter must set the request attribute. */
@RestController
public final class CAP52Controller {
    @FunctionalInterface public interface DecisionPort {
        CAP52Seam.Decision accept(CAP52Seam.Principal principal, CAP52Seam.ImportRequest request);
    }
    private static final Set<String> ALLOWED = Set.of("sourceId", "sourceVersion", "canonicalHash");
    private final DecisionPort decisions;

    public CAP52Controller(CAP52Seam seam) { this(seam::accept); }
    public CAP52Controller(DecisionPort decisions) { this.decisions = decisions; }

    @PostMapping("/api/v1/catalog/imports")
    public ResponseEntity<?> accept(
            @RequestAttribute(value = "trustedEditor", required = false) CAP52Seam.Principal principal,
            @RequestBody JsonNode body) {
        if (principal == null) return ResponseEntity.status(401).build();
        if (principal.role() != CAP52Seam.Role.EDITOR) return ResponseEntity.status(403).build();
        if (body == null || !body.isObject() || !hasExactKeys(body) || !valid(body)) return ResponseEntity.badRequest().build();

        CAP52Seam.ImportRequest request = new CAP52Seam.ImportRequest(
                body.path("sourceId").textValue(), body.path("sourceVersion").textValue(), body.path("canonicalHash").textValue());
        CAP52Seam.Decision decision = decisions.accept(principal, request);
        return switch (decision) {
            case ACCEPTED -> ResponseEntity.status(201).body("ACCEPTED");
            case DUPLICATE -> ResponseEntity.ok("DUPLICATE");
            case CONFLICT -> ResponseEntity.status(409).body("CONFLICT");
            case INVALID -> ResponseEntity.badRequest().body("INVALID");
        };
    }

    private static boolean hasExactKeys(JsonNode body) {
        var names = body.fieldNames();
        int count = 0;
        while (names.hasNext()) {
            if (!ALLOWED.contains(names.next())) return false;
            count++;
        }
        return count == ALLOWED.size();
    }

    private static boolean valid(JsonNode body) {
        JsonNode hash = body.get("canonicalHash");
        return nonBlank(body.get("sourceId")) && nonBlank(body.get("sourceVersion"))
                && hash != null && hash.isTextual()
                && hash.textValue().matches("(?i)[0-9a-f]{64}");
    }

    private static boolean nonBlank(JsonNode value) {
        return value != null && value.isTextual() && !value.textValue().isBlank();
    }
}
