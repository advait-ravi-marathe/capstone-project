package coverstory.modules.catalog.api;

import java.util.*;

/** Learner-owned bounded trace checker; observations are transport evidence, not publication truth. */
public final class CAP167Seam {
    public record PublicationObservation(String partition, long offset, String productId, int version) { }
    public record Diagnostic(String code, String partition, String productId, long expected, long actual) { }

    /** TODO(CAP-167): Check only per-partition offset order and per-product version continuity. */
    public List<Diagnostic> checkTrace(List<PublicationObservation> observations) {
        throw new UnsupportedOperationException("TODO(CAP-167): implement bounded publication trace checker");
    }
}
