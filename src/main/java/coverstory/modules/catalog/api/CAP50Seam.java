package coverstory.modules.catalog.api;

import java.util.List;
import java.util.Map;

/** CAP-50 learner-owned immutable ACCEPT counter. */
public final class CAP50Seam {
    public Map<String, Long> acceptedCounts(List<ImportDecision> records) {
        // LEARNER TODO: validate all inputs, then count exact ACCEPT records only.
        throw new UnsupportedOperationException("CAP-50 learner TODO: implement acceptedCounts");
    }
}
