package coverstory.modules.catalog.api;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** CAP-79 read-only endpoint. No stale source can become a quote or order through this route. */
@RestController
public final class CatalogFreshnessController {
    private final ObjectProvider<CatalogFreshnessPort> results;
    public CatalogFreshnessController(ObjectProvider<CatalogFreshnessPort> results) { this.results = results; }

    @GetMapping("/api/catalog/source-freshness")
    public ResponseEntity<Map<String, Object>> current() {
        CatalogFreshnessPort port = results.getIfAvailable();
        if (port == null) return ResponseEntity.status(501).body(Map.of("code", "FRESHNESS_NOT_IMPLEMENTED"));
        try { return ResponseEntity.ok().body(port.currentResult()); }
        catch (UnsupportedOperationException todo) { return ResponseEntity.status(501).body(Map.of("code", "FRESHNESS_NOT_IMPLEMENTED")); }
        catch (RuntimeException unavailable) { return ResponseEntity.status(503).body(Map.of("code", "FRESHNESS_UNAVAILABLE")); }
    }
}
