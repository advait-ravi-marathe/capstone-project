package assignments.insurance;

import java.util.List;
import java.util.Optional;

/** Learner-owned lookup over the supplied synthetic product catalog. */
public final class ProductCatalogService {
    private final List<ProductCatalogProduct> products;

    public ProductCatalogService(List<ProductCatalogProduct> products) {
        this.products = List.copyOf(products);
    }

    public Optional<ProductCatalogProduct> findByCode(String productCode) {
        // LEARNER TODO: return the exact matching fixture product without exposing
        // mutable catalog state; return Optional.empty() for an unknown code.
        throw new UnsupportedOperationException("Day 4 learner TODO: implement product catalog lookup");
    }
}
