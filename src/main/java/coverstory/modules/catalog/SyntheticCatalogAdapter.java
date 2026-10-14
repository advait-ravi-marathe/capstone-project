package coverstory.modules.catalog;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** Public synthetic health examples for local learning only; no insurer terms or payable quotes. */
@Component
public final class SyntheticCatalogAdapter implements CatalogPort {
    private static final List<CatalogListing> HEALTH_EXAMPLES = List.of(
            new CatalogListing("10000000-0000-4000-8000-000000000001", "v1", "Example A", "health", "SYNTHETIC", "UNKNOWN", "UNKNOWN", "UNKNOWN", "Synthetic layout example"),
            new CatalogListing("10000000-0000-4000-8000-000000000002", "v1", "Example B", "health", "SYNTHETIC", "UNKNOWN", "UNKNOWN", "UNKNOWN", "Synthetic layout example"),
            new CatalogListing("10000000-0000-4000-8000-000000000003", "v1", "Example C", "health", "SYNTHETIC", "UNKNOWN", "UNKNOWN", "UNKNOWN", "Synthetic layout example")
    );

    @Override
    public List<CatalogListing> search(String category, String query) {
        if (!"health".equals(category)) return List.of();
        String needle = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        return HEALTH_EXAMPLES.stream()
                .filter(offer -> needle.isEmpty()
                        || offer.displayName().toLowerCase(Locale.ROOT).contains(needle)
                        || offer.offerId().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    @Override
    public Optional<CatalogOfferRef> findVersion(UUID offerId, String catalogVersion) {
        return HEALTH_EXAMPLES.stream()
                .filter(offer -> offer.offerId().equals(offerId.toString()) && offer.catalogVersion().equals(catalogVersion))
                .findFirst()
                .map(offer -> new CatalogOfferRef(offerId, catalogVersion));
    }
}
