package assignments.insurance;

import java.util.List;

/** Immutable product catalog projection; contains no customer or insurer data. */
public record ProductCatalogProduct(
        String productCode,
        String displayName,
        String productVersion,
        String currency,
        List<CoverageOption> coverageOptions) {
    public ProductCatalogProduct {
        coverageOptions = List.copyOf(coverageOptions);
    }
}
