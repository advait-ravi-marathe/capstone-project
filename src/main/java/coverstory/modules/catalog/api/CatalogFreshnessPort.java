package coverstory.modules.catalog.api;

import java.util.Map;

/** Read-only projection of the source freshness result consumed by the UI. */
public interface CatalogFreshnessPort {
    Map<String, Object> currentResult();
}
