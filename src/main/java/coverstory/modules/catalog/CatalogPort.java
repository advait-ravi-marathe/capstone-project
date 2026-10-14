package coverstory.modules.catalog;

import java.util.List;
import java.util.UUID;

/** Public catalog module interface. No consumer module imports catalog persistence. */
public interface CatalogPort {
    List<CatalogOfferRef> search(String query);
    CatalogOfferRef findVersion(UUID offerId, String catalogVersion);
}
