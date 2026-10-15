package coverstory.modules.catalog.api;

import coverstory.modules.catalog.CatalogDiscoveryPort;
import coverstory.modules.catalog.CatalogDiscoveryVersion;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only public projection for synthetic version history; never a quote or authorization. */
@RestController
@RequestMapping("/api/catalog")
public final class CatalogVersionController {
    private final ObjectProvider<CatalogDiscoveryPort> catalog;

    public CatalogVersionController(ObjectProvider<CatalogDiscoveryPort> catalog) { this.catalog = catalog; }

    @GetMapping("/versions")
    public ResponseEntity<?> versions(@RequestParam(name = "category", defaultValue = "health") String category) {
        if (!"health".equals(category)) return ResponseEntity.badRequest().body(new Unavailable("CATEGORY_NOT_AVAILABLE"));
        CatalogDiscoveryPort port = catalog.getIfAvailable();
        if (port == null) return ResponseEntity.status(503).body(new Unavailable("CATALOG_PERSISTENCE_UNAVAILABLE"));
        try {
            return ResponseEntity.ok(new CatalogEnvelope(1, port.listHealthVersions()));
        } catch (DataAccessException unavailable) {
            return ResponseEntity.status(503).body(new Unavailable("CATALOG_PERSISTENCE_UNAVAILABLE"));
        }
    }

    public record CatalogEnvelope(int schemaMajor, List<CatalogDiscoveryVersion> versions) { }
    public record Unavailable(String code) { }
}
