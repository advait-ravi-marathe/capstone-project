package coverstory.modules.catalog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Public catalog module interface. No consumer module imports catalog persistence. */
public interface CatalogPort {
    List<CatalogListing> search(String category, String query);
    Optional<CatalogOfferRef> findVersion(UUID offerId, String catalogVersion);
}
