package coverstory.modules.catalog;

import java.util.List;

/** Catalog-owned safe read port. Consumer modules never query catalog tables directly. */
public interface CatalogDiscoveryPort {
    List<CatalogDiscoveryVersion> listHealthVersions();
}
