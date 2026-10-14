package coverstory.modules.catalog;

import java.time.OffsetDateTime;

/** Catalog-owned query boundary for source import evidence control totals. */
public interface CatalogEvidenceTotalsPort {
    CatalogEvidenceTotals totals(String editorFixture, OffsetDateTime fromUtc, OffsetDateTime toUtc);
}
