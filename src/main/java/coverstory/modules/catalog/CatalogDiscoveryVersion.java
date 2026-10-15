package coverstory.modules.catalog;

/** Public, non-price catalog version metadata for the persisted discovery contract. */
public record CatalogDiscoveryVersion(
        String versionId,
        String category,
        String scenarioId,
        String sourceId,
        String sourceVersion,
        String evidenceStatus,
        String status) { }
