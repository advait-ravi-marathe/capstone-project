package coverstory.modules.shortlist;

import coverstory.modules.catalog.CatalogOfferRef;
import coverstory.modules.catalog.CatalogPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

/** Shortlist-owned persistence adapter; all reads and writes are scoped by the trusted owner ID. */
public final class JdbcShortlistAdapter implements ShortlistPort {
    private final JdbcTemplate jdbc;
    private final CatalogPort catalog;
    public JdbcShortlistAdapter(JdbcTemplate jdbc, CatalogPort catalog) { this.jdbc = jdbc; this.catalog = catalog; }

    @Override public List<CatalogOfferRef> listForOwner(UUID owner) {
        return jdbc.query("SELECT offer_id,catalog_version FROM shortlist.saved_offer WHERE owner_id=? ORDER BY created_at,offer_id",
                (row, n) -> new CatalogOfferRef(row.getObject(1, UUID.class), row.getString(2)), owner);
    }

    @Override public void add(UUID owner, CatalogOfferRef offer) {
        if (catalog.findVersion(offer.offerId(), offer.catalogVersion()).isEmpty())
            throw new IllegalArgumentException("catalog version is unavailable");
        jdbc.update("INSERT INTO shortlist.saved_offer(owner_id,offer_id,catalog_version) VALUES (?,?,?) " +
                        "ON CONFLICT(owner_id,offer_id) DO UPDATE SET catalog_version=EXCLUDED.catalog_version",
                owner, offer.offerId(), offer.catalogVersion());
    }

    @Override public void remove(UUID owner, UUID offerId) {
        jdbc.update("DELETE FROM shortlist.saved_offer WHERE owner_id=? AND offer_id=?", owner, offerId);
    }
}
