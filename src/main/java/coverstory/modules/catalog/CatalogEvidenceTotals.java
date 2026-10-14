package coverstory.modules.catalog;

import java.math.BigDecimal;

/** Exact imported-source control total; this is never a consumer premium or quote. */
public record CatalogEvidenceTotals(long publishedCount, BigDecimal knownBaseTotalMinor) { }
