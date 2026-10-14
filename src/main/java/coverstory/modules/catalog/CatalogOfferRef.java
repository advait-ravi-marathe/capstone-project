package coverstory.modules.catalog;

import java.util.UUID;

/** Stable synthetic catalog identity plus the catalog version used for a quote. */
public record CatalogOfferRef(UUID offerId, String catalogVersion) { }
