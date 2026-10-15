package coverstory.modules.shortlist.api;

import coverstory.modules.catalog.CatalogOfferRef;
import coverstory.modules.shortlist.ShortlistPort;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Owner-scoped saved shortlist HTTP boundary. Catalog IDs identify records; only the principal authorizes access. */
@RestController
@RequestMapping("/api/v1/me/shortlist")
public final class OwnedShortlistController {
    private final ObjectProvider<ShortlistPort> shortlists;
    public OwnedShortlistController(ObjectProvider<ShortlistPort> shortlists) { this.shortlists = shortlists; }

    @GetMapping
    public ResponseEntity<?> list(HttpServletRequest request) {
        UUID owner = owner(request); if (owner == null) return error(401, "AUTH_REQUIRED");
        ShortlistPort port = shortlists.getIfAvailable(); if (port == null) return error(503, "SHORTLIST_UNAVAILABLE");
        try { return ResponseEntity.ok(Map.of("schemaMajor", 1, "items", port.listForOwner(owner))); }
        catch (RuntimeException unavailable) { return error(503, "SHORTLIST_UNAVAILABLE"); }
    }

    @PostMapping("/items")
    public ResponseEntity<?> add(@RequestBody(required=false) SaveRequest body, HttpServletRequest request) {
        UUID owner = owner(request); if (owner == null) return error(401, "AUTH_REQUIRED");
        if (body == null || body.offerId() == null || body.catalogVersion() == null || !body.catalogVersion().matches("[A-Za-z0-9._-]{1,80}"))
            return error(400, "INVALID_SELECTION");
        ShortlistPort port = shortlists.getIfAvailable(); if (port == null) return error(503, "SHORTLIST_UNAVAILABLE");
        try { port.add(owner, new CatalogOfferRef(body.offerId(), body.catalogVersion())); return ResponseEntity.status(201).body(Map.of("saved", true)); }
        catch (IllegalArgumentException invalid) { return error(422, "CATALOG_VERSION_UNAVAILABLE"); }
        catch (RuntimeException unavailable) { return error(503, "SHORTLIST_UNAVAILABLE"); }
    }

    @DeleteMapping("/items")
    public ResponseEntity<?> remove(@RequestParam(name="offerId") UUID offerId, HttpServletRequest request) {
        UUID owner = owner(request); if (owner == null) return error(401, "AUTH_REQUIRED");
        ShortlistPort port = shortlists.getIfAvailable(); if (port == null) return error(503, "SHORTLIST_UNAVAILABLE");
        try { port.remove(owner, offerId); return ResponseEntity.noContent().build(); }
        catch (RuntimeException unavailable) { return error(503, "SHORTLIST_UNAVAILABLE"); }
    }

    private static UUID owner(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal == null || principal.getName() == null) return null;
        try { return UUID.fromString(principal.getName()); } catch (IllegalArgumentException invalid) { return null; }
    }
    private static ResponseEntity<Map<String,String>> error(int status, String code) { return ResponseEntity.status(status).body(Map.of("code", code)); }
    public record SaveRequest(UUID offerId, String catalogVersion) { }
}
