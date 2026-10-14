package coverstory.modules.catalog.api;

import coverstory.modules.catalog.CatalogListing;
import coverstory.modules.catalog.CatalogPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/** Public catalog read route. The response contains only instructor-owned synthetic examples. */
@RestController
@RequestMapping("/api")
public final class CatalogController {
    private static final Set<String> CATEGORIES = Set.of("health", "term", "motor", "ulip");
    private final CatalogPort catalog;

    public CatalogController(CatalogPort catalog) { this.catalog = catalog; }

    @GetMapping("/catalog")
    public ResponseEntity<?> search(
            @RequestParam(name = "category", defaultValue = "health") String category,
            @RequestParam(name = "query", defaultValue = "") String query) {
        if (!CATEGORIES.contains(category) || query.length() > 100) return ResponseEntity.badRequest().build();
        List<CatalogListing> offers = catalog.search(category, query);
        return ResponseEntity.ok(new CatalogResponse(1, offers));
    }

    public record CatalogResponse(int schemaMajor, List<CatalogListing> offers) { }
}
