package coverstory.modules.shortlist;

import coverstory.modules.catalog.CatalogOfferRef;
import java.util.List;
import java.util.UUID;

/** Owner-scoped shortlist boundary. Authorization is the implementing learner's responsibility. */
public interface ShortlistPort {
    List<CatalogOfferRef> listForOwner(UUID authenticatedOwnerId);
    void add(UUID authenticatedOwnerId, CatalogOfferRef offer);
    void remove(UUID authenticatedOwnerId, UUID offerId);
}
