package capstone.coverstory.catalog;

import java.util.List;
import java.util.UUID;

/** Day 49 learner seam for citation validity at answer time; historical quote evidence is retained. */
public final class InvalidateRetiredCitations {
    public record Citation(String sourceId, String version, UUID ownerId, String contentSha256) { }
    public record SourceState(String sourceId, String version, boolean active, UUID ownerId, String contentSha256) { }
    public record Result(List<Citation> usable, List<Citation> invalidated, boolean answerable) { }

    public Result evaluate(UUID requester, List<Citation> citations, List<SourceState> currentSources) {
        // LEARNER TODO: require exact source/version/hash and requester ownership for
        // private sources. Retired or changed citations invalidate a cached help answer;
        // preserve historical quote references without treating them as current advice.
        throw new UnsupportedOperationException("CAP-171 learner TODO: invalidate retired citations");
    }
}
