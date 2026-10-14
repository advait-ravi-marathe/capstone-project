package coverstory.modules.catalog;

/** Safe public projection for synthetic discovery. It intentionally has no price or policy claim. */
public record CatalogListing(
        String offerId,
        String catalogVersion,
        String displayName,
        String category,
        String evidenceStatus,
        String waitingPeriodStatus,
        String copayStatus,
        String networkStatus,
        String sourceLabel) { }
