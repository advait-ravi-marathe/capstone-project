package coverstory.modules.catalog;

import java.time.OffsetDateTime;

/** Application use case delegates to the Catalog module port; it never reads repository tables directly. */
public final class CatalogEvidenceTotalsUseCase {
    private final CatalogEvidenceTotalsPort evidence;

    public CatalogEvidenceTotalsUseCase(CatalogEvidenceTotalsPort evidence) {
        this.evidence = evidence;
    }

    public CatalogEvidenceTotals forWindow(String editorFixture, OffsetDateTime fromUtc, OffsetDateTime toUtc) {
        return evidence.totals(editorFixture, fromUtc, toUtc);
    }
}
